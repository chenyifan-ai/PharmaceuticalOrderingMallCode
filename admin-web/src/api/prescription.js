import request from '@/utils/request'

export function getMyPrescriptions(params) {
  return request({
    url: '/prescription/my',
    method: 'get',
    params
  })
}

export function getPrescriptionDetail(id) {
  return request({
    url: `/prescription/${id}`,
    method: 'get'
  })
}

export function uploadPrescription(data) {
  return request({
    url: '/prescription/upload',
    method: 'post',
    data
  })
}

export function getPendingPrescriptions(params) {
  return request({
    url: '/pharmacist/prescriptions/pending',
    method: 'get',
    params
  })
}

export function auditPrescription(data) {
  return request({
    url: '/pharmacist/prescription/audit',
    method: 'post',
    data
  })
}

export function getPrescriptionHistory(params) {
  return request({
    url: '/pharmacist/prescriptions/history',
    method: 'get',
    params
  })
}
