<!-- components/auth/VerifyEmail.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <UForm :schema="verifySchema" :state="form" @submit="onSubmit">
      <h2 class="text-2xl font-semibold mb-6">Verify Email</h2>

      <p class="text-gray-600 mb-6">
        Please enter the verification code sent to your email.
      </p>

      <UFormGroup label="Verification Code" name="code">
        <UInput
            v-model="form.code"
            placeholder="Enter verification code"
            class="text-center text-xl tracking-wider"
            maxlength="6"
        />
      </UFormGroup>

      <UButton type="submit" block :loading="loading" class="mt-6">
        Verify Email
      </UButton>

      <div class="text-center mt-4">
        Didn't receive the code?
        <UButton
            variant="link"
            :loading="resendLoading"
            @click="resendCode"
            class="text-primary hover:underline"
        >
          Resend
        </UButton>
      </div>
    </UForm>
  </UCard>
</template>

<script setup lang="ts">
import {useAuthStore} from '~/composables/useAuth'
import {verifySchema} from "~/validators/auth";

const auth = useAuthStore()
const loading = ref(false)
const resendLoading = ref(false)

const form = reactive({
  code: ''
})

const onSubmit = async () => {
  try {
    loading.value = true
    await auth.verifyEmail(auth.user?.email || '', form.code)
    navigateTo('/')
  } catch (error: any) {
    useToast().add({
      title: 'Error',
      description: error.message || 'Verification failed',
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}

const resendCode = async () => {
  try {
    resendLoading.value = true
    await auth.resendVerification(auth.user?.email || '')
    useToast().add({
      title: 'Success',
      description: 'Verification code resent',
      color: 'green'
    })
  } catch (error: any) {
    useToast().add({
      title: 'Error',
      description: error.message || 'Failed to resend code',
      color: 'red'
    })
  } finally {
    resendLoading.value = false
  }
}
</script>