package com.cce.codeeditor.execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.enums.ExecutionStatus;
import com.cce.codeeditor.repository.ExecutionResultRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerExecutionService {

    private static final long TIMEOUT_SECONDS = 10;
    private static final long MEMORY_LIMIT_MB = 256;

    private final ExecutionResultRepository executionResultRepository;

    /**
     * Directory for temporary code files. Set by docker-compose to a shared
     * volume (/tmp/code-exec) so the Docker daemon can bind-mount the files
     * into execution containers; empty when the backend runs on the host.
     */
    @Value("${EXECUTION_TMP_DIR:}")
    private String executionTmpDir;

    /**
     * Name of the Docker volume holding {@link #executionTmpDir}. When set,
     * temp dirs are mounted from the volume (daemon-visible on any platform,
     * including Docker Desktop on Windows) instead of via a host path.
     */
    @Value("${EXECUTION_TMP_VOLUME:}")
    private String executionTmpVolume;

    public void execute(ExecutionResult result, String code, String language) {
        long startTime = System.currentTimeMillis();
        Path tempDir = null;

        try {
            LanguageRuntime runtime = LanguageRuntime.fromString(language);
            tempDir = createTempDirectory();

            Path codeFile = tempDir.resolve(runtime.getFilename());
            Files.writeString(codeFile, code);

            String dockerCmd = buildDockerCommand(runtime, tempDir);

            ProcessBuilder pb = new ProcessBuilder("sh", "-c", dockerCmd);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                result.setStatus(ExecutionStatus.TIMEOUT);
                result.setError("Execution timed out after " + TIMEOUT_SECONDS + " seconds");
                result.setExitCode(-1);
            } else {
                String stdout = readStream(process.getInputStream());
                String stderr = readStream(process.getErrorStream());
                int exitCode = process.exitValue();

                result.setOutput(stdout);
                result.setError(stderr.isEmpty() ? null : stderr);
                result.setExitCode(exitCode);
                result.setStatus(exitCode == 0 ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED);
            }

        } catch (Exception e) {
            log.error("Execution failed for result: {}", result.getId(), e);
            result.setStatus(ExecutionStatus.FAILED);
            result.setError("Internal execution error: " + e.getMessage());
            result.setExitCode(-1);
        } finally {
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            result.setCompletedAt(Instant.now());
            executionResultRepository.save(result);

            if (tempDir != null) {
                cleanupTempDir(tempDir);
            }
        }
    }

    private Path createTempDirectory() throws IOException {
        if (executionTmpDir == null || executionTmpDir.isBlank()) {
            return Files.createTempDirectory("code-exec-");
        }
        Path parent = Path.of(executionTmpDir);
        Files.createDirectories(parent);
        return Files.createTempDirectory(parent, "code-exec-");
    }

    private String buildDockerCommand(LanguageRuntime runtime, Path tempDir) {
        String codeMount;
        if (executionTmpVolume != null && !executionTmpVolume.isBlank()) {
            // Backend runs in a container: mount this run's temp dir from the
            // named volume it shares with the daemon (works on Windows and Linux).
            codeMount = String.format(
                "--mount type=volume,source=%s,volume-subpath=%s,target=/code,readonly",
                executionTmpVolume, tempDir.getFileName());
        } else {
            // Backend runs on the host: bind the host temp dir directly.
            // Quoted because Windows paths contain backslashes that sh would strip.
            codeMount = String.format("-v '%s':/code:ro", tempDir.toAbsolutePath());
        }

        return String.format(
            "docker run --rm " +
            "--network none " +
            "--memory=%dm " +
            "--memory-swap=%dm " +
            "--cpus=0.5 " +
            "--read-only " +
            "--tmpfs /tmp:size=64m " +
            codeMount + " " +
            "-w /code " +
            "%s " +
            "sh -c '%s'",
            MEMORY_LIMIT_MB, MEMORY_LIMIT_MB,
            runtime.getDockerImage(),
            runtime.getRunCommand()
        );
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 1000) {
                sb.append(line).append("\n");
                lineCount++;
            }
        }
        return sb.toString();
    }

    private void cleanupTempDir(Path dir) {
        try {
            Files.walk(dir)
                 .sorted(java.util.Comparator.reverseOrder())
                 .forEach(path -> {
                     try { Files.delete(path); } catch (IOException ignored) {}
                 });
        } catch (IOException e) {
            log.warn("Failed to cleanup temp directory: {}", dir, e);
        }
    }
}
