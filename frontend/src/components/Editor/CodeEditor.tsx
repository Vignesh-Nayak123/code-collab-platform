import { useRef, useCallback } from 'react';
import MonacoEditor, { OnMount, OnChange } from '@monaco-editor/react';
import type { Language } from '../../types';

interface CodeEditorProps {
  code: string;
  language: Language;
  onChange: (value: string) => void;
  username: string;
  readOnly?: boolean;
}

const MONACO_LANGUAGE_MAP: Record<Language, string> = {
  javascript: 'javascript',
  python: 'python',
  java: 'java',
  cpp: 'cpp',
  go: 'go',
  rust: 'rust',
};

export function CodeEditor({ code, language, onChange, readOnly = false }: CodeEditorProps) {
  const editorRef = useRef<Parameters<OnMount>[0] | null>(null);

  const handleMount: OnMount = useCallback((editor) => {
    editorRef.current = editor;
    editor.focus();
  }, []);

  const handleChange: OnChange = useCallback((value) => {
    if (value !== undefined) {
      onChange(value);
    }
  }, [onChange]);

  return (
    <div className="flex-1 overflow-hidden">
      <MonacoEditor
        height="100%"
        language={MONACO_LANGUAGE_MAP[language]}
        value={code}
        theme="vs-dark"
        onChange={handleChange}
        onMount={handleMount}
        options={{
          fontSize: 14,
          fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
          fontLigatures: true,
          minimap: { enabled: true },
          scrollBeyondLastLine: false,
          wordWrap: 'on',
          automaticLayout: true,
          tabSize: 2,
          insertSpaces: true,
          formatOnPaste: true,
          formatOnType: true,
          readOnly,
          cursorBlinking: 'smooth',
          smoothScrolling: true,
          padding: { top: 16, bottom: 16 },
          lineNumbers: 'on',
          renderLineHighlight: 'gutter',
          suggest: { showKeywords: true },
          quickSuggestions: true,
        }}
      />
    </div>
  );
}
