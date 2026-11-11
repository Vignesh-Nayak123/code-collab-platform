import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { CodeEditor } from '../Editor/CodeEditor';
import { ChatPanel } from '../Chat/ChatPanel';
import { UserList } from '../Presence/UserList';
import { OutputPanel } from '../Editor/OutputPanel';
import { EditorHeader } from '../Editor/EditorHeader';
import { useWebSocket } from '../../hooks/useWebSocket';
import { useRoom } from '../../hooks/useRoom';
import { useCodeExecution } from '../../hooks/useCodeExecution';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { getUserColor } from '../../utils/colors';
import type { ChatMessage, User, Language } from '../../types';
import { v4 as uuidv4 } from 'uuid';

function getOrCreateUsername(): string {
  let username = sessionStorage.getItem('username');
  if (!username) {
    username = 'user-' + uuidv4().slice(0, 6);
    sessionStorage.setItem('username', username);
  }
  return username;
}

export function RoomLayout() {
  const { roomCode } = useParams<{ roomCode: string }>();
  const navigate = useNavigate();
  const { room, joinRoom, loading } = useRoom();
  const { result, running, executeCode, clearResult } = useCodeExecution();

  const [username] = useState(getOrCreateUsername);
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState<Language>('javascript');
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [activeUsers, setActiveUsers] = useState<User[]>([]);
  const [showChat, setShowChat] = useState(false);
  const [showOutput, setShowOutput] = useState(false);

  useEffect(() => {
    if (roomCode) {
      joinRoom(roomCode).then((r) => {
        if (!r) {
          navigate('/');
          return;
        }
        if (r.documentContent) setCode(r.documentContent);
        if (r.language) setLanguage(r.language.toLowerCase() as Language);
      });
    }
  }, [roomCode, joinRoom, navigate]);

  const wsHandlers = useCallback(() => ({
    onCodeChange: (msg: { username: string; content: string }) => {
      if (msg.username !== username) {
        setCode(msg.content);
      }
    },
    onChat: (msg: ChatMessage) => {
      setChatMessages((prev) => [...prev, msg]);
    },
    onUserJoined: (msg: { username: string; color: string; activeUsers: string[] }) => {
      setActiveUsers(msg.activeUsers.map((u) => ({ username: u, color: getUserColor(u) })));
    },
    onUserLeft: (msg: { username: string; activeUsers: string[] }) => {
      setActiveUsers(msg.activeUsers.map((u) => ({ username: u, color: getUserColor(u) })));
    },
  }), [username]);

  const { connected, sendCodeChange, sendChatMessage } = useWebSocket({
    roomCode: roomCode || '',
    username,
    handlers: wsHandlers(),
  });

  const handleCodeChange = useCallback((newCode: string) => {
    setCode(newCode);
    if (roomCode) {
      sendCodeChange({
        roomCode,
        username,
        content: newCode,
        version: Date.now(),
        timestamp: new Date().toISOString(),
      });
    }
  }, [roomCode, username, sendCodeChange]);

  const handleSendChat = useCallback((message: string) => {
    if (roomCode) {
      sendChatMessage({
        roomCode,
        username,
        message,
        timestamp: new Date().toISOString(),
      });
    }
  }, [roomCode, username, sendChatMessage]);

  const handleRunCode = useCallback(() => {
    if (roomCode) {
      setShowOutput(true);
      executeCode(roomCode, language, code, username);
    }
  }, [roomCode, language, code, username, executeCode]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" message="Joining room..." />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-editor-bg">
      <EditorHeader
        roomCode={roomCode || ''}
        roomName={room?.name || 'Collaborative Editor'}
        language={language}
        connected={connected}
        onLanguageChange={setLanguage}
        onRun={handleRunCode}
        running={running}
        onToggleChat={() => setShowChat(!showChat)}
        showChat={showChat}
      />

      <div className="flex flex-1 overflow-hidden">
        <div className="flex flex-col flex-1 overflow-hidden">
          <CodeEditor
            code={code}
            language={language}
            onChange={handleCodeChange}
            username={username}
          />
          {showOutput && (
            <OutputPanel
              result={result}
              running={running}
              onClose={() => { setShowOutput(false); clearResult(); }}
            />
          )}
        </div>

        <div className="flex flex-col border-l border-editor-border" style={{ width: '220px' }}>
          <UserList users={activeUsers} currentUser={username} />
        </div>

        {showChat && (
          <div className="flex flex-col border-l border-editor-border" style={{ width: '280px' }}>
            <ChatPanel
              messages={chatMessages}
              username={username}
              onSendMessage={handleSendChat}
            />
          </div>
        )}
      </div>
    </div>
  );
}
