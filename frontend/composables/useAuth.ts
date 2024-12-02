// composables/useAuth.ts
import {ref, computed} from 'vue'
import {defineStore} from 'pinia'
import type {AuthResponse} from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(null)
    const refreshToken = ref<string | null>(null)
    const user = ref<any | null>(null)
    const isAuthenticated = computed(() => !!token.value)

    const config = useRuntimeConfig()
    const baseURL = config.public.apiBase || 'http://localhost:8080/api/v1'

    const register = async (userData: {
        email: string
        username: string
        password: string
    }) => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/register`, {
                method: 'POST',
                body: userData
            })
            return response
        } catch (error) {
            throw error
        }
    }

    const verifyEmail = async (email: string, verificationCode: string) => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/verify-email`, {
                method: 'POST',
                body: {email, verificationCode}
            })
            return response
        } catch (error) {
            throw error
        }
    }

    const resendVerification = async (email: string) => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/resend-verification`, {
                method: 'POST',
                body: {email}
            })
            return response
        } catch (error) {
            throw error
        }
    }

    const login = async (credentials: { email: string; password: string }) => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/login`, {
                method: 'POST',
                body: credentials
            })

            token.value = response.token
            refreshToken.value = response.refreshToken
            user.value = response.user

            return response
        } catch (error) {
            throw error
        }
    }

    const logout = async () => {
        try {
            if (token.value) {
                await $fetch(`${baseURL}/auth/logout`, {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token.value}`
                    }
                })
            }
        } finally {
            token.value = null
            refreshToken.value = null
            user.value = null
        }
    }

    const forgotPassword = async (email: string) => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/forgot-password`, {
                method: 'POST',
                body: {email}
            })
            return response
        } catch (error) {
            throw error
        }
    }

    const resetPassword = async (email: string, resetToken: string, newPassword: string) => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/reset-password`, {
                method: 'POST',
                body: {email, resetToken, newPassword}
            })
            return response
        } catch (error) {
            throw error
        }
    }

    const changePassword = async (currentPassword: string, newPassword: string) => {
        try {
            if (!token.value || !user.value?.email) throw new Error('Not authenticated')

            const response = await $fetch<AuthResponse>(`${baseURL}/auth/change-password`, {
                method: 'POST',
                body: {currentPassword, newPassword},
                headers: {
                    Authorization: `Bearer ${token.value}`
                }
            })
            return response
        } catch (error) {
            throw error
        }
    }

    return {
        token,
        refreshToken,
        user,
        isAuthenticated,
        register,
        verifyEmail,
        resendVerification,
        login,
        logout,
        forgotPassword,
        resetPassword,
        changePassword
    }
})