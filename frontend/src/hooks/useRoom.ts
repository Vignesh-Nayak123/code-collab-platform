import { useState, useCallback } from 'react';
import { roomApi } from '../services/api';
import type { Room, CreateRoomRequest, CreateRoomResponse } from '../types';

export function useRoom() {
  const [room, setRoom] = useState<Room | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createRoom = useCallback(async (request: CreateRoomRequest): Promise<CreateRoomResponse | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await roomApi.create(request);
      return response;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create room';
      setError(msg);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const joinRoom = useCallback(async (roomCode: string): Promise<Room | null> => {
    setLoading(true);
    setError(null);
    try {
      const roomInfo = await roomApi.getInfo(roomCode);
      setRoom(roomInfo);
      return roomInfo;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Room not found';
      setError(msg);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const clearError = useCallback(() => setError(null), []);

  return { room, loading, error, createRoom, joinRoom, clearError };
}
