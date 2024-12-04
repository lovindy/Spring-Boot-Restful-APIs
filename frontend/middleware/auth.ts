// middleware/auth.global.ts
export default defineNuxtRouteMiddleware((to) => {
    const auth = useAuthStore()

    // Public routes that don't require authentication
    const publicRoutes = [
        '/auth/login',
        '/auth/register',
        '/auth/forgot-password',
        '/auth/verify-email'
    ]

    // Check if the route is public
    const isPublicRoute = publicRoutes.includes(to.path)

    // If user is authenticated and tries to access auth pages, redirect to dashboard
    if (auth.isAuthenticated && isPublicRoute) {
        return navigateTo('/dashboard')
    }

    // If user is not authenticated and tries to access protected routes
    if (!auth.isAuthenticated && !isPublicRoute) {
        return navigateTo('/auth/login')
    }

    // Handle role-based access
    if (to.path.startsWith('/admin') && auth.user?.role !== 'ADMIN') {
        return navigateTo('/dashboard')
    }

    if (to.path.startsWith('/employee') && !['EMPLOYEE', 'ADMIN'].includes(auth.user?.role)) {
        return navigateTo('/dashboard')
    }
})