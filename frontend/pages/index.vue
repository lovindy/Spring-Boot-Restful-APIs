<template>
  <div class="bg-gray-100 min-h-screen flex items-center justify-center">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
        <div>
          <h1 class="text-4xl font-bold text-black mb-4">Streamline Your Employee Data</h1>
          <p class="text-black mb-8">
            Our employee management system helps you track attendance, generate comprehensive reports, and make
            data-driven decisions.
          </p>
          <div class="flex space-x-4">
            <NuxtLink to="/admin/dashboard" class="text-white bg-green-500 hover:bg-gray-200 hover:text-gray-600 font-medium py-3 px-6 rounded-md">
              Manage Employees
            </NuxtLink>
            <NuxtLink to="/admin/dashboard"
                      class="bg-gray-200 hover:bg-green-500 text-gray-800 hover:text-white font-medium py-3 px-6 rounded-md">
              View Reports
            </NuxtLink>
          </div>
        </div>
        <div>
          <img src="../assets/image/img_1.png" alt="Employee Management" class="rounded-lg shadow-lg">
        </div>
      </div>
    </div>
  </div>
</template>