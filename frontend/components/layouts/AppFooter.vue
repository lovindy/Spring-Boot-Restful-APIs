<template>
  <footer class="bg-gray-900 text-gray-300 py-10 m-4 rounded-md">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
        <!-- Company Info -->
        <div>
          <h3 class="text-lg font-semibold text-white">Talentexis</h3>
          <p class="mt-4 text-sm">
            Empowering solutions for modern businesses. Our mission is to deliver excellence and value through innovative technology.
          </p>
        </div>

        <!-- Navigation Links -->
        <div>
          <h4 class="text-sm font-semibold text-white">Quick Links</h4>
          <ul class="mt-4 space-y-2">
            <li><a href="#" class="hover:text-green-400">About Us</a></li>
            <li><a href="#" class="hover:text-green-400">Services</a></li>
            <li><a href="#" class="hover:text-green-400">Careers</a></li>
            <li><a href="#" class="hover:text-green-400">Contact</a></li>
          </ul>
        </div>

        <!-- Resources -->
        <div>
          <h4 class="text-sm font-semibold text-white">Resources</h4>
          <ul class="mt-4 space-y-2">
            <li><a href="#" class="hover:text-green-400">Blog</a></li>
            <li><a href="#" class="hover:text-green-400">Help Center</a></li>
            <li><a href="#" class="hover:text-green-400">Privacy Policy</a></li>
            <li><a href="#" class="hover:text-green-400">Terms of Service</a></li>
          </ul>
        </div>

        <!-- Contact Info -->
        <div>
          <h4 class="text-sm font-semibold text-white">Contact Us</h4>
          <ul class="mt-4 space-y-2 text-sm">
            <li>Email: <a href="mailto:support@myapp.com" class="hover:text-green-400">seangdy085@gmail.com</a></li>
            <li>Phone: <a href="tel:+1234567890" class="hover:text-green-400">+855 (16) 802 387</a></li>
            <li>Address: 123 Business Lane, Suite 100</li>
            <li>Phnom penh city, Cambodia, 56789</li>
          </ul>
        </div>
      </div>

      <!-- Divider -->
      <div class="border-t border-gray-700 mt-8 pt-6 flex flex-col sm:flex-row justify-between items-center">
        <div class="text-sm">&copy; {{ currentYear }} Talentexis. All rights reserved.</div>
        <div class="flex space-x-4 mt-4 sm:mt-0">
          <a href="#" aria-label="Facebook" class="hover:text-green-400">
            <i class="fab fa-facebook-f"></i>
          </a>
          <a href="#" aria-label="Twitter" class="hover:text-green-400">
            <i class="fab fa-twitter"></i>
          </a>
          <a href="#" aria-label="LinkedIn" class="hover:text-green-400">
            <i class="fab fa-linkedin-in"></i>
          </a>
          <a href="#" aria-label="Instagram" class="hover:text-green-400">
            <i class="fab fa-instagram"></i>
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear();
</script>

<style scoped>
footer a {
  transition: color 0.3s;
}
i {
  font-size: 1.25rem;
}
</style>
