export interface AuthResponse {
  success?: boolean;
  message?: string;
  token?: string;
  refreshToken?: string;
  resetToken?: string;
  expiresIn?: number;
  refreshTokenExpiresIn?: number;
  resetTokenExpiresIn?: number;
  role?: string;
  userId?: string;
  username?: string;
  fullName?: string;
  designation?: string;
  sessionExpiryWarning?: string;
}