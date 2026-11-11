import type { User } from '../../types';

interface UserListProps {
  users: User[];
  currentUser: string;
}

export function UserList({ users, currentUser }: UserListProps) {
  return (
    <div className="p-3">
      <div className="text-xs text-gray-500 uppercase tracking-wider mb-2">
        Participants ({users.length || 1})
      </div>

      {users.length > 0 ? (
        <ul className="space-y-2">
          {users.map((user) => (
            <li key={user.username} className="flex items-center gap-2">
              <div
                className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white flex-shrink-0"
                style={{ backgroundColor: user.color }}
              >
                {user.username.charAt(0).toUpperCase()}
              </div>
              <span className={`text-sm truncate ${user.username === currentUser ? 'text-editor-accent' : 'text-editor-text'}`}>
                {user.username}
                {user.username === currentUser && <span className="text-gray-500 text-xs"> (you)</span>}
              </span>
            </li>
          ))}
        </ul>
      ) : (
        <div className="flex items-center gap-2">
          <div
            className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white flex-shrink-0"
            style={{ backgroundColor: '#89b4fa' }}
          >
            {currentUser.charAt(0).toUpperCase()}
          </div>
          <span className="text-sm text-editor-accent truncate">
            {currentUser} <span className="text-gray-500 text-xs">(you)</span>
          </span>
        </div>
      )}
    </div>
  );
}
