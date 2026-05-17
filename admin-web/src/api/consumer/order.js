import request from '@/utils/request'

export function getMyOrders(params) {
  return request({ url: '/c/order/list', method: 'get', params })
}

export function getOrderDetail(id) {
  return request({ url: `/c/order/${id}`, method: 'get' })
}

export function cancelOrder(id, reason) {
  return request({ url: `/c/order/cancel/${id}`, method: 'post', data: { reason } })
}

export function confirmReceive(id) {
  return request({ url: `/c/order/confirm/${id}`, method: 'post' })
}

export function createOtcOrder(data) {
  return request({ url: '/c/order/create-otc', method: 'post', data })
}

export function createPrescriptionOrder(data) {
  return request({ url: '/c/order/create-prescription', method: 'post', data })
}

export function createPackageOrder(data) {
  return request({ url: '/c/order/create-package', method: 'post', data })
}

export function getOrderLogistics(id) {
  return request({ url: `/c/order/${id}/logistics`, method: 'get' })
}

export function applyOrderRefund(orderId, reason) {
  return request({ url: '/order/status/refund/apply', method: 'post', data: { orderId, reason } })
}
