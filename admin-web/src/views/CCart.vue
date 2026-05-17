<template>
  <div class="consumer-page cart-page">
    <div class="consumer-container" v-loading="loading">
      <header class="consumer-page-header cart-header">
        <div>
          <h2>订货清单</h2>
          <p class="subtitle">已选 {{ selectedCount }} 件 · 共 {{ cartItems.length }} 种商品</p>
        </div>
        <el-button v-if="cartItems.length" link type="danger" @click="clearInvalid" :disabled="!hasUnchecked">
          清空未选
        </el-button>
      </header>

      <div v-if="cartItems.length > 0" class="cart-panel consumer-card">
        <div
          v-for="item in cartItems"
          :key="item.id"
          class="consumer-cart-item"
          :class="{ selected: item.checked }"
        >
          <el-checkbox v-model="item.checked" @change="updateTotal" />
          <div class="thumb" @click="goProduct(item.productId)">
            <ProductImage :src="item.productImage" :alt="item.productName" img-class="cart-thumb" />
          </div>
          <div class="info">
            <h3 class="name" @click="goProduct(item.productId)">
              {{ item.productName }}
              <el-tag v-if="item.seckillId > 0" type="danger" size="small" effect="plain" class="seckill-tag">秒杀</el-tag>
            </h3>
            <p class="spec">{{ item.specification }}</p>
            <p class="manufacturer" v-if="item.manufacturer">{{ item.manufacturer }}</p>
            <div class="row-foot">
              <span class="price">¥{{ formatPrice(item.price || item.productPrice) }}</span>
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="99"
                size="small"
                @change="updateQuantity(item)"
              />
            </div>
          </div>
          <el-button type="danger" link class="btn-remove" @click="removeItem(item.id)">删除</el-button>
        </div>

        <div class="consumer-checkout-bar">
          <div class="bar-left">
            <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
            <span class="total-label">
              合计 <strong class="total-amount">¥{{ totalPrice.toFixed(2) }}</strong>
            </span>
          </div>
          <el-button
            type="primary"
            size="large"
            round
            :disabled="selectedCount === 0"
            @click="handleCheckout"
          >
            去结算 ({{ selectedCount }})
          </el-button>
        </div>
      </div>

      <div v-else class="consumer-card consumer-empty-wrap empty-cart">
        <el-empty description="订货清单是空的，去挑选药品吧">
          <el-button type="primary" size="large" round @click="goShopping">去选药</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartList, updateCartQuantity, removeFromCart } from '@/api/consumer/cart'
import { cPath } from '@/utils/consumer-path'
import ProductImage from '@/components/ProductImage.vue'

const router = useRouter()
const refreshCounts = inject('refreshConsumerCounts', null)
const cartItems = ref([])
const selectAll = ref(false)
const loading = ref(false)

const selectedCount = computed(() => cartItems.value.filter((item) => item.checked).length)
const hasUnchecked = computed(() => cartItems.value.some((item) => !item.checked))
const totalPrice = computed(() =>
  cartItems.value
    .filter((item) => item.checked)
    .reduce((sum, item) => sum + Number(item.price || item.productPrice || 0) * item.quantity, 0)
)
const selectedItemIds = computed(() =>
  cartItems.value.filter((item) => item.checked).map((item) => item.id)
)

onMounted(fetchCartItems)

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

async function fetchCartItems() {
  loading.value = true
  try {
    const list = await getCartList()
    cartItems.value = (list || []).map((item) => ({ ...item, checked: true }))
    updateTotal()
  } catch {
    ElMessage.error('获取购物车失败')
  } finally {
    loading.value = false
  }
}

async function updateQuantity(item) {
  try {
    await updateCartQuantity(item.id, item.quantity)
  } catch {
    ElMessage.error('更新失败')
    fetchCartItems()
  }
}

async function removeItem(id) {
  try {
    await ElMessageBox.confirm('确定移出该商品？', '提示', { type: 'warning' })
    await removeFromCart(id)
    cartItems.value = cartItems.value.filter((item) => item.id !== id)
    ElMessage.success('已删除')
    refreshCounts?.()
    updateTotal()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function clearInvalid() {
  cartItems.value = cartItems.value.filter((item) => item.checked)
  updateTotal()
}

function handleSelectAll(val) {
  cartItems.value.forEach((item) => {
    item.checked = val
  })
  updateTotal()
}

function updateTotal() {
  selectAll.value =
    cartItems.value.length > 0 && cartItems.value.every((item) => item.checked)
}

function handleCheckout() {
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  router.push({ path: cPath('checkout'), query: { items: selectedItemIds.value.join(',') } })
}

function goShopping() {
  router.push(cPath('products'))
}

function goProduct(id) {
  if (id) router.push(cPath(`product/${id}`))
}
</script>

<style scoped>
.cart-page {
  padding-bottom: 40px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.cart-panel {
  padding: 8px 24px 0;
  overflow: hidden;
}

.consumer-cart-item.selected {
  background: #f8fafc;
}

.consumer-cart-item .name {
  cursor: pointer;
}

.consumer-cart-item .name:hover {
  color: var(--c-primary, #0d6e9f);
}

.manufacturer {
  font-size: 12px;
  color: var(--c-text-secondary);
  margin: 4px 0 0;
}

.row-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.price {
  color: var(--c-accent, #dc2626);
  font-size: 20px;
  font-weight: 700;
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.total-label {
  font-size: 14px;
  color: var(--c-text-secondary);
}

.total-amount {
  color: var(--c-accent, #dc2626);
  font-size: 22px;
  margin-left: 4px;
}

.empty-cart {
  padding: 48px 24px;
}

.btn-remove {
  flex-shrink: 0;
}
</style>
