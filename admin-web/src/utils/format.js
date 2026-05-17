/**
 * 统一日期时间格式化
 */

function pad(n) {
  return String(n).padStart(2, '0')
}

function toDate(input) {
  if (input == null || input === '') return null
  if (input instanceof Date) return Number.isNaN(input.getTime()) ? null : input
  if (typeof input === 'number') {
    const d = new Date(input)
    return Number.isNaN(d.getTime()) ? null : d
  }
  const str = String(input).trim()
  if (!str) return null
  // ISO / 带 T 的 LocalDateTime
  const normalized = str.includes('T') ? str.replace('T', ' ').replace(/\.\d+/, '') : str
  const d = new Date(normalized.replace(/-/g, '/'))
  return Number.isNaN(d.getTime()) ? null : d
}

/** yyyy-MM-dd HH:mm:ss */
export function formatDateTime(input) {
  const d = toDate(input)
  if (!d) return '-'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** yyyy-MM-dd */
export function formatDate(input) {
  const d = toDate(input)
  if (!d) return '-'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** yyyy-MM-dd HH:mm */
export function formatDateTimeShort(input) {
  const d = toDate(input)
  if (!d) return '-'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
