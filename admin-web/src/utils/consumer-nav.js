import { isConsumerStandalone, cPath } from '@/utils/consumer-path'
import { getAppRouter } from '@/utils/app-context'

/** 从管理端进入采购商城（C 端） */
export function goToConsumerMall() {
  if (isConsumerStandalone()) {
    getAppRouter()?.push(cPath('home'))
    return
  }
  const router = getAppRouter()
  if (router) {
    router.push(cPath('home'))
    return
  }
  window.location.assign(`${window.location.origin}/consumer.html`)
}
