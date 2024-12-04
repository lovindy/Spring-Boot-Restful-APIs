// composables/useAuth.ts
import {ref, computed} from 'vue'
import {defineStore} from 'pinia'
import type {AuthResponse} from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(null)
    const refreshToken = ref<string | null>(null)
    const user = ref<any | null>(null)
    const pendingEmail = ref<string | null>(null) // New ref to store email during registration
    const isAuthenticated = computed(() => !!token.value)

    const config = useRuntimeConfig()
    const baseURL = config.public.apiBase || 'http://localhost:8080/api/v1'

    // Function to check authentication status and refresh user data
    const checkAuth = async () => {
        try {
            const response = await $fetch<AuthResponse>(`${baseURL}/auth/me`, {
                method: 'GET',
                credentials: 'include',
            })

            user.value = response.user
            return true
        } catch (error) {
            user.value = null
            return false
        }
    }

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

            // Store the email in pendingEmail for verification
            pendingEmail.value = userData.email

            return response
        } catch (error) {
            throw error
        }
    }

    const verifyEmail = async (email: string, verificationCode: string) => {
        try {
            // Use pendingEmail if no email is provided
            const emailToVerify = email || pendingEmail.value

            if (!emailToVerify) {
                throw new Error('No email available for verification')
            }

            const response = await $fetch<AuthResponse>(`${baseURL}/auth/verify-email`, {
                method: 'POST',
                body: {email: emailToVerify, verificationCode}
            })

            // Clear pendingEmail after successful verification
            pendingEmail.value = null

            return response
        } catch (error) {
            throw error
        }
    }

    const resendVerification = async (email: string) => {
        try {
            // Use pendingEmail if no email is provided
            const emailToResend = email || pendingEmail.value

            if (!emailToResend) {
                throw new Error('No email available for resending verification')
            }

            const response = await $fetch<AuthResponse>(`${baseURL}/auth/resend-verification`, {
                method: 'POST',
                body: {email: emailToResend}
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
                body: credentials,
                credentials: 'include'
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
                    },
                    credentials: 'include'
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
        pendingEmail,
        checkAuth,
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