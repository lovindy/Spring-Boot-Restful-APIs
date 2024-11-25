export interface User {
  id: number
  username: string
  email: string
  // Add other user properties
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
}

export interface ApiError {
  message: string
  status: number
}
