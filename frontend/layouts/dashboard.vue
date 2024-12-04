<!-- layouts/dashboard.vue -->
<template>
  <div class="min-h-screen flex">
    <aside class="w-64 bg-gray-800 text-white p-4">
      <div class="mb-8">
        <h1 class="text-xl font-bold">Dashboard</h1>
      </div>

      <nav class="space-y-2">
        <NuxtLink
            to="/admin/dashboard"
            class="block px-4 py-2 rounded hover:bg-gray-700"
            active-class="bg-gray-700"
        >
          Dashboard
        </NuxtLink>
        <!-- Add more navigation links as needed -->
        <button
            @click="handleLogout"
            :disabled="isLoading"
            class="w-full text-left px-4 py-2 rounded hover:bg-gray-700 mt-8 text-red-400 disabled:opacity-50"
        >
          {{ isLoading ? 'Logging out...' : 'Logout' }}
        </button>
      </nav>
    </aside>

    <main class="flex-1 p-8 bg-gray-100">
      <slot />
    </main>
  </div>
</template>

<script setup>
const router = useRouter()
const { $pinia } = useNuxtApp()
const auth = useAuthStore($pinia)
const { add: addToast } = useToast()
const isLoading = ref(false)

const handleLogout = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true
    await auth.logout()

    addToast({
      title: 'Logged Out',
      description: 'You have been successfully logged out',
      color: 'green'
    })

    // Redirect to login page
    await router.push('/auth/login')
  } catch (error) {
    console.error('Logout error:', error)
    addToast({
      title: 'Logout Failed',
      description: error instanceof Error ? error.message : 'An error occurred during logout',
      color: 'red'
    })
  } finally {
    isLoading.value = false
  }
}

// Add route protection on component mount
onMounted(() => {
  if (!auth.isAuthenticated) {
    router.push('/auth/login')
  }
})
</script>