export const PRODUCT_STATUS = {
  0: { label: '已下架', tag: 'info' },
  1: { label: '已上架', tag: 'success' },
  2: { label: '待审核', tag: 'warning' }
}

export const PRESCRIPTION_TYPE = {
  OTC: { label: 'OTC（非处方）', tag: 'success' },
  PRESCRIPTION: { label: '处方药', tag: 'danger' },
  DUAL_TRACK: { label: '双轨制', tag: 'warning' }
}

export function productStatusLabel(status) {
  return PRODUCT_STATUS[status]?.label ?? '未知'
}

export function productStatusTag(status) {
  return PRODUCT_STATUS[status]?.tag ?? ''
}

export function prescriptionLabel(type) {
  return PRESCRIPTION_TYPE[type]?.label ?? type ?? '-'
}

export function prescriptionTag(type) {
  return PRESCRIPTION_TYPE[type]?.tag ?? 'info'
}

export function formatPrice(value) {
  if (value == null || value === '') return '-'
  const n = Number(value)
  return Number.isFinite(n) ? `¥${n.toFixed(2)}` : '-'
}

export function flagLabel(value) {
  return value === 1 ? '是' : '否'
}

/** 表单字段 -> Tab，用于校验失败时跳转 */
export const PRODUCT_FORM_FIELD_TAB = {
  productName: 'basic',
  prescriptionType: 'basic',
  categoryId: 'basic',
  wholesalePrice: 'price',
  stock: 'price',
  mainImage: 'images'
}

export function defaultProductForm() {
  return {
    id: null,
    productName: '',
    genericName: '',
    prescriptionType: 'OTC',
    brand: '',
    specification: '',
    dosageForm: '',
    manufacturer: '',
    approvalNumber: '',
    barcode: '',
    wholesalePrice: 0,
    marketPrice: 0,
    stock: 0,
    minOrderQuantity: 1,
    maxOrderQuantity: 999,
    weight: null,
    volume: null,
    status: 1,
    categoryId: 1,
    isHot: 0,
    isNew: 0,
    isRecommend: 0,
    sort: 0,
    indications: '',
    usage: '',
    contraindications: '',
    adverseReactions: '',
    precautions: '',
    storageCondition: '',
    validityPeriod: null,
    description: '',
    instruction: '',
    mainImage: '',
    images: '',
    detailImages: ''
  }
}

export function mapProductToForm(detail) {
  return {
    ...defaultProductForm(),
    id: detail.id,
    productName: detail.productName,
    genericName: detail.genericName,
    prescriptionType: detail.prescriptionType || 'OTC',
    brand: detail.brand,
    specification: detail.specification,
    dosageForm: detail.dosageForm,
    manufacturer: detail.manufacturer,
    approvalNumber: detail.approvalNumber,
    barcode: detail.barcode,
    wholesalePrice: detail.wholesalePrice,
    marketPrice: detail.marketPrice,
    stock: detail.stock,
    minOrderQuantity: detail.minOrderQuantity ?? 1,
    maxOrderQuantity: detail.maxOrderQuantity ?? 999,
    status: detail.status,
    categoryId: detail.categoryId ?? 1,
    isHot: detail.isHot ?? 0,
    isNew: detail.isNew ?? 0,
    isRecommend: detail.isRecommend ?? 0,
    sort: detail.sort ?? 0,
    weight: detail.weight,
    volume: detail.volume,
    indications: detail.indications,
    usage: detail.usage,
    contraindications: detail.contraindications,
    adverseReactions: detail.adverseReactions,
    precautions: detail.precautions,
    storageCondition: detail.storageCondition,
    validityPeriod: detail.validityPeriod,
    description: detail.description,
    instruction: detail.instruction,
    mainImage: detail.mainImage || '',
    images: detail.images || '',
    detailImages: detail.detailImages || ''
  }
}
