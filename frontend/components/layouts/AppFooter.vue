<template>
  <footer class="bg-gray-800 text-white py-6 mt-6">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex justify-between items-center">
      <div>
        &copy; {{ currentYear }} My App. All rights reserved.
      </div>
      <div class="space-x-4">
        <a href="#" class="hover:text-gray-400">Privacy Policy</a>
        <a href="#" class="hover:text-gray-400">Terms of Service</a>
      </div>
    </div>
  </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear()
</script>

<style scoped>

</style>