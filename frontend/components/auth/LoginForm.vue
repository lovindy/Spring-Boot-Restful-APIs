<!-- components/auth/LoginForm.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <UForm :schema="loginSchema" :state="form" @submit="onSubmit">
      <h2 class="text-2xl font-semibold mb-6">Login</h2>

      <UFormGroup label="Email" name="email">
        <UInput v-model="form.email" type="email" placeholder="Enter your email"/>
      </UFormGroup>

      <UFormGroup label="Password" name="password">
        <UInput v-model="form.password" type="password" placeholder="Enter your password"/>
      </UFormGroup>

      <div class="flex justify-between items-center mt-4 mb-6">
        <UCheckbox v-model="rememberMe" label="Remember me"/>
        <NuxtLink to="/auth/forgot-password" class="text-primary hover:underline">
          Forgot Password?
        </NuxtLink>
      </div>

      <UButton type="submit" block :loading="loading">
        Login
      </UButton>

      <div class="text-center mt-4">
        Don't have an account?
        <NuxtLink to="/auth/register" class="text-primary hover:underline">
          Register
        </NuxtLink>
      </div>
    </UForm>
  </UCard>
</template>

<script setup lang="ts">
import {useAuthStore} from '~/composables/useAuth'
import {loginSchema} from "~/validators/auth";

const auth = useAuthStore()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const onSubmit = async () => {
  try {
    loading.value = true
    await auth.login({
      email: form.email,
      password: form.password
    })
    navigateTo('/dashboard')
  } catch (error: any) {
    useToast().add({
      title: 'Error',
      description: error.message || 'Login failed',
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}
</script>