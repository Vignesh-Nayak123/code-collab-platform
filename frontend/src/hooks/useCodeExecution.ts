import { useState, useCallback, useRef } from 'react';
import { executionApi } from '../services/api';
import type { ExecutionResponse } from '../types';

export function useCodeExecution() {
  const [result, setResult] = useState<ExecutionResponse | null>(null);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const stopPolling = useCallback(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }, []);

  const executeCode = useCallback(async (roomCode: string, language: string, code: string, username?: string) => {
    setRunning(true);
    setError(null);
    setResult(null);

    try {
      const response = await executionApi.submit({
        roomCode,
        language: language.toUpperCase(),
        code,
        submittedBy: username,
      });

      setResult(response);

      // Poll for result
      let attempts = 0;
      const maxAttempts = 30;
      pollRef.current = setInterval(async () => {
        attempts++;
        try {
          const latest = await executionApi.getResult(response.executionId);
          setResult(latest);

          if (['COMPLETED', 'FAILED', 'TIMEOUT'].includes(latest.status) || attempts >= maxAttempts) {
            stopPolling();
            setRunning(false);
          }
        } catch {
          stopPolling();
          setRunning(false);
        }
      }, 1000);

    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Execution failed';
      setError(msg);
      setRunning(false);
    }
  }, [stopPolling]);

  const clearResult = useCallback(() => {
    setResult(null);
    setError(null);
  }, []);

  return { result, running, error, executeCode, clearResult };
}
