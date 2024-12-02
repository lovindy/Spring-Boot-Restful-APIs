<!-- pages/about.vue -->
<template>
  <div class="container mx-auto px-4 py-8">
    <UCard class="max-w-4xl mx-auto">
      <template #header>
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">About Us</h1>
      </template>

      <div class="space-y-6">
        <div class="flex items-center space-x-6">
          <UAvatar
              src="/api/placeholder/150/150"
              alt="Company Logo"
              size="3xl"
              class="shadow-lg"
          />
          <div>
            <h2 class="text-2xl font-semibold text-gray-800 dark:text-gray-200">
              Our Mission
            </h2>
            <p class="text-gray-600 dark:text-gray-400 mt-2">
              We are dedicated to creating innovative solutions that make a difference.
              Our team is passionate about delivering high-quality products and services
              that empower our customers.
            </p>
          </div>
        </div>

        <USeparator/>

        <div class="grid md:grid-cols-2 gap-6">
          <div>
            <h3 class="text-xl font-semibold mb-4">Our Values</h3>
            <UList>
              <UListItem>Innovation</UListItem>
              <UListItem>Integrity</UListItem>
              <UListItem>Customer Focus</UListItem>
              <UListItem>Continuous Learning</UListItem>
            </UList>
          </div>

          <div>
            <h3 class="text-xl font-semibold mb-4">Our Team</h3>
            <p class="text-gray-600 dark:text-gray-400">
              We are a diverse group of professionals committed to excellence.
              Our team brings together unique skills and perspectives to solve
              complex challenges.
            </p>
          </div>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup>
definePageMeta({
  title: 'About Us'
})
</script>
