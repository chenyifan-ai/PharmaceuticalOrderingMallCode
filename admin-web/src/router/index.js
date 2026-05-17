import { createRouter, createWebHistory } from 'vue-router'
import { getHomeByUserType, isConsumerUser, isAdminUser, isMerchantUser } from '@/utils/consumer'
import { consumerChildRoutes } from './consumer-routes'
import { cPath } from '@/utils/consumer-path'

const adminRoutes = {
  path: '/',
  component: () => import('@/layout/index.vue'),
  redirect: '/dashboard',
  meta: { requiresAdmin: true },
  children: [
    {
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { title: '数据概览', icon: 'DataAnalysis', fullscreen: true, roles: ['admin', 'merchant'] }
    },
    {
      path: 'products',
      name: 'Products',
      component: () => import('@/views/ProductList.vue'),
      meta: { title: '商品管理', icon: 'Goods', roles: ['admin'] }
    },
    {
      path: 'products/create',
      name: 'ProductCreate',
      component: () => import('@/views/ProductEditPage.vue'),
      meta: { title: '添加药品', hidden: true, roles: ['admin'] }
    },
    {
      path: 'products/:id/edit',
      name: 'ProductEdit',
      component: () => import('@/views/ProductEditPage.vue'),
      meta: { title: '编辑药品', hidden: true, roles: ['admin'] }
    },
    {
      path: 'products/:id',
      name: 'ProductDetail',
      component: () => import('@/views/ProductDetailPage.vue'),
      meta: { title: '商品详情', hidden: true, roles: ['admin'] }
    },
    {
      path: 'merchant-products',
      name: 'MerchantProducts',
      component: () => import('@/views/MerchantProductList.vue'),
      meta: { title: '我的商品', icon: 'Goods', roles: ['merchant'] }
    },
    {
      path: 'orders',
      name: 'Orders',
      component: () => import('@/views/OrderList.vue'),
      meta: { title: '订单管理', icon: 'List', roles: ['admin', 'merchant'] }
    },
    {
      path: 'stock',
      name: 'StockManage',
      component: () => import('@/views/StockManage.vue'),
      meta: { title: '库存管理', icon: 'Box', roles: ['admin', 'merchant'] }
    },
    {
      path: 'invoices',
      name: 'Invoices',
      component: () => import('@/views/InvoiceList.vue'),
      meta: { title: '发票管理', icon: 'Tickets', roles: ['admin'] }
    },
    {
      path: 'merchants',
      name: 'Merchants',
      component: () => import('@/views/MerchantList.vue'),
      meta: { title: '供应商管理', icon: 'Shop', roles: ['admin'] }
    },
    {
      path: 'qualifications',
      name: 'Qualifications',
      component: () => import('@/views/QualificationList.vue'),
      meta: { title: '资质审核', icon: 'Document', roles: ['admin'] }
    },
    {
      path: 'payment-vouchers',
      name: 'PaymentVouchers',
      component: () => import('@/views/PaymentVoucherList.vue'),
      meta: { title: '付款凭证审核', icon: 'Wallet', roles: ['admin'] }
    },
    {
      path: 'refund-audit',
      name: 'RefundAudit',
      component: () => import('@/views/RefundAuditList.vue'),
      meta: { title: '退款审核', icon: 'RefreshLeft', roles: ['admin', 'merchant'] }
    },
    {
      path: 'settlements',
      name: 'Settlements',
      component: () => import('@/views/SettlementList.vue'),
      meta: { title: '结算管理', icon: 'Money', roles: ['admin'] }
    },
    {
      path: 'users',
      name: 'Users',
      component: () => import('@/views/UserList.vue'),
      meta: { title: '用户管理', icon: 'User', roles: ['admin'] }
    }
  ]
}

const consumerRoutes = {
  path: '/c',
  component: () => import('@/layouts/ConsumerLayout.vue'),
  redirect: () => cPath('home'),
  meta: { requiresConsumer: true },
  children: consumerChildRoutes
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  adminRoutes,
  consumerRoutes
]

const router = createRouter({
  history: createWebHistory(),
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
  const userType = userInfo.userType

  if (to.path === '/login') {
    next(getHomeByUserType(userType))
    return
  }

  if (isAdminUser(userType)) {
    next()
    return
  }

  if (to.matched.some(r => r.meta.requiresAdmin) && isConsumerUser(userType)) {
    next(cPath('home'))
    return
  }

  if (to.matched.some(r => r.meta.requiresAdmin) && isMerchantUser(userType)) {
    const allowed = to.matched.every(
      (r) => !r.meta.roles || r.meta.roles.includes('merchant')
    )
    if (!allowed) {
      next('/merchant-products')
      return
    }
  }

  next()
})

export default router
