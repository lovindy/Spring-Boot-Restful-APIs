<!-- components/auth/RegisterForm.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <UForm :schema="registerSchema" :state="form" @submit="onSubmit">
      <h2 class="text-2xl font-semibold mb-6">Create Account</h2>

      <UFormGroup label="Email" name="email">
        <UInput v-model="form.email" type="email" placeholder="Enter your email" />
      </UFormGroup>

      <UFormGroup label="Username" name="username">
        <UInput v-model="form.username" type="text" placeholder="Enter your username" />
      </UFormGroup>

      <UFormGroup label="Password" name="password">
        <UInput v-model="form.password" type="password" placeholder="Create password" />
      </UFormGroup>

      <UFormGroup label="Confirm Password" name="confirmPassword">
        <UInput
            v-model="form.confirmPassword"
            type="password"
            placeholder="Confirm password"
        />
      </UFormGroup>

      <UButton type="submit" block :loading="loading" class="mt-6">
        Register
      </UButton>

      <div class="text-center mt-4">
        Already have an account?
        <NuxtLink to="/auth/login" class="text-primary hover:underline">
          Login
        </NuxtLink>
      </div>
    </UForm>
  </UCard>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/composables/useAuth'
import { registerSchema } from "~/validators/auth";

const auth = useAuthStore()
const loading = ref(false)

const form = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const onSubmit = async () => {
  try {
    loading.value = true
    await auth.register({
      email: form.email,
      username: form.username,
      password: form.password
    })
    navigateTo('/auth/verify-email')
  } catch (error: any) {
    useToast().add({
      title: 'Error',
      description: error.message || 'Registration failed',
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}
</script>