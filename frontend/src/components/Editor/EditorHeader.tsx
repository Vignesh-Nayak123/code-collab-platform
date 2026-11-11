import type { Language } from '../../types';

const LANGUAGES: { value: Language; label: string }[] = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' },
];

interface EditorHeaderProps {
  roomCode: string;
  roomName: string;
  language: Language;
  connected: boolean;
  running: boolean;
  showChat: boolean;
  onLanguageChange: (lang: Language) => void;
  onRun: () => void;
  onToggleChat: () => void;
}

export function EditorHeader({
  roomCode, roomName, language, connected, running,
  showChat, onLanguageChange, onRun, onToggleChat,
}: EditorHeaderProps) {

  const copyRoomCode = () => {
    navigator.clipboard.writeText(roomCode);
  };

  return (
    <div className="bg-editor-sidebar border-b border-editor-border px-4 py-2 flex items-center gap-4">
      <div className="flex items-center gap-2 flex-1">
        <span className="text-editor-text font-medium truncate">{roomName}</span>
        <button
          onClick={copyRoomCode}
          className="flex items-center gap-1 px-2 py-1 bg-editor-border rounded text-xs text-gray-400 hover:text-editor-accent transition font-mono"
          title="Click to copy room code"
        >
          {roomCode}
          <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
          </svg>
        </button>

        <div className={`flex items-center gap-1 text-xs ${connected ? 'text-editor-green' : 'text-red-400'}`}>
          <span className={`w-2 h-2 rounded-full ${connected ? 'bg-editor-green' : 'bg-red-400'}`} />
          {connected ? 'Connected' : 'Disconnected'}
        </div>
      </div>

      <select
        value={language}
        onChange={(e) => onLanguageChange(e.target.value as Language)}
        className="bg-editor-bg border border-editor-border rounded px-2 py-1 text-sm text-editor-text focus:outline-none focus:border-editor-accent"
      >
        {LANGUAGES.map((l) => (
          <option key={l.value} value={l.value}>{l.label}</option>
        ))}
      </select>

      <button
        onClick={onToggleChat}
        className={`px-3 py-1 rounded text-sm transition ${showChat
          ? 'bg-editor-accent text-editor-bg'
          : 'bg-editor-border text-gray-400 hover:text-editor-text'}`}
      >
        Chat
      </button>

      <button
        onClick={onRun}
        disabled={running}
        className="flex items-center gap-2 px-4 py-1 bg-editor-green text-editor-bg font-semibold rounded text-sm hover:opacity-90 transition disabled:opacity-50"
      >
        {running ? (
          <>
            <span className="w-3 h-3 border-2 border-editor-bg border-t-transparent rounded-full animate-spin" />
            Running...
          </>
        ) : (
          <>
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M8 5v14l11-7z" />
            </svg>
            Run
          </>
        )}
      </button>
    </div>
  );
}
