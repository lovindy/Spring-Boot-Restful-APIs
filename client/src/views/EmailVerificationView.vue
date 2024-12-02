<!-- EmailVerification.vue -->
<template>
  <div class="auth-container">
    <form @submit.prevent="handleSubmit" class="auth-form">
      <h2>Verify Your Email</h2>
      <p class="verification-message">
        Please enter the verification code sent to {{ email }}
      </p>
      <div class="form-group">
        <label for="code">Verification Code</label>
        <input
          type="text"
          id="code"
          v-model="verificationCode"
          required
          class="form-input"
          placeholder="Enter 6-digit code"
        />
      </div>
      <button type="submit" :disabled="loading" class="submit-button">
        {{ loading ? 'Verifying...' : 'Verify Email' }}
      </button>
      <button
        @click="handleResendCode"
        :disabled="resendLoading"
        class="resend-button"
      >
        {{ resendLoading ? 'Sending...' : 'Resend Code' }}
      </button>
      <p v-if="error" class="error-message">{{ error }}</p>
      <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const email = ref(route.query.email as string);
const verificationCode = ref('');
const loading = ref(false);
const resendLoading = ref(false);
const error = ref('');
const successMessage = ref('');

onMounted(() => {
  if (!email.value) {
    router.push('/register');
  }
});

const handleSubmit = async () => {
  try {
    loading.value = true;
    await authStore.verifyEmail({
      email: email.value,
      verificationCode: verificationCode.value
    });
    router.push('/dashboard');
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Verification failed';
  } finally {
    loading.value = false;
  }
};

const handleResendCode = async () => {
  try {
    resendLoading.value = true;
    await authStore.resendVerification(email.value);
    successMessage.value = 'Verification code resent successfully';
    error.value = '';
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to resend code';
    successMessage.value = '';
  } finally {
    resendLoading.value = false;
  }
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 1rem;
}

.auth-form {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: white;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.submit-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  margin-bottom: 1rem;
}

.resend-button {
  width: 100%;
  padding: 0.75rem;
  background-color: transparent;
  color: #4CAF50;
  border: 1px solid #4CAF50;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.submit-button:disabled,
.resend-button:disabled {
  background-color: #cccccc;
  border-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  margin-top: 1rem;
  text-align: center;
}

.success-message {
  color: #28a745;
  margin-top: 1rem;
  text-align: center;
}

.verification-message {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #666;
}

.auth-link {
  text-align: center;
  margin-top: 1rem;
}

.auth-link a {
  color: #4CAF50;
  text-decoration: none;
}

.auth-link a:hover {
  text-decoration: underline;
}
</style>
