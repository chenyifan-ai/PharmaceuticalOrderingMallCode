export const STOCK_STATUS = {
  NORMAL: { label: '正常', tag: 'success' },
  LOW: { label: '库存偏低', tag: 'warning' },
  OUT: { label: '缺货', tag: 'danger' }
}

export function stockStatusLabel(status) {
  return STOCK_STATUS[status]?.label ?? '—'
}

export function stockStatusTag(status) {
  return STOCK_STATUS[status]?.tag ?? 'info'
}

export const CHANGE_TYPES = [
  { value: 1, label: '进库' },
  { value: 2, label: '出库' },
  { value: 3, label: '盘点设置' }
]

export function changeTypeLabel(type) {
  return CHANGE_TYPES.find((t) => t.value === type)?.label ?? '—'
}
