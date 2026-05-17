import request from '@/utils/request'

export function getMyQualification() {
  return request({ url: '/qualification/my', method: 'get' })
}

export function submitQualification(data) {
  return request({ url: '/qualification/submit', method: 'post', data })
}
