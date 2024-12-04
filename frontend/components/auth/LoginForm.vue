<!-- components/auth/LoginForm.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <div v-if="!mounted">
      <!-- Loading placeholder -->
      <div class="animate-pulse">
        <div class="h-8 w-48 bg-gray-200 rounded mb-6"></div>
        <div class="space-y-4">
          <div class="h-10 bg-gray-200 rounded"></div>
          <div class="h-10 bg-gray-200 rounded"></div>
        </div>
      </div>
    </div>

    <template v-else>
      <UForm :schema="loginSchema" :state="formState" @submit="onSubmit">
        <h2 class="text-2xl font-semibold mb-6">Login</h2>

        <UFormGroup label="Email" name="email">
          <UInput
              v-model="formState.email"
              type="email"
              placeholder="Enter your email"
              autocomplete="email"
          />
        </UFormGroup>

        <UFormGroup label="Password" name="password">
          <UInput
              v-model="formState.password"
              type="password"
              placeholder="Enter your password"
              autocomplete="current-password"
          />
        </UFormGroup>

        <div class="flex justify-between items-center mt-4 mb-6">
          <UCheckbox
              v-model="rememberMe"
              label="Remember me"
              name="remember"
          />
          <NuxtLink
              to="/auth/forgot-password"
              class="text-primary hover:underline"
          >
            Forgot Password?
          </NuxtLink>
        </div>

        <UButton
            type="submit"
            block
            :loading="isLoading"
            :disabled="isLoading"
        >
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </UButton>

        <div class="text-center mt-4">
          Don't have an account?
          <NuxtLink to="/auth/register" class="text-primary hover:underline">
            Register
          </NuxtLink>
        </div>
      </UForm>
    </template>
  </UCard>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import {storeToRefs} from 'pinia'
import {loginSchema} from "~/validators/auth";
import type {LoginForm} from "~/types/auth"

const isLoading = ref(false)
const rememberMe = ref(false)
const mounted = ref(false)

const formState = reactive<LoginForm>({
  email: '',
  password: ''
})

// Initialize auth store inside setup
const {$pinia} = useNuxtApp()
const auth = useAuthStore($pinia)
const {user} = storeToRefs(auth)

const {add: addToast} = useToast()

onMounted(() => {
  mounted.value = true
})

const handleAuthError = (error: unknown) => {
  const errorMessage = error instanceof Error
      ? error.message
      : 'An unexpected error occurred during login'

  addToast({
    title: 'Login Failed',
    description: errorMessage,
    color: 'red'
  })
}

const onSubmit = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true

    await auth.login({
      email: formState.email,
      password: formState.password
    })
    console.log("User data login request: ",user.value)

    const redirectPath = user.value?.roles.includes('ADMIN')
        ? '/admin/dashboard'
        : '/employee/dashboard';

    await navigateTo(redirectPath)
  } catch (error) {
    handleAuthError(error)
  } finally {
    isLoading.value = false
  }
}
</script>