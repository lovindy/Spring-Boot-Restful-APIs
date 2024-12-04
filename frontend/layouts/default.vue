<template>
  <div class="bg-gray-100 min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-grow">
      <slot />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '~/components/layouts/AppHeader.vue'
import AppFooter from '~/components/layouts/AppFooter.vue'
</script>