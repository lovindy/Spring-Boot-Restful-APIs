<script setup lang="ts">

</script>

<template>
  <header class="bg-gray-100 shadow-md">
    <nav class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex">
          <NuxtLink to="/" class="flex items-center text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md">
            <span class="font-bold text-green-400">Talentexis</span>
          </NuxtLink>
        </div>
        <div class="flex items-center">
          <NuxtLink to="/about" class="text-black hover:text-green-400 px-3 py-2 rounded-md">
            About
          </NuxtLink>
          <NuxtLink to="/contact" class="text-black hover:text-green-400 px-3 py-2 rounded-md">
            Contact
          </NuxtLink>
          <NuxtLink to="/auth/login" class="text-black hover:text-green-400 px-3 py-2 rounded-md">
            Login
          </NuxtLink>
          <NuxtLink to="/auth/register" class="text-black hover:text-green-400 px-3 py-2 rounded-md">
            Register
          </NuxtLink>
        </div>
      </div>
    </nav>
  </header>
</template>

<style scoped>

</style>