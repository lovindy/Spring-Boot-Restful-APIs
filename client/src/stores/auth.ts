import {defineStore} from 'pinia'
import api from '@/api/axios'
import type {
  User,
  LoginRequest,
  RegistrationRequest,
  EmailVerificationRequest,
  AuthResponse,
  PasswordResetRequest,
  ChangePasswordRequest,
} from '@/types/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    isAuthenticated: !!localStorage.getItem('accessToken'),
  }),

  actions: {
    setTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
    },

    clearTokens() {
      this.accessToken = null
      this.refreshToken = null
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    },

    async register(credentials: RegistrationRequest) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/register', credentials)
        return data
      } catch (error) {
        throw error
      }
    },

    async verifyEmail(verificationData: EmailVerificationRequest) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/verify-email', verificationData)
        if (data.accessToken && data.refreshToken) {
          this.setTokens(data.accessToken, data.refreshToken)
          this.user = data.user || null
          this.isAuthenticated = true
        }
        return data
      } catch (error) {
        throw error
      }
    },

    async resendVerification(email: string) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/resend-verification', {email})
        return data
      } catch (error) {
        throw error
      }
    },

    async login(credentials: LoginRequest) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/login', credentials)
        if (data.accessToken && data.refreshToken) {
          this.setTokens(data.accessToken, data.refreshToken)
          this.user = data.user || null
          this.isAuthenticated = true
        }
        return data
      } catch (error) {
        throw error
      }
    },

    async logout() {
      try {
        if (this.accessToken) {
          await api.post<AuthResponse>('/auth/logout')
        }
      } finally {
        this.clearTokens()
      }
    },

    async refreshTokens() {
      try {
        if (!this.refreshToken) throw new Error('No refresh token available')

        const {data} = await api.post<AuthResponse>('/auth/refresh-token')
        if (data.accessToken && data.refreshToken) {
          this.setTokens(data.accessToken, data.refreshToken)
        }
        return data
      } catch (error) {
        this.clearTokens()
        throw error
      }
    },

    async forgotPassword(email: string) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/forgot-password', {email})
        return data
      } catch (error) {
        throw error
      }
    },

    async resetPassword(resetData: PasswordResetRequest) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/reset-password', resetData)
        return data
      } catch (error) {
        throw error
      }
    },

    async changePassword(passwordData: ChangePasswordRequest) {
      try {
        const {data} = await api.post<AuthResponse>('/auth/change-password', passwordData)
        return data
      } catch (error) {
        throw error
      }
    }
  }
})
