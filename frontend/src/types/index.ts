export type Language = 'javascript' | 'python' | 'java' | 'cpp' | 'go' | 'rust';

export interface Room {
  id: string;
  name: string;
  roomCode: string;
  language: Language;
  maxUsers: number;
  activeUsersCount: number;
  status: 'ACTIVE' | 'EXPIRED' | 'CLOSED';
  createdAt: string;
  documentContent?: string;
}

export interface CreateRoomRequest {
  name: string;
  language: Language;
  maxUsers: number;
  createdBy?: string;
}

export interface CreateRoomResponse {
  id: string;
  roomCode: string;
  name: string;
  language: string;
  joinUrl: string;
}

export interface User {
  username: string;
  color: string;
}

export interface CodeChangeMessage {
  roomCode: string;
  username: string;
  content: string;
  version: number;
  timestamp: string;
}

export interface CursorPositionMessage {
  roomCode: string;
  username: string;
  lineNumber: number;
  column: number;
  selectionStartLine?: number;
  selectionStartColumn?: number;
  selectionEndLine?: number;
  selectionEndColumn?: number;
  color: string;
}

export interface ChatMessage {
  roomCode: string;
  username: string;
  message: string;
  timestamp: string;
}

export interface UserJoinedMessage {
  roomCode: string;
  username: string;
  color: string;
  activeUsers: string[];
}

export interface UserLeftMessage {
  roomCode: string;
  username: string;
  activeUsers: string[];
}

export interface ExecutionRequest {
  roomCode: string;
  language: string;
  code: string;
  submittedBy?: string;
}

export interface ExecutionResponse {
  executionId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'TIMEOUT';
  output?: string;
  error?: string;
  exitCode?: number;
  executionTimeMs?: number;
}
