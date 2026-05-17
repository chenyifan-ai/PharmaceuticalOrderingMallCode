import request from '@/utils/request'

// 管理员商品管理
export function getAdminProductList(params) {
  return request({
    url: '/admin/product/list',
    method: 'get',
    params
  })
}

export function getAdminProductDetail(id) {
  return request({
    url: `/admin/product/${id}`,
    method: 'get'
  })
}

export function auditProduct(id, status, remark) {
  return request({
    url: `/admin/product/audit/${id}`,
    method: 'post',
    params: { status, remark }
  })
}

export function offlineProduct(id) {
  return request({
    url: `/admin/product/offline/${id}`,
    method: 'post'
  })
}

export function onlineProduct(id) {
  return request({
    url: `/admin/product/online/${id}`,
    method: 'post'
  })
}

export function createAdminProduct(data) {
  return request({
    url: '/admin/product',
    method: 'post',
    data
  })
}

export function updateAdminProduct(id, data) {
  return request({
    url: `/admin/product/${id}`,
    method: 'put',
    data
  })
}

export function deleteAdminProduct(id) {
  return request({
    url: `/admin/product/${id}`,
    method: 'delete'
  })
}

// 管理员订单管理
export function getAdminOrderList(params) {
  return request({
    url: '/admin/order/list',
    method: 'get',
    params
  })
}

export function getAdminOrderDetail(id) {
  return request({
    url: `/admin/order/detail/${id}`,
    method: 'get'
  })
}

export function adminShipOrder(id, data) {
  return request({
    url: `/admin/order/ship/${id}`,
    method: 'post',
    data
  })
}

export function adminCancelOrder(id, data) {
  return request({
    url: `/admin/order/cancel/${id}`,
    method: 'post',
    data
  })
}

export function processAdminRefund(id, data) {
  return request({
    url: `/admin/order/refund/${id}`,
    method: 'post',
    data
  })
}

export function getOrderStatusHistory(orderId) {
  return request({
    url: `/order/status/history/${orderId}`,
    method: 'get'
  })
}

export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

// 管理员资质审核
export function getAdminQualificationList(params) {
  return request({
    url: '/admin/qualification/list',
    method: 'get',
    params
  })
}

export function getAdminQualificationDetail(id) {
  return request({
    url: `/admin/qualification/${id}`,
    method: 'get'
  })
}

export function reviewQualification(qualificationId, status, reason) {
  return request({
    url: '/admin/qualification/review',
    method: 'post',
    params: { qualificationId, status, reason }
  })
}

// 管理员用户管理
export function getAdminUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

export function getAdminUserDetail(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

export function createAdminUser(data) {
  return request({
    url: '/admin/user',
    method: 'post',
    data
  })
}

export function updateAdminUser(id, data) {
  return request({
    url: `/admin/user/${id}`,
    method: 'put',
    data
  })
}

export function deleteAdminUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}

export function toggleUserStatus(id) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'post'
  })
}
