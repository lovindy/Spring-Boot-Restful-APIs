<!-- components/auth/ForgotPassword.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <div v-if="!mounted">
      <!-- Loading placeholder -->
      <div class="animate-pulse">
        <div class="h-8 w-48 bg-gray-200 rounded mb-6"></div>
        <div class="h-16 bg-gray-100 rounded mb-6"></div>
        <div class="h-10 bg-gray-200 rounded"></div>
      </div>
    </div>

    <template v-else>
      <UForm :state="formState" :schema="forgotPasswordSchema" @submit="onSubmit">
        <h2 class="text-2xl font-semibold mb-6">Forgot Password</h2>
        <p class="text-gray-600 mb-6">
          Enter your email address and we'll send you instructions to reset your password.
        </p>
        <UFormGroup label="Email" name="email">
          <UInput
              v-model="formState.email"
              type="email"
              placeholder="Enter your email"
              autocomplete="email"
          />
        </UFormGroup>
        <UButton
            type="submit"
            block
            :loading="isLoading"
            :disabled="isLoading"
            class="mt-6"
        >
          {{ isLoading ? 'Sending Instructions...' : 'Send Reset Instructions' }}
        </UButton>
        <div class="text-center mt-4">
          Remember your password?
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
import {forgotPasswordSchema} from "~/validators/auth"
import type {ForgotPasswordForm} from "~/types/auth"

const mounted = ref(false)
const isLoading = ref(false)

const formState = reactive<ForgotPasswordForm>({
  email: ''
})

// Initialize auth store inside setup
const {$pinia} = useNuxtApp()
const auth = useAuthStore($pinia)
const {add: addToast} = useToast()

onMounted(() => {
  mounted.value = true
})

const onSubmit = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true
    await auth.forgotPassword(formState.email)

    addToast({
      title: 'Success',
      description: 'Password reset instructions sent to your email',
      color: 'green'
    })
  } catch (error) {
    addToast({
      title: 'Error',
      description: error instanceof Error ? error.message : 'Failed to send reset instructions',
      color: 'red'
    })
  } finally {
    isLoading.value = false
  }
}
</script>