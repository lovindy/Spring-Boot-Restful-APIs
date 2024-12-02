// middleware/auth.ts
export default defineNuxtRouteMiddleware((to, from) => {
    const auth = useAuthStore();
    const user = auth.user;

    // Redirect unauthenticated users to the login page
    if (!auth.isAuthenticated) {
        return navigateTo('/auth/login');
    }

    // Role-based route protection (optional)
    if (to.path.startsWith('/admin') && user?.role !== 'ADMIN') {
        return navigateTo('/employee/dashboard');
    }
});
