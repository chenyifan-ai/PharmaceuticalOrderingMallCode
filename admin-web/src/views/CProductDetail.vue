<template>
  <div class="product-detail">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: cPath('products') }">商品列表</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.productName }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-row :gutter="24" class="detail-section">
      <el-col :span="10">
        <ProductImageGallery :product="product" :alt="product.productName" />
      </el-col>
      <el-col :span="14">
        <h2 class="product-name">{{ product.productName }}</h2>
        <p class="generic-name" v-if="product.genericName">通用名：{{ product.genericName }}</p>
        <el-alert
          v-if="seckillPrice"
          type="error"
          :closable="false"
          show-icon
          class="seckill-alert"
          title="秒杀专享价"
          :description="`限时 ¥${seckillPrice}，活动结束恢复批发价`"
        />
        <div class="price-section">
          <span class="price">¥{{ displayPrice }}</span>
          <span v-if="seckillPrice" class="market-price">批发价 ¥{{ product.wholesalePrice }}</span>
          <span v-else-if="product.marketPrice" class="market-price">市场价 ¥{{ product.marketPrice }}</span>
        </div>
        <div class="info-items">
          <div class="info-item">
            <span class="label">规格：</span>
            <span>{{ product.specification }}</span>
          </div>
          <div class="info-item">
            <span class="label">剂型：</span>
            <span>{{ product.dosageForm }}</span>
          </div>
          <div class="info-item">
            <span class="label">生产厂家：</span>
            <span>{{ product.manufacturer }}</span>
          </div>
          <div class="info-item">
            <span class="label">批准文号：</span>
            <span>{{ product.approvalNumber }}</span>
          </div>
          <div class="info-item">
            <span class="label">处方类型：</span>
            <el-tag :type="prescriptionTypeTag">{{ prescriptionTypeText }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">库存：</span>
            <span :class="stockClass">{{ stockText }}</span>
          </div>
        </div>
        <div class="quantity-section">
          <span class="label">数量：</span>
          <el-input-number v-model="quantity" :min="product.minOrderQuantity || 1" :max="product.maxOrderQuantity || 999" />
          <span v-if="product.minOrderQuantity" class="min-order">最小起订量：{{ product.minOrderQuantity }}</span>
        </div>
        <div class="tier-price" v-if="tierPrices.length > 0">
          <h4>阶梯价格</h4>
          <div v-for="tp in tierPrices" :key="tp.id" class="tier-item">
            购{{ tp.minQuantity }}{{ tp.maxQuantity ? '-' + tp.maxQuantity : '+' }}件：¥{{ tp.price }}
          </div>
        </div>
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="addToCart">加入订货清单</el-button>
          <el-button type="danger" size="large" @click="buyNow">立即订购</el-button>
        </div>
      </el-col>
    </el-row>

    <el-card v-if="detailImageList.length" class="detail-images-section">
      <template #header><span>商品详情图</span></template>
      <div class="detail-images">
        <ProductImage
          v-for="(url, i) in detailImageList"
          :key="i"
          :src="url"
          :alt="product.productName"
          img-class="detail-img"
        />
      </div>
    </el-card>

    <el-card class="instruction-section" v-if="product.instruction">
      <template #header>
        <span>药品说明书</span>
      </template>
      <div v-html="product.instruction"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, getTierPrices } from '@/api/consumer/product'
import { addToCart as addToCartApi } from '@/api/consumer/cart'
import { cPath } from '@/utils/consumer-path'
import ProductImageGallery from '@/components/ProductImageGallery.vue'
import ProductImage from '@/components/ProductImage.vue'
import { parseProductImageList } from '@/utils/productImage'

const route = useRoute()
const router = useRouter()
const product = ref({})
const quantity = ref(1)
const tierPrices = ref([])
function parseSeckillId(raw) {
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
}

const seckillId = ref(parseSeckillId(route.query.seckillId))
const seckillPrice = ref(
  route.query.seckillPrice != null && route.query.seckillPrice !== ''
    ? Number(route.query.seckillPrice)
    : null
)

const displayPrice = computed(() =>
  seckillPrice.value != null ? seckillPrice.value : product.value.wholesalePrice
)

const detailImageList = computed(() => parseProductImageList(product.value?.detailImages))

const prescriptionTypeTag = computed(() => {
  const map = { OTC: 'success', PRESCRIPTION: 'danger', DUAL_TRACK: 'warning' }
  return map[product.value.prescriptionType] || 'info'
})

const prescriptionTypeText = computed(() => {
  const map = { OTC: '非处方药(OTC)', PRESCRIPTION: '处方药', DUAL_TRACK: '双轨制' }
  return map[product.value.prescriptionType] || '未知'
})

const stockText = computed(() => {
  if (!product.value.stock || product.value.stock <= 0) return '无货'
  if (product.value.stock < 10) return '紧张（仅剩' + product.value.stock + '件）'
  return '充足'
})

const stockClass = computed(() => {
  if (!product.value.stock || product.value.stock <= 0) return 'stock-none'
  if (product.value.stock < 10) return 'stock-low'
  return 'stock-ok'
})

const fetchProductDetail = async () => {
  try {
    product.value = await getProductDetail(route.params.id)
  } catch (e) {
    console.error('获取商品详情失败', e)
  }
}

const fetchTierPrices = async () => {
  try {
    const json = await getTierPrices(route.params.id)
    if (json) {
      tierPrices.value = typeof json === 'string' ? JSON.parse(json) : json
    }
  } catch (e) {
    console.error('获取阶梯价格失败', e)
  }
}

const addToCart = async () => {
  try {
    await addToCartApi(route.params.id, quantity.value, seckillId.value)
    ElMessage.success(seckillId.value ? '秒杀商品已加入订货清单' : '已加入订货清单')
    router.push(cPath('cart'))
  } catch (e) {
    console.error('加入订货清单失败', e)
  }
}

const buyNow = () => {
  if (product.value.prescriptionType === 'PRESCRIPTION') {
    router.push({ path: cPath('checkout'), query: { productId: route.params.id, quantity: quantity.value, type: 'prescription' } })
  } else {
    const query = { productId: route.params.id, quantity: quantity.value, type: 'otc' }
    if (seckillId.value) {
      query.seckillId = seckillId.value
    }
    router.push({ path: cPath('checkout'), query })
  }
}

onMounted(() => {
  fetchProductDetail()
  fetchTierPrices()
})
</script>

<style scoped>
.product-detail { max-width: 1200px; margin: 0 auto; padding: 20px; }
.detail-section { margin-top: 20px; }
.detail-images-section { margin-top: 24px; }
.detail-images { display: flex; flex-direction: column; gap: 12px; }
.detail-images :deep(.detail-img) {
  width: 100%;
  max-height: 480px;
  object-fit: contain;
  border-radius: 8px;
  background: #f8fafc;
}
.product-name { font-size: 24px; color: #333; margin: 0 0 10px; }
.generic-name { color: #999; font-size: 14px; margin-bottom: 15px; }
.seckill-alert { margin-bottom: 12px; }
.price-section { margin: 15px 0; padding: 15px 0; border-top: 1px solid #eee; border-bottom: 1px solid #eee; }
.price { font-size: 28px; color: #e4393c; font-weight: bold; }
.market-price { font-size: 14px; color: #999; text-decoration: line-through; margin-left: 10px; }
.info-items { margin: 15px 0; }
.info-item { line-height: 32px; font-size: 14px; }
.info-item .label { color: #999; }
.stock-ok { color: #67c23a; }
.stock-low { color: #e6a23c; }
.stock-none { color: #f56c6c; }
.quantity-section { margin: 20px 0; display: flex; align-items: center; gap: 10px; }
.quantity-section .label { color: #999; }
.min-order { font-size: 12px; color: #999; }
.tier-price { background: #f5f7fa; padding: 15px; border-radius: 4px; margin: 15px 0; }
.tier-price h4 { margin: 0 0 10px; font-size: 14px; color: #e4393c; }
.tier-item { font-size: 13px; color: #666; line-height: 24px; }
.action-buttons { margin-top: 20px; display: flex; gap: 15px; }
.instruction-section { margin-top: 30px; }
</style>
