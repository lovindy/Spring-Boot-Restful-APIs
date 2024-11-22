import {createRouter, createWebHistory} from 'vue-router';
import RegisterPage from '@/views/RegisterPage.vue';

const routes = [
    {
        path: '/register',
        name: 'Register',
        component: RegisterPage,
    },
    // Add other routes here
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
});

export default router;
