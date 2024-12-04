export interface AuthResponse {
    token: string
    refreshToken: string
    user: {
        email: string
    }
}

export interface LoginForm {
    email: string,
    password: string
}

export interface RegisterForm {
    email: string
    username: string
    password: string
    confirmPassword: string
}

export interface ForgotPasswordForm {
    email: string
}