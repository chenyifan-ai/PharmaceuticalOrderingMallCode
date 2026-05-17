import request from '@/utils/request'

export function getPendingVouchers(params) {
  return request({ url: '/admin/payment/voucher/pending', method: 'get', params })
}

export function reviewVoucher(id, data) {
  return request({ url: `/admin/payment/voucher/review/${id}`, method: 'post', data })
}
