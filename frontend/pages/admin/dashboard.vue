<template>
  <div>
    <!-- admin dashboard content -->
    <h1 class="text-black">Admin Dashboard</h1>
    <div class="text-black">
      <p v-if="hydrated && user">User ID: {{ user.id }}</p>
      <p v-else-if="!hydrated">Loading...</p>
      <p v-else>Error loading user data</p>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue';
import {useAuthStore} from '@/composables/useAuth';

// State to track hydration
const hydrated = ref(false);

// Access user data and authentication functions
const {user, checkAuth} = useAuthStore();

// Set page layout metadata
definePageMeta({
  layout: 'dashboard',
});

// Ensure authentication check runs after component mounts
onMounted(async () => {
  await checkAuth();
  hydrated.value = true;
});
</script>
