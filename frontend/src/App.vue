<template>
  <img alt="Vue logo" src="./assets/logo.png">
  <HelloWorld msg="Welcome to Full-Stack Java Spring Boot & Vue.js App"/>
  <RegisterPage/>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'
import RegisterPage from "@/views/RegisterPage.vue";

export default {
  name: 'App',
  components: {
    RegisterPage,
    HelloWorld
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
