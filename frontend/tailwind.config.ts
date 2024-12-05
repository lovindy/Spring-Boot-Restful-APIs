// tailwind.config.js
/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        './components/**/*.{js,vue,ts}',
        './layouts/**/*.vue',
        './pages/**/*.vue',
        './plugins/**/*.{js,ts}',
        './app.vue',
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: '#3B82F6',
                    dark: '#2563EB',
                    light: '#60A5FA',
                },
                secondary: {
                    DEFAULT: '#1F2937',
                    dark: '#111827',
                    light: '#374151',
                },
            },
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
        },
    },
    plugins: [],
}