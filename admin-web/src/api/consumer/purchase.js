import request from '@/utils/request'

export function getPurchaseStats() {
  return request({ url: '/c/purchase/stats', method: 'get' })
}

export function exportPurchaseStats() {
  return request({
    url: '/c/purchase/export',
    method: 'get',
    responseType: 'blob'
  })
}
