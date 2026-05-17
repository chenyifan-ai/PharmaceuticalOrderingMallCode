const PLACEHOLDER =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">' +
      '<rect fill="#f0f4f8" width="400" height="400"/>' +
      '<text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#94a3b8" font-size="18" font-family="sans-serif">暂无图片</text>' +
      '</svg>'
  )

export function resolveProductImageUrl(url) {
  if (!url || typeof url !== 'string') {
    return PLACEHOLDER
  }
  const trimmed = url.trim()
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://') || trimmed.startsWith('data:')) {
    return trimmed
  }
  if (trimmed.startsWith('/files')) {
    return trimmed
  }
  return trimmed.startsWith('/') ? trimmed : `/${trimmed}`
}

export function parseProductImageList(value) {
  if (!value) return []
  if (Array.isArray(value)) {
    return value.filter(Boolean)
  }
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.filter(Boolean) : []
  } catch {
    return []
  }
}

export function stringifyProductImageList(list) {
  const arr = (list || []).filter(Boolean)
  return arr.length ? JSON.stringify(arr) : ''
}

export function getProductGallery(product) {
  if (!product) return [PLACEHOLDER]
  const main = product.mainImage
  const carousel = parseProductImageList(product.images)
  const detail = parseProductImageList(product.detailImages)
  const merged = []
  const seen = new Set()
  for (const url of [main, ...carousel, ...detail]) {
    if (!url || seen.has(url)) continue
    seen.add(url)
    merged.push(url)
  }
  return merged.length ? merged : [null]
}

export function productImagePlaceholder() {
  return PLACEHOLDER
}
