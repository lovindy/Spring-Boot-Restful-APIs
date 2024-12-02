<template>
  <div class="min-h-screen bg-gray-100">
    <nav class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <NuxtLink to="/" class="flex items-center">
              Home
            </NuxtLink>
          </div>
          <div class="flex items-center">
            <NuxtLink
                to="/login"
                class="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md"
            >
              Login
            </NuxtLink>
            <NuxtLink
                to="/register"
                class="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md"
            >
              Register
            </NuxtLink>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <slot /> <!-- This is where your page content will be rendered -->
    </main>
  </div>
</template>

<script setup>
// You can add any layout-wide logic here
</script>