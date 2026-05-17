import request from '@/utils/request'

export function getHomeData() {
  return request({
    url: '/c/home',
    method: 'get'
  })
}

export function getPackageDetail(id) {
  return request({
    url: `/c/home/package/${id}`,
    method: 'get'
  })
}
