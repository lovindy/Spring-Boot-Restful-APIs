<template>
  <div class="bg-gray-100 min-h-screen flex flex-col">
    <main class="flex-grow flex items-center justify-center">
      <div class="max-w-3xl w-full px-4 sm:px-6 lg:px-8 py-12">
        <h1 class="text-4xl font-bold text-gray-800 mb-4">Welcome to Slaves Management</h1>
        <p class="text-gray-600 mb-8">
          Streamline your employee data, track attendance, and generate comprehensive reports.
        </p>
        <div class="flex justify-center space-x-4">
          <NuxtLink to="/employees" class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-3 px-6 rounded-md">
            Manage Employees
          </NuxtLink>
          <NuxtLink to="/reports" class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium py-3 px-6 rounded-md">
            View Reports
          </NuxtLink>
        </div>
      </div>
    </main>
  </div>
</template>