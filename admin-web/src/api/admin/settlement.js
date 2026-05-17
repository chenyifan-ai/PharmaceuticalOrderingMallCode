import request from '@/utils/request'

export function getSettlementList(params) {
  return request({
    url: '/admin/settlement/list',
    method: 'get',
    params
  })
}

export function generateSettlement(data) {
  return request({
    url: '/admin/settlement/generate',
    method: 'post',
    data
  })
}

export function confirmSettlement(id) {
  return request({
    url: `/admin/settlement/confirm/${id}`,
    method: 'post'
  })
}
