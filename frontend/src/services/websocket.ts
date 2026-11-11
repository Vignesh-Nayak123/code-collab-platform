import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type {
  CodeChangeMessage,
  CursorPositionMessage,
  ChatMessage,
  UserJoinedMessage,
  UserLeftMessage,
} from '../types';

type MessageHandler<T> = (message: T) => void;

export interface WebSocketHandlers {
  onCodeChange?: MessageHandler<CodeChangeMessage>;
  onCursorMove?: MessageHandler<CursorPositionMessage>;
  onChat?: MessageHandler<ChatMessage>;
  onUserJoined?: MessageHandler<UserJoinedMessage>;
  onUserLeft?: MessageHandler<UserLeftMessage>;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

class WebSocketService {
  private client: Client | null = null;
  private roomCode: string | null = null;
  private handlers: WebSocketHandlers = {};

  connect(roomCode: string, username: string, handlers: WebSocketHandlers): void {
    this.roomCode = roomCode;
    this.handlers = handlers;

    const wsUrl = import.meta.env.VITE_WS_URL || `${window.location.protocol}//${window.location.host}/ws`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: {
        username,
        roomCode,
      },
      debug: (str) => {
        if (import.meta.env.DEV) console.log('[STOMP]', str);
      },
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('WebSocket connected');
        this.subscribeToRoom(roomCode);
        handlers.onConnect?.();
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
        handlers.onDisconnect?.();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });

    this.client.activate();
  }

  private subscribeToRoom(roomCode: string): void {
    if (!this.client?.connected) return;

    this.client.subscribe(`/topic/room/${roomCode}/code-change`, (msg: IMessage) => {
      this.handlers.onCodeChange?.(JSON.parse(msg.body));
    });

    this.client.subscribe(`/topic/room/${roomCode}/cursor`, (msg: IMessage) => {
      this.handlers.onCursorMove?.(JSON.parse(msg.body));
    });

    this.client.subscribe(`/topic/room/${roomCode}/chat`, (msg: IMessage) => {
      this.handlers.onChat?.(JSON.parse(msg.body));
    });

    this.client.subscribe(`/topic/room/${roomCode}/user-joined`, (msg: IMessage) => {
      this.handlers.onUserJoined?.(JSON.parse(msg.body));
    });

    this.client.subscribe(`/topic/room/${roomCode}/user-left`, (msg: IMessage) => {
      this.handlers.onUserLeft?.(JSON.parse(msg.body));
    });
  }

  sendCodeChange(message: CodeChangeMessage): void {
    this.client?.publish({
      destination: `/app/editor/${message.roomCode}/code-change`,
      body: JSON.stringify(message),
    });
  }

  sendCursorPosition(message: CursorPositionMessage): void {
    this.client?.publish({
      destination: `/app/editor/${message.roomCode}/cursor`,
      body: JSON.stringify(message),
    });
  }

  sendChatMessage(message: ChatMessage): void {
    this.client?.publish({
      destination: `/app/editor/${message.roomCode}/chat`,
      body: JSON.stringify(message),
    });
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
    this.roomCode = null;
  }

  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

export const wsService = new WebSocketService();
