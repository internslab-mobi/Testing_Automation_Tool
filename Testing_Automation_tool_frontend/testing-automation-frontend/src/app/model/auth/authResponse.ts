export interface AuthResponse {
  success?: boolean;
  message?: string;
  token: string;
  refreshToken: string;
  expiresIn: number;
  refreshTokenExpiresIn?: number;
}