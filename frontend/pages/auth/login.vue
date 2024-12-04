<!-- pages/login.vue -->
<template>
  <LoginForm/>
</template>

<script setup>
import LoginForm from '~/components/auth/LoginForm.vue'
definePageMeta({
  layout: 'auth'
})
</script>