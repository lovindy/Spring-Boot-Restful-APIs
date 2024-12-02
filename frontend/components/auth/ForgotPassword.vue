<!-- components/auth/ForgotPassword.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <UForm :schema="forgotPasswordSchema" @submit="onSubmit">
      <h2 class="text-2xl font-semibold mb-6">Forgot Password</h2>

      <p class="text-gray-600 mb-6">
        Enter your email address and we'll send you instructions to reset your password.
      </p>

      <UFormGroup label="Email" name="email">
        <UInput v-model="form.email" type="email" placeholder="Enter your email" />
      </UFormGroup>

      <UButton type="submit" block :loading="loading" class="mt-6">
        Send Reset Instructions
      </UButton>

      <div class="text-center mt-4">
        Remember your password?
        <NuxtLink to="/auth/login" class="text-primary hover:underline">
          Login
        </NuxtLink>
      </div>
    </UForm>
  </UCard>
</template>

<script setup lang="ts">
import { z } from 'zod'
import { useAuthStore } from '~/composables/useAuth'

const auth = useAuthStore()
const loading = ref(false)

const form = reactive({
  email: ''
})

const forgotPasswordSchema = z.object({
  email: z.string().email('Invalid email address')
})

const onSubmit = async () => {
  try {
    loading.value = true
    await auth.forgotPassword(form.email)
    useToast().add({
      title: 'Success',
      description: 'Password reset instructions sent to your email',
      color: 'green'
    })
  } catch (error: any) {
    useToast().add({
      title: 'Error',
      description: error.message || 'Failed to send reset instructions',
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}
</script>