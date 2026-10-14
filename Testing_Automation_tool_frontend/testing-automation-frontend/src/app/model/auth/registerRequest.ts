export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
  designation?: string;
  skills?: string;
}
