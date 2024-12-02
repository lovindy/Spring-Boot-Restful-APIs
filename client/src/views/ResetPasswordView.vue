<!-- ResetPassword.vue -->
<template>
  <div class="auth-container">
    <form @submit.prevent="handleSubmit" class="auth-form">
      <h2>Set New Password</h2>
      <div class="form-group">
        <label for="password">New Password</label>
        <input
          type="password"
          id="password"
          v-model="form.newPassword"
          required
          class="form-input"
        />
      </div>
      <div class="form-group">
        <label for="confirmPassword">Confirm Password</label>
        <input
          type="password"
          id="confirmPassword"
          v-model="confirmPassword"
          required
          class="form-input"
        />
      </div>
      <button type="submit" :disabled="loading || !isPasswordMatch" class="submit-button">
        {{ loading ? 'Updating...' : 'Reset Password' }}
      </button>
      <p v-if="error" class="error-message">{{ error }}</p>
      <p v-if="!isPasswordMatch" class="error-message">Passwords do not match</p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  email: route.query.email as string,
  resetToken: route.query.token as string,
  newPassword: '',
});

const confirmPassword = ref('');
const loading = ref(false);
const error = ref('');

const isPasswordMatch = computed(() => {
  return form.newPassword === confirmPassword.value;
});

const handleSubmit = async () => {
  if (!isPasswordMatch.value) return;

  try {
    loading.value = true;
    await authStore.resetPassword(form);
    router.push({
      path: '/login',
      query: { message: 'Password reset successful. Please login with your new password.' }
    });
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to reset password';
  } finally {
    loading.value = false;
  }
};
</script>
