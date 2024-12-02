<!-- pages/contact.vue -->
<template>
  <div class="container mx-auto px-4 py-8">
    <UCard class="max-w-2xl mx-auto">
      <template #header>
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">Contact Us</h1>
      </template>

      <form @submit.prevent="submitForm" class="space-y-6">
        <UFormGroup label="Name" name="name" required>
          <UInput
              v-model="form.name"
              placeholder="Your Name"
              icon="i-heroicons-user"
          />
        </UFormGroup>

        <UFormGroup label="Email" name="email" required>
          <UInput
              v-model="form.email"
              type="email"
              placeholder="your.email@example.com"
              icon="i-heroicons-envelope"
          />
        </UFormGroup>

        <UFormGroup label="Message" name="message" required>
          <UTextarea
              v-model="form.message"
              placeholder="Your message..."
              :rows="4"
          />
        </UFormGroup>

        <div class="flex justify-end">
          <UButton
              type="submit"
              color="primary"
              variant="solid"
              :loading="isSubmitting"
          >
            Send Message
          </UButton>
        </div>
      </form>
    </UCard>
  </div>
</template>

<script setup>
const form = reactive({
  name: '',
  email: '',
  message: ''
})

const isSubmitting = ref(false)

const submitForm = async () => {
  // Basic form validation
  if (!form.name || !form.email || !form.message) {
    useToast().add({
      title: 'Validation Error',
      description: 'Please fill out all fields',
      color: 'red'
    })
    return
  }

  isSubmitting.value = true

  try {
    // Simulate form submission (replace with actual API call)
    await new Promise(resolve => setTimeout(resolve, 1500))

    useToast().add({
      title: 'Success',
      description: 'Your message has been sent!',
      color: 'green'
    })

    // Reset form
    form.name = ''
    form.email = ''
    form.message = ''
  } catch (error) {
    useToast().add({
      title: 'Error',
      description: 'Failed to send message. Please try again.',
      color: 'red'
    })
  } finally {
    isSubmitting.value = false
  }
}

definePageMeta({
  title: 'Contact Us'
})
</script>