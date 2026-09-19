import api from './axios';
import type { Book, BookFilters, BookRequest } from '../types/book.types';

export const getBooks = (filters: BookFilters = {}) =>
  api.get<Book[]>('/books', { params: filters }).then((r) => r.data);

export const getBook = (id: string) =>
  api.get<Book>(`/books/${id}`).then((r) => r.data);

export const createBook = (data: BookRequest) =>
  api.post<Book>('/books', data).then((r) => r.data);

export const updateBook = (id: string, data: BookRequest) =>
  api.put<Book>(`/books/${id}`, data).then((r) => r.data);

export const deleteBook = (id: string) => api.delete(`/books/${id}`);
