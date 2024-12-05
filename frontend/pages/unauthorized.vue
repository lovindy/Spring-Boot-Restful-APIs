<!-- pages/unauthorized.vue -->
<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="max-w-md w-full p-6 bg-white rounded-lg shadow-lg text-center">
      <h1 class="text-3xl font-bold text-red-600 mb-4">Unauthorized Access</h1>
      <p class="text-gray-600 mb-6">
        You don't have permission to access this page. Please contact your administrator if you believe this is an error.
      </p>
      <UButton @click="handleReturn" color="primary">
        Return to Dashboard
      </UButton>
    </div>
  </div>
</template>

<script setup>
const { $pinia } = useNuxtApp()
const auth = useAuthStore($pinia)

const handleReturn = () => {
  navigateTo(auth.getDashboardRoute)
}
</script>