import api from './axios';
import type { Book, BookFilters, BookImportResult, BookRequest } from '../types/book.types';

export const getBooks = (libraryId: string, filters: BookFilters = {}) =>
  api.get<Book[]>('/books', { params: { ...filters, libraryId } }).then((r) => r.data);

export const getBook = (id: string) =>
  api.get<Book>(`/books/${id}`).then((r) => r.data);

export const createBook = (data: BookRequest) =>
  api.post<Book>('/books', data).then((r) => r.data);

export const updateBook = (id: string, data: BookRequest) =>
  api.put<Book>(`/books/${id}`, data).then((r) => r.data);

export const deleteBook = (id: string) => api.delete(`/books/${id}`);

export const importBooksFromExcel = (file: File, libraryId: string, sheet?: string) => {
  const form = new FormData();
  form.append('file', file);
  form.append('libraryId', libraryId);
  if (sheet) form.append('sheet', sheet);
  return api.post<BookImportResult>('/books/import', form).then((r) => r.data);
};
