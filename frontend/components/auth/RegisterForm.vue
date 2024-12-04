<!-- components/auth/RegisterForm.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <div v-if="!mounted">
      <!-- Loading placeholder -->
      <div class="animate-pulse">
        <div class="h-8 w-48 bg-gray-200 rounded mb-6"></div>
        <div class="space-y-4">
          <div class="h-10 bg-gray-200 rounded"></div>
          <div class="h-10 bg-gray-200 rounded"></div>
          <div class="h-10 bg-gray-200 rounded"></div>
          <div class="h-10 bg-gray-200 rounded"></div>
        </div>
      </div>
    </div>

    <template v-else>
      <UForm :schema="registerSchema" :state="formState" @submit="onSubmit">
        <h2 class="text-2xl font-semibold mb-6">Create Account</h2>

        <UFormGroup label="Email" name="email">
          <UInput
              v-model="formState.email"
              type="email"
              placeholder="Enter your email"
              autocomplete="email"
          />
        </UFormGroup>

        <UFormGroup label="Username" name="username">
          <UInput
              v-model="formState.username"
              type="text"
              placeholder="Enter your username"
              autocomplete="username"
          />
        </UFormGroup>

        <UFormGroup label="Password" name="password">
          <UInput
              v-model="formState.password"
              type="password"
              placeholder="Create password"
              autocomplete="new-password"
          />
        </UFormGroup>

        <UFormGroup label="Confirm Password" name="confirmPassword">
          <UInput
              v-model="formState.confirmPassword"
              type="password"
              placeholder="Confirm password"
              autocomplete="new-password"
          />
        </UFormGroup>

        <UButton
            type="submit"
            block
            :loading="isLoading"
            :disabled="isLoading"
            class="mt-6"
        >
          {{ isLoading ? 'Creating Account...' : 'Register' }}
        </UButton>

        <div class="text-center mt-4">
          Already have an account?
          <NuxtLink to="/auth/login" class="text-primary hover:underline">
            Login
          </NuxtLink>
        </div>
      </UForm>
    </template>
  </UCard>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import {registerSchema} from "~/validators/auth";
import type {RegisterForm} from "~/types/auth";

const isLoading = ref(false)
const mounted = ref(false)

const formState = reactive<RegisterForm>({
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
})

// Initialize auth store inside setup
const {$pinia} = useNuxtApp()
const auth = useAuthStore($pinia)

const {add: addToast} = useToast()

onMounted(() => {
  mounted.value = true
})

const handleAuthError = (error: unknown) => {
  const errorMessage = error instanceof Error
      ? error.message
      : 'An unexpected error occurred during registration'

  addToast({
    title: 'Registration Failed',
    description: errorMessage,
    color: 'red'
  })
}

const onSubmit = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true

    await auth.register({
      email: formState.email,
      username: formState.username,
      password: formState.password
    })

    await navigateTo('/auth/verify-email')
  } catch (error) {
    handleAuthError(error)
  } finally {
    isLoading.value = false
  }
}
</script>