export interface CreateManagerRequest {
  username: string;
  email: string;
  fullName: string;
  designation?: string;
  skills?: string;
}

export interface ManagerResponse {
  userId: number;
  username: string;
  fullName: string;
  designation?: string;
  skills?: string;
  email: string;
  role: string;
  active?: boolean;
}
