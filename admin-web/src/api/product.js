import request from '@/utils/request'

// B端-供应商商品管理
export function getMerchantProducts(params) {
  return request({
    url: '/merchant/product/list',
    method: 'get',
    params
  })
}

export function publishProduct(data) {
  return request({
    url: '/merchant/product/publish',
    method: 'post',
    data
  })
}

export function updateProduct(data) {
  return request({
    url: '/merchant/product/update',
    method: 'put',
    data
  })
}

export function offlineProduct(id) {
  return request({
    url: `/merchant/product/offline/${id}`,
    method: 'post'
  })
}
