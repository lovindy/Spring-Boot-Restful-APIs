// src/axios.js
import axios from 'axios';

// Set the base URL to your Spring Boot backend API
const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/v1/auth',
    headers: {
        'Content-Type': 'application/json',
    },
});

export default axiosInstance;
