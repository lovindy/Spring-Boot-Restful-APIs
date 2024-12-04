<!-- layouts/auth.vue -->
<template>
  <div class="bg-gray-100 min-h-screen flex flex-col">
    <div class="p-4">
      <button
          @click="goBack"
          class="flex items-center text-gray-600 hover:text-gray-800"
      >
        <span class="mr-2">←</span> Back
      </button>
    </div>
    <main class="flex-grow flex items-center justify-center">
      <slot />
    </main>
  </div>
</template>

<script setup>
const router = useRouter()

const goBack = () => {
  router.back()
}
</script>