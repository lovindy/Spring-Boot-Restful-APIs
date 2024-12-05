<template>
  <div>
    <!-- admin dashboard content -->
    <h1 class="text-black">Admin Dashboard</h1>
    <p v-if="user">User ID: {{ user.id }}</p>
    <p v-else>Loading...</p>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/composables/useAuth';

// Set page layout metadata
definePageMeta({
  layout: 'dashboard',
  async setup() {
    const { prefetchAuth } = useAuthStore();
    await prefetchAuth();
  },
});

// Access user data from the auth store
const { user, checkAuth } = useAuthStore();

// Ensure authentication check runs when the component is mounted
onMounted(async () => {
  await checkAuth();
});
</script>