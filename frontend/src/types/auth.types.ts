export interface AuthUser {
  token: string;
  role: 'USER' | 'ADMIN';
  username: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  role: 'USER' | 'ADMIN';
  username: string;
}
