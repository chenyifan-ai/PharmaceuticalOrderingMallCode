/** 当前应用实例的路由（管理端 / 采购端独立入口各设一次） */
let appRouter = null

export function setAppRouter(router) {
  appRouter = router
}

export function getAppRouter() {
  return appRouter
}
