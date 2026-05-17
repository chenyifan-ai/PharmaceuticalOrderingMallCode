import request from '@/utils/request'

export function getUserInfo() {
  return request({ url: '/user/info', method: 'get' })
}

export function updateUserInfo(data) {
  return request({ url: '/user/info', method: 'put', data })
}

export function updateUserProfile(data) {
  return request({ url: '/user/profile', method: 'put', data })
}

export function changePassword(data) {
  return request({ url: '/user/changePassword', method: 'post', data })
}
