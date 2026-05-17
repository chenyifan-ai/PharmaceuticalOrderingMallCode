import request from '@/utils/request'

export function getAddressList() {
  return request({ url: '/address/list', method: 'get' })
}

export function getDefaultAddress() {
  return request({ url: '/address/default', method: 'get' })
}

export function getRegionOptions() {
  return request({ url: '/address/regions', method: 'get' })
}

export function addAddress(data) {
  return request({ url: '/address/add', method: 'post', data: toApiAddress(data) })
}

export function updateAddress(data) {
  return request({ url: '/address/update', method: 'put', data: toApiAddress(data) })
}

export function deleteAddress(id) {
  return request({ url: `/address/delete/${id}`, method: 'delete' })
}

export function setDefaultAddress(id) {
  return request({ url: `/address/setDefault/${id}`, method: 'post' })
}

/** 智能解析粘贴的收货地址 */
export function parseAddressText(text) {
  return request({ url: '/address/parse', method: 'post', data: { text } })
}

/** 表单字段 -> 后端实体字段 */
export function toApiAddress(form) {
  return {
    id: form.id,
    receiverName: form.receiverName ?? form.name,
    receiverPhone: form.receiverPhone ?? form.phone,
    province: form.province,
    city: form.city,
    district: form.district,
    detailAddress: form.detailAddress ?? form.detail,
    isDefault: form.isDefault === true || form.isDefault === 1 ? 1 : 0
  }
}

/** 后端 -> 展示用 */
export function fromApiAddress(item) {
  return {
    ...item,
    name: item.receiverName,
    phone: item.receiverPhone,
    detail: item.detailAddress,
    isDefault: item.isDefault === 1
  }
}
