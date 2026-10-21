export interface UserProfile {
  userId?: string | number;
  username?: string;
  email?: string;
  fullName?: string;
  designation?: string;
  skills?: string;
  role?: string;
  active?: boolean;
  createdAt?: string | Date;
  updatedAt?: string | Date;
}
