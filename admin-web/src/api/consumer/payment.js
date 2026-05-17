import request from '@/utils/request'

export function createPayment(data) {
  return request({ url: '/payment/create', method: 'post', data })
}

export function confirmPayment(data) {
  return request({ url: '/payment/confirm', method: 'post', data })
}

export function getPaymentStatus(id) {
  return request({ url: `/payment/status/${id}`, method: 'get' })
}

export function submitPaymentVoucher(data) {
  return request({ url: '/payment/voucher', method: 'post', data })
}
