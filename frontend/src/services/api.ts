import axios from 'axios';
import type { CreateRoomRequest, CreateRoomResponse, Room, ExecutionRequest, ExecutionResponse } from '../types';

const API_BASE = import.meta.env.VITE_API_URL || '/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export const roomApi = {
  create: (request: CreateRoomRequest): Promise<CreateRoomResponse> =>
    api.post('/rooms', request).then((r) => r.data),

  getInfo: (roomCode: string): Promise<Room> =>
    api.get(`/rooms/${roomCode}`).then((r) => r.data),

  close: (roomCode: string): Promise<void> =>
    api.delete(`/rooms/${roomCode}`).then((r) => r.data),

  getDocument: (roomCode: string): Promise<{ content: string }> =>
    api.get(`/rooms/${roomCode}/document`).then((r) => r.data),
};

export const executionApi = {
  submit: (request: ExecutionRequest): Promise<ExecutionResponse> =>
    api.post('/execute', request).then((r) => r.data),

  getResult: (executionId: string): Promise<ExecutionResponse> =>
    api.get(`/execute/${executionId}`).then((r) => r.data),

  getStatus: (executionId: string): Promise<{ status: string }> =>
    api.get(`/execute/${executionId}/status`).then((r) => r.data),
};
