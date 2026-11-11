import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRoom } from '../../hooks/useRoom';
import { LoadingSpinner } from '../common/LoadingSpinner';

export function JoinRoom() {
  const navigate = useNavigate();
  const { joinRoom, loading, error } = useRoom();
  const [roomCode, setRoomCode] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const room = await joinRoom(roomCode.toUpperCase());
    if (room) {
      navigate(`/room/${roomCode.toUpperCase()}`);
    }
  };

  return (
    <div className="max-w-md mx-auto">
      <h2 className="text-2xl font-bold text-editor-text mb-6">Join a Room</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm text-gray-400 mb-1">Room Code</label>
          <input
            type="text"
            required
            value={roomCode}
            onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
            className="w-full bg-editor-sidebar border border-editor-border rounded-lg px-3 py-2 text-editor-text font-mono text-lg tracking-widest focus:outline-none focus:border-editor-accent"
            placeholder="ABCD1234"
            maxLength={8}
          />
        </div>

        {error && (
          <div className="bg-red-900/20 border border-red-500/30 rounded-lg p-3 text-red-400 text-sm">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading || roomCode.length < 6}
          className="w-full bg-editor-accent text-editor-bg font-semibold py-2 px-4 rounded-lg hover:opacity-90 transition disabled:opacity-50 flex items-center justify-center gap-2"
        >
          {loading ? <LoadingSpinner size="sm" /> : null}
          {loading ? 'Joining...' : 'Join Room'}
        </button>
      </form>
    </div>
  );
}
