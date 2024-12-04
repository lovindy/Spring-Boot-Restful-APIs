<!-- components/auth/VerifyEmail.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <UForm :schema="verifySchema" :state="form" @submit="onSubmit">
      <h2 class="text-2xl font-semibold mb-6">Verify Email</h2>

      <p class="text-gray-600 mb-6">
        Please enter the verification code sent to
        <span class="font-semibold">{{ emailToVerify }}</span>
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

// Compute the email to use for verification
const emailToVerify = computed(() =>
    auth.pendingEmail || auth.user?.email || 'Unknown Email'
)

const form = reactive({
  code: ''
})

const onSubmit = async () => {
  try {
    loading.value = true

    // Use computed email value
    await auth.verifyEmail(emailToVerify.value, form.code)

    // Show success toast
    useToast().add({
      title: 'Success',
      description: 'Email verified successfully',
      color: 'green'
    })

    navigateTo('/auth/login')
  } catch (error: any) {
    useToast().add({
      title: 'Verification Failed',
      description: error.message || 'Unable to verify email',
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}

const resendCode = async () => {
  try {
    resendLoading.value = true

    // Use computed email value
    await auth.resendVerification(emailToVerify.value)

    useToast().add({
      title: 'Verification Code Resent',
      description: `A new verification code has been sent to ${emailToVerify.value}`,
      color: 'green'
    })
  } catch (error: any) {
    useToast().add({
      title: 'Resend Failed',
      description: error.message || 'Failed to resend verification code',
      color: 'red'
    })
  } finally {
    resendLoading.value = false
  }
}

// Redirect if no email is available for verification
onMounted(() => {
  if (!emailToVerify.value || emailToVerify.value === 'Unknown Email') {
    useToast().add({
      title: 'Error',
      description: 'No email available for verification',
      color: 'red'
    })
    navigateTo('/auth/register')
  }
})
</script>