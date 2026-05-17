import request from '@/utils/request'

// B端-供应商订单管理
export function getMerchantOrders(params) {
  return request({
    url: '/merchant/order/list',
    method: 'get',
    params
  })
}

export function shipOrder(id, data) {
  return request({
    url: `/merchant/order/ship/${id}`,
    method: 'post',
    data
  })
}

export function processMerchantRefund(id, data) {
  return request({
    url: `/merchant/order/refund/${id}`,
    method: 'post',
    data
  })
}
