// composables/useAuth.ts
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { AuthResponse } from '@/types/auth'

/**
 * Authentication store using Pinia
 * Handles user authentication state and related operations including:
 * - User registration and email verification
 * - Login/logout functionality
 * - Password management (reset, change, forgot)
 * - Authentication state checking
 */
export const useAuthStore = defineStore('auth', () => {
    // State Management
    const state = {
        token: ref<string | null>(null),
        refreshToken: ref<string | null>(null),
        user: ref<any | null>(null),
        pendingEmail: ref<string | null>(null),
        isInitialized: ref(false)
    }

    // Runtime Configuration
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBase || 'http://localhost:8080/api/v1'

    /**
     * Computed property to check if user is authenticated
     * Validates user existence and role assignments
     */
    const isAuthenticated = computed(() => {
        const hasValidUser = state.user.value &&
            state.user.value.roles &&
            state.user.value.roles.length > 0

        // Debug logging for authentication state
        console.log('Authentication state:', {
            userExists: !!state.user.value,
            userRoles: state.user.value?.roles,
            isAuthenticated: hasValidUser,
            isInitialized: state.isInitialized.value
        })

        return hasValidUser
    })

    /**
     * Determines the appropriate dashboard route based on user role
     * @returns {string} Dashboard route path
     */
    const getDashboardRoute = computed(() => {
        if (!state.user.value?.roles) return '/auth/login'

        const userRoles = state.user.value.roles
        if (userRoles.includes('ADMIN')) return '/admin/dashboard'
        if (userRoles.includes('EMPLOYEE')) return '/employee/dashboard'

        return '/unauthorized'
    })

    /**
     * Authentication API Methods
     */
    const authApi = {
        /**
         * Verifies current authentication status
         * Fetches user data if not initialized
         * @returns {Promise<boolean>} Authentication status
         */
        async checkAuth(): Promise<boolean> {
            try {
                if (!state.isInitialized.value) {
                    console.log('Checking authentication status...')
                    const response = await $fetch<AuthResponse>(`${API_BASE_URL}/auth/me`, {
                        method: 'GET',
                        credentials: 'include',
                    })

                    state.user.value = response.user
                    state.isInitialized.value = true
                    console.log('Auth check successful:', response.user)
                    return true
                }
                return isAuthenticated.value
            } catch (error) {
                console.error('Auth check failed:', error)
                state.user.value = null
                state.isInitialized.value = true
                return false
            }
        },

        /**
         * Registers a new user
         * @param userData User registration data
         */
        async register(userData: { email: string; username: string; password: string }) {
            try {
                const response = await $fetch<AuthResponse>(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    body: userData
                })
                state.pendingEmail.value = userData.email
                return response
            } catch (error) {
                throw error
            }
        },

        /**
         * Verifies user email with verification code
         * @param email User email (optional if pendingEmail exists)
         * @param verificationCode Email verification code
         */
        async verifyEmail(email: string, verificationCode: string) {
            const emailToVerify = email || state.pendingEmail.value
            if (!emailToVerify) throw new Error('No email available for verification')

            try {
                const response = await $fetch<AuthResponse>(`${API_BASE_URL}/auth/verify-email`, {
                    method: 'POST',
                    body: { email: emailToVerify, verificationCode }
                })
                state.pendingEmail.value = null
                return response
            } catch (error) {
                throw error
            }
        },

        /**
         * Resends email verification code
         * @param email User email (optional if pendingEmail exists)
         */
        async resendVerification(email: string) {
            const emailToResend = email || state.pendingEmail.value
            if (!emailToResend) throw new Error('No email available for resending verification')

            try {
                return await $fetch<AuthResponse>(`${API_BASE_URL}/auth/resend-verification`, {
                    method: 'POST',
                    body: { email: emailToResend }
                })
            } catch (error) {
                throw error
            }
        },

        /**
         * Authenticates user and redirects to appropriate dashboard
         * @param credentials User login credentials
         */
        async login(credentials: { email: string; password: string }) {
            try {
                const response = await $fetch<AuthResponse>(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    body: credentials,
                    credentials: 'include'
                })

                state.user.value = response.user
                state.isInitialized.value = true

                if (isAuthenticated.value) {
                    const redirectPath = getDashboardRoute.value
                    navigateTo(redirectPath)
                }

                return response
            } catch (error) {
                console.error('Login error:', error)
                throw error
            }
        },

        /**
         * Logs out user and clears authentication state
         */
        async logout() {
            try {
                await $fetch(`${API_BASE_URL}/auth/logout`, {
                    method: 'POST',
                    credentials: 'include'
                })
            } finally {
                state.user.value = null
                state.isInitialized.value = false
                navigateTo('/auth/login')
            }
        },

        /**
         * Password Management Methods
         */
        async forgotPassword(email: string) {
            try {
                return await $fetch<AuthResponse>(`${API_BASE_URL}/auth/forgot-password`, {
                    method: 'POST',
                    body: { email }
                })
            } catch (error) {
                throw error
            }
        },

        async resetPassword(email: string, resetToken: string, newPassword: string) {
            try {
                return await $fetch<AuthResponse>(`${API_BASE_URL}/auth/reset-password`, {
                    method: 'POST',
                    body: { email, resetToken, newPassword }
                })
            } catch (error) {
                throw error
            }
        },

        async changePassword(currentPassword: string, newPassword: string) {
            if (!state.user.value?.email) throw new Error('Not authenticated')

            try {
                return await $fetch<AuthResponse>(`${API_BASE_URL}/auth/change-password`, {
                    method: 'POST',
                    body: { currentPassword, newPassword },
                    credentials: 'include'
                })
            } catch (error) {
                throw error
            }
        }
    }

    // Return store interface
    return {
        ...state,
        isAuthenticated,
        getDashboardRoute,
        ...authApi
    }
})