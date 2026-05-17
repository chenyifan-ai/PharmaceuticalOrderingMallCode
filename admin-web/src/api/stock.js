import request from '@/utils/request'

function stockBase() {
  try {
    const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return user.userType === 4 ? '/merchant/stock' : '/admin/stock'
  } catch {
    return '/admin/stock'
  }
}

export function getStockSummary() {
  return request({
    url: `${stockBase()}/summary`,
    method: 'get'
  })
}

export function getStockList(params) {
  return request({
    url: `${stockBase()}/list`,
    method: 'get',
    params
  })
}

export function adjustStock(data) {
  return request({
    url: `${stockBase()}/adjust`,
    method: 'post',
    data
  })
}

/** 商品进库 */
export function inboundStock(data) {
  return request({
    url: `${stockBase()}/inbound`,
    method: 'post',
    data
  })
}

export function updateStockWarning(data) {
  return request({
    url: `${stockBase()}/warning`,
    method: 'post',
    data
  })
}

export function getStockLogs(productId, params) {
  return request({
    url: `${stockBase()}/logs/${productId}`,
    method: 'get',
    params
  })
}

export function getStockBatches(productId) {
  return request({
    url: `${stockBase()}/batches/${productId}`,
    method: 'get'
  })
}
