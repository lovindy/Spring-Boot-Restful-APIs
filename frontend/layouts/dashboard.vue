<!-- layouts/dashboard.vue -->
<template>
  <div class="min-h-screen flex">
    <!-- Sidebar Navigation -->
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
            class="w-full text-left px-4 py-2 rounded hover:bg-gray-700 mt-8 text-red-400"
        >
          Logout
        </button>
      </nav>
    </aside>

    <!-- Main Content -->
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

const handleLogout = async () => {
  try {
    await auth.logout()

    addToast({
      title: 'Logged Out',
      description: 'You have been successfully logged out',
      color: 'green'
    })

    // Redirect to login page
    await router.push('/auth/login')
  } catch (error) {
    addToast({
      title: 'Logout Failed',
      description: error instanceof Error ? error.message : 'An error occurred during logout',
      color: 'red'
    })
  }
}
</script>