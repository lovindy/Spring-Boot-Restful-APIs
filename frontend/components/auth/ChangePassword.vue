<!-- components/auth/ChangePassword.vue -->
<template>
  <UCard class="w-full max-w-md mx-auto p-6">
    <UForm :state="form" :schema="changePasswordSchema" @submit="onSubmit">
      <h2 class="text-2xl font-semibold mb-6">Change Password</h2>

      <UFormGroup label="Current Password" name="currentPassword">
        <UInput
            v-model="form.currentPassword"
            type="password"
            placeholder="Enter current password"
        />
      </UFormGroup>

      <UFormGroup label="New Password" name="newPassword">
        <UInput
            v-model="form.newPassword"
            type="password"
            placeholder="Enter new password"
        />
      </UFormGroup>

      <UFormGroup label="Confirm New Password" name="confirmPassword">
        <UInput
            v-model="form.confirmPassword"
            type="password"
            placeholder="Confirm new password"
        />
      </UFormGroup>

      <UButton type="submit" block :loading="loading" class="mt-6">
        Change Password
      </UButton>
    </UForm>
  </UCard>
</template>

<script setup lang="ts">
import { z } from 'zod'
import { useAuthStore } from '~/composables/useAuth'

const auth = useAuthStore()
const loading = ref(false)

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const changePasswordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(6, 'New password must be at least 6 characters'),
  confirmPassword: z.string()
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"]
})

const onSubmit = async () => {
  try {
    loading.value = true
    await auth.changePassword(form.currentPassword, form.newPassword)
    useToast().add({
      title: 'Success',
      description: 'Password changed successfully',
      color: 'green'
    })
    navigateTo('/')
  } catch (error: any) {
    useToast().add({
      title: 'Error',
      description: error.message || 'Failed to change password',
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}
</script>