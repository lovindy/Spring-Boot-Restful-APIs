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
        console.log('User is authenticated and trying to access a public route');
        if (auth.user?.role === 'ADMIN') {
            console.log('Redirecting admin user to /admin/dashboard');
            return navigateTo('/admin/dashboard')
        } else if (auth.user?.role === 'EMPLOYEE') {
            console.log('Redirecting employee user to /employee/dashboard');
            return navigateTo('/employee/dashboard')
        } else {
            console.log('User has an unknown role:', auth.user?.role);
            return navigateTo('/unauthorized')
        }
    }

    // If user is not authenticated and tries to access protected routes
    if (!auth.isAuthenticated && !isPublicRoute) {
        console.log('User is not authenticated and trying to access a protected route');
        return navigateTo('/auth/login')
    }

    // Handle role-based access
    if (to.path.startsWith('/admin') && auth.user?.role !== 'ADMIN') {
        console.log('User is trying to access an admin route but is not an admin');
        return navigateTo('/unauthorized')
    }

    if (to.path.startsWith('/employee') && auth.user?.role !== 'EMPLOYEE') {
        console.log('User is trying to access an employee route but is not an employee');
        return navigateTo('/unauthorized')
    }
})