// composables/useAuth.ts
import {ref, computed} from 'vue'
import {defineStore} from 'pinia'
import type {AuthResponse} from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(null)
    const refreshToken = ref<string | null>(null)
    const user = ref<any | null>(null)
    const pendingEmail = ref<string | null>(null)
    const isInitialized = ref(false)

    // Check the user authentication
    const isAuthenticated = computed(() => {
        const isValid = (
            user.value &&
            user.value.roles &&
            user.value.roles.length > 0
        )

        console.log('Authentication state:', {
            userExists: !!user.value,
            userRoles: user.value?.roles,
            isAuthenticated: isValid,
            isInitialized: isInitialized.value
        })

        return isValid
    })


    // Add helper function to determine user's dashboard route
    const getDashboardRoute = computed(() => {
        if (!user.value?.roles) return '/auth/login'

        if (user.value.roles.includes('ADMIN')) {
            return '/admin/dashboard'
        }

        if (user.value.roles.includes('EMPLOYEE')) {
            return '/employee/dashboard'
        }

        return '/unauthorized'
    })

    const config = useRuntimeConfig()
    const baseURL = config.public.apiBase || 'http://localhost:8080/api/v1'

    // Function to check authentication status and refresh user data
    const checkAuth = async () => {
        try {
            if (!isInitialized.value) {
                console.log('Checking authentication status...')
                const response = await $fetch<AuthResponse>(`${baseURL}/auth/me`, {
                    method: 'GET',
                    credentials: 'include',
                })

                user.value = response.user
                isInitialized.value = true
                console.log('Auth check successful:', response.user)
                return true
            }
            return isAuthenticated.value
        } catch (error) {
            console.error('Auth check failed:', error)
            user.value = null
            isInitialized.value = true
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

            user.value = response.user
            isInitialized.value = true

            if (isAuthenticated.value) {
                const redirectPath = user.value?.roles.includes('ADMIN')
                    ? '/admin/dashboard'
                    : '/employee/dashboard'

                navigateTo(redirectPath)
            }

            return response
        } catch (error) {
            console.error('Login error:', error)
            throw error
        }
    }

    const logout = async () => {
        try {
            await $fetch(`${baseURL}/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            })
        } finally {
            user.value = null
            isInitialized.value = false
            navigateTo('/auth/login')
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

    // Update changePassword to use cookie authentication
    const changePassword = async (currentPassword: string, newPassword: string) => {
        try {
            if (!user.value?.email) throw new Error('Not authenticated')

            const response = await $fetch<AuthResponse>(`${baseURL}/auth/change-password`, {
                method: 'POST',
                body: {currentPassword, newPassword},
                credentials: 'include'  // Use cookies for authentication
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
        isInitialized,
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