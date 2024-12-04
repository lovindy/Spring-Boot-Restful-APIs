// // plugins/auth.client.ts
// export default defineNuxtPlugin(async () => {
//     const auth = useAuthStore()
//
//     // Use useCookie instead of localStorage
//     const token = useCookie('token')
//     const refreshToken = useCookie('refresh_token')
//     const user = useCookie('user')
//
//     if (token.value && refreshToken.value && user.value) {
//         try {
//             auth.token = token.value
//             auth.refreshToken = refreshToken.value
//             auth.user = JSON.parse(user.value)
//         } catch (error) {
//             token.value = null
//             refreshToken.value = null
//             user.value = null
//         }
//     }
// })