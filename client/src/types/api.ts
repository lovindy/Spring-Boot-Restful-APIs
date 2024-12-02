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

export interface AuthResponse {
  accessToken?: string
  refreshToken?: string
  user?: User
  message: string
}

export interface RegistrationRequest {
  username: string
  email: string
  password: string
}

export interface EmailVerificationRequest {
  email: string
  verificationCode: string
}

export interface PasswordResetRequest {
  email: string
  resetToken: string
  newPassword: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}
