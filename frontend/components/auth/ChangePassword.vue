<!-- components/auth/ChangePassword.vue -->
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
        </div>
      </div>
    </div>

    <template v-else>
      <UForm :state="formState" :schema="changePasswordSchema" @submit="onSubmit">
        <h2 class="text-2xl font-semibold mb-6">Change Password</h2>

        <UFormGroup label="Current Password" name="currentPassword">
          <UInput
              v-model="formState.currentPassword"
              type="password"
              placeholder="Enter current password"
              autocomplete="current-password"
          />
        </UFormGroup>

        <UFormGroup label="New Password" name="newPassword">
          <UInput
              v-model="formState.newPassword"
              type="password"
              placeholder="Enter new password"
              autocomplete="new-password"
          />
        </UFormGroup>

        <UFormGroup label="Confirm New Password" name="confirmPassword">
          <UInput
              v-model="formState.confirmPassword"
              type="password"
              placeholder="Confirm new password"
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
          {{ isLoading ? 'Changing Password...' : 'Change Password' }}
        </UButton>
      </UForm>
    </template>
  </UCard>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { changePasswordSchema } from "~/validators/auth"

interface ChangePasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

const mounted = ref(false)
const isLoading = ref(false)

const formState = reactive<ChangePasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Initialize auth store inside setup
const { $pinia } = useNuxtApp()
const auth = useAuthStore($pinia)
const { add: addToast } = useToast()

onMounted(() => {
  mounted.value = true
})

const onSubmit = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true
    await auth.changePassword(formState.currentPassword, formState.newPassword)

    addToast({
      title: 'Success',
      description: 'Password changed successfully',
      color: 'green'
    })

    await navigateTo('/')
  } catch (error) {
    addToast({
      title: 'Error',
      description: error instanceof Error ? error.message : 'Failed to change password',
      color: 'red'
    })
  } finally {
    isLoading.value = false
  }
}
</script>