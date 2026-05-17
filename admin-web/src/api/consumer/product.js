import request from '@/utils/request'

export function getProductList(params) {
  return request({ url: '/c/product/list', method: 'get', params })
}

export function getProductDetail(id) {
  return request({ url: `/c/product/detail/${id}`, method: 'get' })
}

export function getCategories(parentId) {
  return request({ url: '/c/product/categories', method: 'get', params: { parentId } })
}

export function getCategoryTree() {
  return request({ url: '/c/product/category-tree', method: 'get' })
}

export function getRecommendProducts(params) {
  return request({ url: '/c/product/recommend', method: 'get', params })
}

export function getHotProducts(params) {
  return request({ url: '/c/product/hot', method: 'get', params })
}

export function getHotSearches() {
  return request({ url: '/c/product/hot-searches', method: 'get' })
}

export function getSearchSuggestions(keyword) {
  return request({ url: '/c/product/search-suggestions', method: 'get', params: { keyword } })
}

export function getTierPrices(productId) {
  return request({ url: `/product/tier-price/get/${productId}`, method: 'get' })
}

export function calculateTierPrice(data) {
  return request({ url: '/product/tier-price/calculate', method: 'post', data })
}
