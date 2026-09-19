import api from './axios';

export interface UserRecord {
  id: string;
  email: string;
  username: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export const getUsers = () =>
  api.get<UserRecord[]>('/admin/users').then((r) => r.data);

export const activateUser = (id: string) =>
  api.patch<UserRecord>(`/admin/users/${id}/activate`).then((r) => r.data);

export const deactivateUser = (id: string) =>
  api.patch<UserRecord>(`/admin/users/${id}/deactivate`).then((r) => r.data);
