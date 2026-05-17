/**
 * 根据表头与样本内容估算列最小宽度（字符级自适应）
 */
export function estimateColMinWidth(label, sample = '', { min = 72, max = 320 } = {}) {
  const labelW = (label?.length || 0) * 15 + 28
  const sampleStr = sample == null ? '' : String(sample)
  const dataW = sampleStr.length * 9 + 32
  return Math.min(max, Math.max(min, labelW, dataW))
}

/** 从表格数据中取某列最长字符串样本 */
export function maxCellSample(rows, prop) {
  if (!rows?.length || !prop) return ''
  let max = ''
  for (const row of rows) {
    const v = row[prop]
    if (v == null) continue
    const s = String(v)
    if (s.length > max.length) max = s
  }
  return max
}
