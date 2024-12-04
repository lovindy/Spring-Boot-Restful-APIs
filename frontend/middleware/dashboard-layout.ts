// middleware/dashboard-layout.ts
export default defineNuxtRouteMiddleware((to) => {
    if (to.path.includes('/dashboard') ||
        to.path.startsWith('/admin/') ||
        to.path.startsWith('/employee/')) {
        setPageLayout('dashboard')
    }
})