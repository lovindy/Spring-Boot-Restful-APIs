// middleware/auth.ts
export default defineNuxtRouteMiddleware(async (to) => {
    const { $pinia } = useNuxtApp()
    const auth = useAuthStore($pinia)

    // Wait for authentication check
    await auth.checkAuth()

    if (!auth.isAuthenticated) {
        console.log('Not authenticated, redirecting to login')
        return navigateTo('/auth/login')
    }

    const currentUser = auth.user

    // Define role-based route patterns
    const adminRoutes = /^\/admin/
    const employeeRoutes = /^\/employee/

    // Check if user has the required role for the route
    const hasAdminAccess = currentUser?.roles.includes('ADMIN')
    const hasEmployeeAccess = currentUser?.roles.includes('EMPLOYEE')

    // Handle admin routes
    if (adminRoutes.test(to.path)) {
        if (!hasAdminAccess) {
            console.log('Unauthorized admin access, redirecting')
            if (hasEmployeeAccess) {
                return navigateTo('/employee/dashboard')
            }
            return navigateTo('/unauthorized')
        }
    }

    // Handle employee routes
    if (employeeRoutes.test(to.path)) {
        if (!hasEmployeeAccess && !hasAdminAccess) {
            console.log('Unauthorized employee access, redirecting')
            return navigateTo('/unauthorized')
        }
    }
})