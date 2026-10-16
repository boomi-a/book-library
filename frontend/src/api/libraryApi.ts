import api from './axios';
import type { Library } from '../types/book.types';

export const getLibraries = () => api.get<Library[]>('/libraries').then((r) => r.data);

export const createLibrary = (name: string) =>
  api.post<Library>('/libraries', { name }).then((r) => r.data);
