import request from '@/utils/request'

export function getCartList() {
  return request({ url: '/cart/list', method: 'get' })
}

export function addToCart(productId, quantity = 1, seckillId = null) {
  const data = {
    productId: Number(productId),
    quantity: Number(quantity) || 1
  }
  const sid = Number(seckillId)
  if (Number.isFinite(sid) && sid > 0) {
    data.seckillId = sid
  }
  return request({ url: '/cart/add', method: 'post', data })
}

export function updateCartQuantity(id, quantity) {
  return request({ url: '/cart/update', method: 'put', data: { id, quantity } })
}

export function removeFromCart(id) {
  return request({ url: `/cart/${id}`, method: 'delete' })
}

export function clearCart() {
  return request({ url: '/cart/clear', method: 'delete' })
}
