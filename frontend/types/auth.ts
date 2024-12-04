export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    user: {
        id: number;
        email: string;
        username: string;
        roles: string[];
        emailVerified: boolean;
        twoFactorAuthEnabled: boolean;
        managedEmployeesCount: number;
    }
    message?: string;
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