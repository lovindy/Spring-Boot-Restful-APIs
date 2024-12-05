// plugins/auth.ts
export default defineNuxtPlugin(async (nuxtApp) => {
    // Only run on client-side
    if (process.client) {
        const auth = useAuthStore(nuxtApp.$pinia)

        try {
            await auth.checkAuth()
            console.log('Initial auth check completed')
        } catch (error) {
            console.error('Initial auth check failed:', error)
        }
    }
})