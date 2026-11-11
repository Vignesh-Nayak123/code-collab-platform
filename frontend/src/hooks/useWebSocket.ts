import { useEffect, useRef, useCallback, useState } from 'react';
import { wsService, WebSocketHandlers } from '../services/websocket';
import type { CodeChangeMessage, CursorPositionMessage, ChatMessage } from '../types';

interface UseWebSocketOptions {
  roomCode: string;
  username: string;
  handlers: WebSocketHandlers;
}

export function useWebSocket({ roomCode, username, handlers }: UseWebSocketOptions) {
  const [connected, setConnected] = useState(false);
  const handlersRef = useRef(handlers);

  useEffect(() => {
    handlersRef.current = handlers;
  }, [handlers]);

  useEffect(() => {
    if (!roomCode || !username) return;

    wsService.connect(roomCode, username, {
      ...handlersRef.current,
      onConnect: () => {
        setConnected(true);
        handlersRef.current.onConnect?.();
      },
      onDisconnect: () => {
        setConnected(false);
        handlersRef.current.onDisconnect?.();
      },
    });

    return () => {
      wsService.disconnect();
      setConnected(false);
    };
  }, [roomCode, username]);

  const sendCodeChange = useCallback((message: CodeChangeMessage) => {
    wsService.sendCodeChange(message);
  }, []);

  const sendCursorPosition = useCallback((message: CursorPositionMessage) => {
    wsService.sendCursorPosition(message);
  }, []);

  const sendChatMessage = useCallback((message: ChatMessage) => {
    wsService.sendChatMessage(message);
  }, []);

  return { connected, sendCodeChange, sendCursorPosition, sendChatMessage };
}
