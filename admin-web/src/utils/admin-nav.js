import { getAppRouter } from '@/utils/app-context'
import { isConsumerStandalone, cPath } from '@/utils/consumer-path'

/** 从采购端跳转到管理后台（B 端） */
export function goToAdminConsole() {
  if (isConsumerStandalone()) {
    window.location.assign(`${window.location.origin}/index.html`)
    return
  }
  const router = getAppRouter()
  if (router) {
    router.push('/dashboard')
    return
  }
  window.location.assign(`${window.location.origin}/`)
}
