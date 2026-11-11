interface RemoteCursorProps {
  username: string;
  color: string;
  top: number;
  left: number;
}

export function RemoteCursor({ username, color, top, left }: RemoteCursorProps) {
  return (
    <div
      className="absolute pointer-events-none z-10"
      style={{ top, left }}
    >
      <div
        className="w-0.5 h-5"
        style={{ backgroundColor: color }}
      />
      <div
        className="absolute -top-5 left-0 px-1 py-0.5 rounded text-xs text-white whitespace-nowrap"
        style={{ backgroundColor: color, fontSize: '10px' }}
      >
        {username}
      </div>
    </div>
  );
}
