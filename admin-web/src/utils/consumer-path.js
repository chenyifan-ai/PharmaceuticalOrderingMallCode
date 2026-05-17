/** 是否为采购端独立入口（consumer.html） */
let consumerStandalone = false

export function setConsumerStandalone(value) {
  consumerStandalone = !!value
}

export function isConsumerStandalone() {
  return consumerStandalone
}

/** 采购端路由前缀：独立入口为 ''，管理端内嵌为 '/c' */
export function consumerBase() {
  return consumerStandalone ? '' : '/c'
}

/**
 * 生成采购端页面路径
 * @param {string} sub 如 'products'、'/order/1'、'pay/2'
 */
export function cPath(sub = '') {
  const base = consumerBase()
  if (!sub) return base || '/'
  const part = sub.startsWith('/') ? sub : `/${sub}`
  return base ? `${base}${part}` : part
}

/** 独立入口下采购方登录后首页 */
export function consumerHomePath() {
  return cPath('home')
}
