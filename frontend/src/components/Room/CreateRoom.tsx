import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRoom } from '../../hooks/useRoom';
import { LoadingSpinner } from '../common/LoadingSpinner';
import type { Language } from '../../types';

const LANGUAGES: { value: Language; label: string }[] = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' },
];

export function CreateRoom() {
  const navigate = useNavigate();
  const { createRoom, loading, error } = useRoom();
  const [form, setForm] = useState({
    name: '',
    language: 'javascript' as Language,
    maxUsers: 10,
    createdBy: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const response = await createRoom({
      ...form,
      language: form.language.toUpperCase() as Language,
    });
    if (response) {
      navigate(`/room/${response.roomCode}`);
    }
  };

  return (
    <div className="max-w-md mx-auto">
      <h2 className="text-2xl font-bold text-editor-text mb-6">Create a Room</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm text-gray-400 mb-1">Room Name</label>
          <input
            type="text"
            required
            maxLength={100}
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="w-full bg-editor-sidebar border border-editor-border rounded-lg px-3 py-2 text-editor-text focus:outline-none focus:border-editor-accent"
            placeholder="My Coding Session"
          />
        </div>

        <div>
          <label className="block text-sm text-gray-400 mb-1">Language</label>
          <select
            value={form.language}
            onChange={(e) => setForm({ ...form, language: e.target.value as Language })}
            className="w-full bg-editor-sidebar border border-editor-border rounded-lg px-3 py-2 text-editor-text focus:outline-none focus:border-editor-accent"
          >
            {LANGUAGES.map((lang) => (
              <option key={lang.value} value={lang.value}>{lang.label}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm text-gray-400 mb-1">Your Name (optional)</label>
          <input
            type="text"
            value={form.createdBy}
            onChange={(e) => setForm({ ...form, createdBy: e.target.value })}
            className="w-full bg-editor-sidebar border border-editor-border rounded-lg px-3 py-2 text-editor-text focus:outline-none focus:border-editor-accent"
            placeholder="Anonymous"
          />
        </div>

        <div>
          <label className="block text-sm text-gray-400 mb-1">Max Users: {form.maxUsers}</label>
          <input
            type="range"
            min={2}
            max={20}
            value={form.maxUsers}
            onChange={(e) => setForm({ ...form, maxUsers: parseInt(e.target.value) })}
            className="w-full accent-editor-accent"
          />
        </div>

        {error && (
          <div className="bg-red-900/20 border border-red-500/30 rounded-lg p-3 text-red-400 text-sm">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-editor-accent text-editor-bg font-semibold py-2 px-4 rounded-lg hover:opacity-90 transition disabled:opacity-50 flex items-center justify-center gap-2"
        >
          {loading ? <LoadingSpinner size="sm" /> : null}
          {loading ? 'Creating...' : 'Create Room'}
        </button>
      </form>
    </div>
  );
}
