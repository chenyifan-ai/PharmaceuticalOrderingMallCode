/** 订单状态 */
export const ORDER_STATUS = {
  0: { label: '待付款', tag: 'warning' },
  1: { label: '待审核', tag: 'info' },
  2: { label: '待发货', tag: 'primary' },
  3: { label: '已发货', tag: '' },
  4: { label: '已完成', tag: 'success' },
  5: { label: '已取消', tag: 'info' }
}

export function orderStatusLabel(status) {
  return ORDER_STATUS[status]?.label ?? '未知'
}

export function orderStatusTag(status) {
  return ORDER_STATUS[status]?.tag ?? 'info'
}

/** 处方审核状态 */
export const RX_STATUS = {
  0: { label: '待审核', tag: 'warning' },
  1: { label: '已通过', tag: 'success' },
  2: { label: '已拒绝', tag: 'danger' },
  3: { label: '已过期', tag: 'info' }
}

export function rxStatusLabel(status) {
  return RX_STATUS[status]?.label ?? '未知'
}

export function rxStatusTag(status) {
  return RX_STATUS[status]?.tag ?? 'info'
}

export const RX_TYPE = {
  1: '用户上传',
  2: '电子处方',
  3: '在线问诊'
}

/** 登录后默认首页（管理端统一入口） */
export function getHomeByUserType(userType) {
  if (userType === 5) return '/dashboard'
  if (userType === 4) return '/merchant-products'
  return '/c/home'
}

export function isConsumerUser(userType) {
  return userType === 1 || userType === 2 || userType === 3
}

export function isAdminUser(userType) {
  return userType === 5
}

export function isMerchantUser(userType) {
  return userType === 4
}
