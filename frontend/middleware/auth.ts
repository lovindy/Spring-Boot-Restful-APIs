export default defineNuxtRouteMiddleware(async (to) => {
    const auth = useAuthStore()

    // Check authentication status on each route change
    await auth.checkAuth()

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
        if (auth.user?.role === 'ADMIN') {
            return navigateTo('/admin/dashboard')
        } else if (auth.user?.role === 'EMPLOYEE') {
            return navigateTo('/employee/dashboard')
        } else {
            return navigateTo('/unauthorized')
        }
    }

    // If user is not authenticated and tries to access protected routes
    if (!auth.isAuthenticated && !isPublicRoute) {
        return navigateTo('/auth/login')
    }

    // Handle role-based access
    if (to.path.startsWith('/admin') && auth.user?.role !== 'ADMIN') {
        return navigateTo('/unauthorized')
    }

    if (to.path.startsWith('/employee') && auth.user?.role !== 'EMPLOYEE') {
        return navigateTo('/unauthorized')
    }
})