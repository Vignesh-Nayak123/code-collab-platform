import { useState, useRef, useEffect } from 'react';
import { ChatMessageItem } from './ChatMessage';
import type { ChatMessage } from '../../types';

interface ChatPanelProps {
  messages: ChatMessage[];
  username: string;
  onSendMessage: (message: string) => void;
}

export function ChatPanel({ messages, username, onSendMessage }: ChatPanelProps) {
  const [input, setInput] = useState('');
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = input.trim();
    if (trimmed) {
      onSendMessage(trimmed);
      setInput('');
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="px-3 py-2 border-b border-editor-border text-sm font-medium text-gray-400">
        Chat
      </div>

      <div className="flex-1 overflow-y-auto p-3 space-y-3">
        {messages.length === 0 ? (
          <p className="text-center text-xs text-gray-600 mt-4">No messages yet</p>
        ) : (
          messages.map((msg, i) => (
            <ChatMessageItem
              key={i}
              message={msg}
              isOwn={msg.username === username}
            />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={handleSubmit} className="p-2 border-t border-editor-border">
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Message..."
            maxLength={500}
            className="flex-1 bg-editor-bg border border-editor-border rounded px-2 py-1 text-sm text-editor-text focus:outline-none focus:border-editor-accent"
          />
          <button
            type="submit"
            disabled={!input.trim()}
            className="px-2 py-1 bg-editor-accent text-editor-bg rounded text-sm font-medium disabled:opacity-50 hover:opacity-90 transition"
          >
            →
          </button>
        </div>
      </form>
    </div>
  );
}
