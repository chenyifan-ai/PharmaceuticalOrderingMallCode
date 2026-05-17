import request from '@/utils/request'

export function getAvailableCoupons() {
  return request({ url: '/c/coupon/available', method: 'get' })
}

export function receiveCoupon(couponId) {
  return request({ url: `/c/coupon/receive/${couponId}`, method: 'post' })
}

export function getMyCoupons(params) {
  return request({ url: '/c/coupon/my', method: 'get', params })
}
