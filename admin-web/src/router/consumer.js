import { createRouter, createWebHistory } from 'vue-router'
import { consumerChildRoutes } from './consumer-routes'
import { consumerHomePath } from '@/utils/consumer-path'
import { isConsumerUser, isAdminUser } from '@/utils/consumer'

const routes = [
  {
    path: '/login',
    name: 'ConsumerLogin',
    component: () => import('@/views/consumer/ConsumerLogin.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/ConsumerLayout.vue'),
    redirect: '/home',
    meta: { requiresConsumer: true },
    children: consumerChildRoutes
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.public) {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }

  let userInfo = {}
  try {
    userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    userInfo = {}
  }

  if (to.path === '/login') {
    next(consumerHomePath())
    return
  }

  if (isConsumerUser(userInfo.userType) || isAdminUser(userInfo.userType)) {
    next()
    return
  }

  if (to.matched.some(r => r.meta.requiresConsumer)) {
    next('/login')
    return
  }

  next()
})

export default router
