import request from '@/utils/request'

export function getAddressList() {
  return request({
    url: '/user-address/list',
    method: 'get'
  })
}

export function getAddressDetail(id) {
  return request({
    url: `/user-address/${id}`,
    method: 'get'
  })
}

export function addAddress(data) {
  return request({
    url: '/user-address/add',
    method: 'post',
    data
  })
}

export function updateAddress(data) {
  return request({
    url: '/user-address/update',
    method: 'put',
    data
  })
}

export function deleteAddress(id) {
  return request({
    url: `/user-address/delete/${id}`,
    method: 'delete'
  })
}

export function setDefaultAddress(id) {
  return request({
    url: `/user-address/set-default/${id}`,
    method: 'post'
  })
}
