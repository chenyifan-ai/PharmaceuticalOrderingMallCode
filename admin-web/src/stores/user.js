import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login } from '@/api/auth'
import { getHomeByUserType } from '@/utils/consumer'
import { consumerHomePath, isConsumerStandalone } from '@/utils/consumer-path'
import { getAppRouter } from '@/utils/app-context'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 登录
  async function loginAction(loginForm, options = {}) {
    const data = await login(loginForm)
    token.value = data.token
    userInfo.value = data
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))

    const router = getAppRouter()
    if (!router) return

    let target
    if (options.consumerOnly || isConsumerStandalone()) {
      target = consumerHomePath()
    } else {
      target = getHomeByUserType(data.userType)
    }
    router.push(target)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    const router = getAppRouter()
    if (router) router.push('/login')
  }

  return {
    token,
    userInfo,
    loginAction,
    logout
  }
})
