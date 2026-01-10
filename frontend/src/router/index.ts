import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
    },
    {
      path: '/devices',
      name: 'devices',
      component: () => import('@/views/Devices.vue'),
    },
    {
      path: '/light-sensor',
      name: 'light-sensor',
      component: () => import('@/views/LightSensor.vue'),
    },
    {
      path: '/curtain',
      name: 'curtain',
      component: () => import('@/views/Curtain.vue'),
    },
    {
      path: '/automation',
      name: 'automation',
      component: () => import('@/views/Automation.vue'),
    },
  ],
})

export default router
