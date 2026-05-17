import request from '@/utils/request'

export function getPrescriptionList(params) {
  return request({ url: '/prescription/list', method: 'get', params })
}

export function getPrescriptionDetail(id) {
  return request({ url: `/prescription/detail/${id}`, method: 'get' })
}

export function uploadPrescription(data) {
  return request({ url: '/prescription/upload', method: 'post', data })
}
