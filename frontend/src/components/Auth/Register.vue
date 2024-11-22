<template>
  <div>
    <h2>Register</h2>
    <form @submit.prevent="registerUser">
      <div>
        <label for="username">Username:</label>
        <input type="text" v-model="username" id="username" required/>
      </div>
      <div>
        <label for="email">Email:</label>
        <input type="email" v-model="email" id="email" required/>
      </div>
      <div>
        <label for="password">Password:</label>
        <input type="password" v-model="password" id="password" required/>
      </div>
      <button type="submit">Register</button>
    </form>
  </div>
</template>

<script>
import axios from '@/axios';

export default {
  data() {
    return {
      username: '',
      email: '',
      password: '',
    };
  },
  methods: {
    async registerUser() {
      try {
        const response = await axios.post('/register', {
          username: this.username,
          email: this.email,
          password: this.password,
        });
        console.log('User registered successfully:', response.data);
        this.$router.push('/login'); // Redirect to login page after successful registration
      } catch (error) {
        console.error('Error registering user:', error);
        alert('Registration failed. Please try again.');
      }
    },
  },
};
</script>
