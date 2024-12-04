// middleware/auth-layout.ts
export default defineNuxtRouteMiddleware((to) => {
    if (to.path.startsWith('/auth/')) {
        setPageLayout('auth')
    }
})