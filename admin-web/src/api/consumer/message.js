import request from '@/utils/request'

export function getMessageList(params) {
  return request({ url: '/message/list', method: 'get', params })
}

export function markMessageRead(id) {
  return request({ url: `/message/read/${id}`, method: 'post' })
}

export function markAllMessagesRead() {
  return request({ url: '/message/readAll', method: 'post' })
}

export function getUnreadCount() {
  return request({ url: '/message/unreadCount', method: 'get' })
}
