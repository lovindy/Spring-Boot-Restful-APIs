export interface AuthResponse {
    token: string
    refreshToken: string
    user: {
        email: string
    }
}