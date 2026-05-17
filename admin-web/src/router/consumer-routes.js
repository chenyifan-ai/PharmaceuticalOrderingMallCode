/** 采购端业务子路由（供 /c/* 与独立入口共用） */
export const consumerChildRoutes = [
  {
    path: 'home',
    name: 'CHome',
    component: () => import('@/views/CHome.vue'),
    meta: { title: '首页' }
  },
  {
    path: 'products',
    name: 'CProducts',
    component: () => import('@/views/CProductList.vue'),
    meta: { title: '商品浏览' }
  },
  {
    path: 'product/:id',
    name: 'CProductDetail',
    component: () => import('@/views/CProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: 'package/:id',
    name: 'CPackageDetail',
    component: () => import('@/views/CPackageDetail.vue'),
    meta: { title: '套餐详情' }
  },
  {
    path: 'cart',
    name: 'CCart',
    component: () => import('@/views/CCart.vue'),
    meta: { title: '订货清单' }
  },
  {
    path: 'checkout',
    name: 'CCheckout',
    component: () => import('@/views/CCheckout.vue'),
    meta: { title: '确认订单' }
  },
  {
    path: 'orders',
    name: 'COrders',
    component: () => import('@/views/COrders.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: 'order/:id',
    name: 'COrderDetail',
    component: () => import('@/views/COrderDetail.vue'),
    meta: { title: '订单详情' }
  },
  {
    path: 'profile',
    name: 'CProfile',
    component: () => import('@/views/CProfile.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: 'addresses',
    name: 'CAddresses',
    component: () => import('@/views/CAddresses.vue'),
    meta: { title: '收货地址' }
  },
  {
    path: 'qualification',
    name: 'CQualification',
    component: () => import('@/views/CQualification.vue'),
    meta: { title: '企业资质' }
  },
  {
    path: 'messages',
    name: 'CMessages',
    component: () => import('@/views/CMessages.vue'),
    meta: { title: '消息中心' }
  },
  {
    path: 'pay/:orderId',
    name: 'CPay',
    component: () => import('@/views/CPay.vue'),
    meta: { title: '订单支付' }
  },
  {
    path: 'prescriptions',
    name: 'CPrescriptions',
    component: () => import('@/views/CPrescriptions.vue'),
    meta: { title: '我的处方' }
  },
  {
    path: 'purchase-stats',
    name: 'CPurchaseStats',
    component: () => import('@/views/CPurchaseStats.vue'),
    meta: { title: '采购统计' }
  }
]
