import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 发送验证码
export function sendCode(phone) {
  return request({
    url: '/auth/sendCode',
    method: 'get',
    params: { phone }
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

// 更新用户资料
export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

// 获取收货地址列表
export function getAddressList() {
  return request({
    url: '/address/list',
    method: 'get'
  })
}

// 添加收货地址
export function addAddress(data) {
  return request({
    url: '/address/add',
    method: 'post',
    data
  })
}

// 更新收货地址
export function updateAddress(data) {
  return request({
    url: '/address/update',
    method: 'post',
    data
  })
}

// 删除收货地址
export function deleteAddress(id) {
  return request({
    url: `/address/delete/${id}`,
    method: 'post'
  })
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request({
    url: `/address/setDefault/${id}`,
    method: 'post'
  })
}
