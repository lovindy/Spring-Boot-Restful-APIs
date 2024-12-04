<!-- ForgotPassword.vue -->
<template>
  <div class="auth-container">
    <form @submit.prevent="handleSubmit" class="auth-form">
      <h2>Reset Password</h2>
      <p class="reset-message">
        Enter your email address and we'll send you instructions to reset your password.
      </p>
      <div class="form-group">
        <label for="email">Email</label>
        <input
          type="email"
          id="email"
          v-model="email"
          required
          class="form-input"
        />
      </div>
      <button type="submit" :disabled="loading" class="submit-button">
        {{ loading ? 'Sending...' : 'Send Reset Instructions' }}
      </button>
      <p v-if="error" class="error-message">{{ error }}</p>
      <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
      <p class="auth-link">
        Remember your password?
        <router-link to="/login">Login here</router-link>
      </p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const email = ref('');
const loading = ref(false);
const error = ref('');
const successMessage = ref('');

const handleSubmit = async () => {
  try {
    loading.value = true;
    await authStore.forgotPassword(email.value);
    successMessage.value = 'If an account exists with this email, you will receive password reset instructions.';
    error.value = '';
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to process request';
    successMessage.value = '';
  } finally {
    loading.value = false;
  }
};
</script>

