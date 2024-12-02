export default defineNuxtConfig({
    modules: [
        '@nuxt/ui',
        '@pinia/nuxt',
        '@vee-validate/nuxt'
    ],
    compatibilityDate: '2024-12-02',
    runtimeConfig: {
        public: {
            apiBase: 'http://localhost:8080/api/v1'
        }
    }
})