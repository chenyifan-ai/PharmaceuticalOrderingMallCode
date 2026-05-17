/** 发票类型 */
export function invoiceTypeLabel(type) {
  const map = { 1: '个人', 2: '企业' }
  return map[type] ?? '—'
}

/** 发票流程状态（invoice 表 status） */
export function invoiceStatusLabel(status) {
  const map = { 0: '待开票', 1: '已开票', 2: '已寄送', 3: '已作废' }
  return map[status] ?? '—'
}

export function invoiceStatusTag(status) {
  const map = { 0: 'warning', 1: 'success', 2: '', 3: 'info' }
  return map[status] ?? 'info'
}

/** 订单表 invoiceStatus：0 未开票 1 已开票 */
export function orderInvoiceStatusLabel(status) {
  if (status === 1) return '已开票'
  if (status === 0) return '未开票'
  return '—'
}

export function orderInvoiceStatusTag(status) {
  return status === 1 ? 'success' : 'info'
}

/** 订单是否有关联发票信息（记录或抬头） */
export function hasInvoiceInfo(order) {
  if (!order) return false
  if (order.invoice) return true
  return !!(order.invoiceTitle || order.invoiceTaxNo)
}

/** 列表/卡片用简短摘要 */
export function invoiceSummary(order) {
  if (!order) return '—'
  const inv = order.invoice
  if (inv?.invoiceNo) {
    return `${inv.invoiceTitle || order.invoiceTitle || '—'} · ${invoiceStatusLabel(inv.status)}`
  }
  if (inv?.invoiceTitle) {
    return `${inv.invoiceTitle} · ${invoiceStatusLabel(inv.status)}`
  }
  if (order.invoiceTitle) {
    return `${order.invoiceTitle}${order.invoiceTaxNo ? '（' + order.invoiceTaxNo + '）' : ''}`
  }
  return '未申请发票'
}
