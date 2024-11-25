<template>
    <div class="login-container">
        <form @submit.prevent="handleSubmit" class="login-form">
            <h2>Login</h2>
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" v-model="form.email" required class="form-input" />
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" v-model="form.password" required class="form-input" />
            </div>
            <button type="submit" :disabled="loading" class="submit-button">
                {{ loading ? 'Loading...' : 'Login' }}
            </button>
            <p v-if="error" class="error-message">{{ error }}</p>
        </form>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import type { LoginRequest } from '@/types/api';

const router = useRouter();
const authStore = useAuthStore();

const form = reactive<LoginRequest>({
    email: '',
    password: '',
});

const loading = ref(false);
const error = ref('');

const handleSubmit = async () => {
    try {
        loading.value = true;
        await authStore.login(form);
        router.push('/dashboard');
    } catch (err: any) {
        error.value = err.response?.data?.message || 'Login failed';
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 1rem;
}

.login-form {
    width: 100%;
    max-width: 400px;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
    margin-bottom: 1rem;
}

.form-input {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.submit-button {
    width: 100%;
    padding: 0.75rem;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.submit-button:disabled {
    background-color: #cccccc;
}

.error-message {
    color: red;
    margin-top: 1rem;
}
</style>