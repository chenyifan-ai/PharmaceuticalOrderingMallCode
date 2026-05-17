import request from '@/utils/request'

// 获取商家列表（管理员）
export function getMerchantList(params) {
  return request({
    url: '/admin/merchant/list',
    method: 'get',
    params
  })
}

// 审核商家（管理员）
export function auditMerchant(id, data) {
  return request({
    url: `/admin/merchant/audit/${id}`,
    method: 'post',
    data
  })
}

// 获取商家详情（管理员）
export function getMerchantDetail(id) {
  return request({
    url: `/admin/merchant/${id}`,
    method: 'get'
  })
}

export function createMerchant(data) {
  return request({
    url: '/admin/merchant',
    method: 'post',
    data
  })
}

export function updateMerchant(id, data) {
  return request({
    url: `/admin/merchant/${id}`,
    method: 'put',
    data
  })
}

export function deleteMerchant(id) {
  return request({
    url: `/admin/merchant/${id}`,
    method: 'delete'
  })
}

// 获取我的商家信息（B端商家）
export function getMyMerchantInfo() {
  return request({
    url: '/merchant/info/me',
    method: 'get'
  })
}

// 更新商家信息（B端商家）
export function updateMerchantInfo(data) {
  return request({
    url: '/merchant/info/update',
    method: 'put',
    data
  })
}
