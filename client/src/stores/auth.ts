import { defineStore } from 'pinia'
import api from '@/api/axios'
import type { User, LoginRequest, LoginResponse } from '@/types/api'

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem('token'),
    isAuthenticated: !!localStorage.getItem('token'),
  }),

  actions: {
    async login(credentials: LoginRequest) {
      try {
        const { data } = await api.post<LoginResponse>('/auth/login', credentials)
        this.token = data.token
        this.user = data.user
        this.isAuthenticated = true
        localStorage.setItem('token', data.token)
        return data
      } catch (error) {
        throw error
      }
    },

    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    },

    async fetchUser() {
      try {
        const { data } = await api.get<User>('/user/profile')
        this.user = data
      } catch (error) {
        throw error
      }
    },
  },
})
