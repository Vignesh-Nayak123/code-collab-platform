import { getUserColor } from '../../utils/colors';

interface ChatMessageProps {
  message: {
    username: string;
    message: string;
    timestamp: string;
  };
  isOwn: boolean;
}

export function ChatMessageItem({ message, isOwn }: ChatMessageProps) {
  const color = getUserColor(message.username);
  const time = new Date(message.timestamp).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className={`flex flex-col gap-0.5 ${isOwn ? 'items-end' : 'items-start'}`}>
      {!isOwn && (
        <span className="text-xs font-medium" style={{ color }}>
          {message.username}
        </span>
      )}
      <div className={`max-w-[90%] px-3 py-2 rounded-lg text-sm ${
        isOwn
          ? 'bg-editor-accent text-editor-bg'
          : 'bg-editor-border text-editor-text'
      }`}>
        {message.message}
      </div>
      <span className="text-xs text-gray-600">{time}</span>
    </div>
  );
}
