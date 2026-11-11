import { LoadingSpinner } from '../common/LoadingSpinner';
import type { ExecutionResponse } from '../../types';

interface OutputPanelProps {
  result: ExecutionResponse | null;
  running: boolean;
  onClose: () => void;
}

export function OutputPanel({ result, running, onClose }: OutputPanelProps) {
  const isSuccess = result?.exitCode === 0;
  const isTimeout = result?.status === 'TIMEOUT';

  return (
    <div className="h-48 bg-editor-sidebar border-t border-editor-border flex flex-col">
      <div className="flex items-center justify-between px-4 py-2 border-b border-editor-border">
        <div className="flex items-center gap-2 text-sm">
          <span className="text-gray-400">Output</span>
          {result && (
            <span className={`text-xs px-2 py-0.5 rounded ${
              isTimeout ? 'bg-yellow-900/30 text-yellow-400' :
              isSuccess ? 'bg-green-900/30 text-editor-green' :
              'bg-red-900/30 text-red-400'
            }`}>
              {isTimeout ? 'TIMEOUT' : isSuccess ? 'SUCCESS' : 'ERROR'}
            </span>
          )}
          {result?.executionTimeMs && (
            <span className="text-xs text-gray-500">{result.executionTimeMs}ms</span>
          )}
        </div>
        <button
          onClick={onClose}
          className="text-gray-500 hover:text-gray-300 transition"
          aria-label="Close output"
        >
          ✕
        </button>
      </div>

      <div className="flex-1 overflow-auto p-4 font-mono text-sm">
        {running && !result && (
          <div className="flex items-center gap-2 text-gray-400">
            <LoadingSpinner size="sm" />
            <span>Executing code...</span>
          </div>
        )}
        {result?.output && (
          <pre className="text-editor-text whitespace-pre-wrap">{result.output}</pre>
        )}
        {result?.error && (
          <pre className="text-red-400 whitespace-pre-wrap">{result.error}</pre>
        )}
        {isTimeout && (
          <p className="text-yellow-400">Execution timed out. Your code took too long to run.</p>
        )}
      </div>
    </div>
  );
}
