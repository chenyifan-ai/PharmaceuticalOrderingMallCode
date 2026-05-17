<template>
  <div class="consumer-page package-detail" v-loading="loading">
    <div class="consumer-container" v-if="pkg">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: cPath('products') }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ pkg.packageName }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="hero">
        <img :src="resolveProductImageUrl(pkg.bannerImage)" :alt="pkg.packageName" class="banner" />
        <div class="info">
          <h1>{{ pkg.packageName }}</h1>
          <p class="sub">{{ pkg.subtitle }}</p>
          <div class="price-box">
            <span class="price">¥{{ pkg.packagePrice }}</span>
            <span class="orig">¥{{ pkg.originalPrice }}</span>
            <el-tag v-if="pkg.discountPercent" type="danger" effect="dark">立省{{ pkg.discountPercent }}%</el-tag>
          </div>
          <el-button type="primary" size="large" @click="buyPackage">立即购买套餐</el-button>
          <el-button size="large" @click="addPackageToCart">加入订货清单</el-button>
        </div>
      </div>

      <el-card class="items-card">
        <template #header>套餐包含</template>
        <div v-for="it in pkg.items" :key="it.productId" class="item-row">
          <ProductImage :src="it.image" :alt="it.productName" img-class="item-thumb" />
          <div class="item-info">
            <h4>{{ it.productName }}</h4>
            <p>{{ it.specification }} × {{ it.quantity }}</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPackageDetail } from '@/api/consumer/home'
import { addToCart } from '@/api/consumer/cart'
import { cPath } from '@/utils/consumer-path'
import ProductImage from '@/components/ProductImage.vue'
import { resolveProductImageUrl } from '@/utils/productImage'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const pkg = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    pkg.value = await getPackageDetail(route.params.id)
  } catch {
    ElMessage.error('套餐不存在')
    router.replace(cPath('home'))
  } finally {
    loading.value = false
  }
})

function buyPackage() {
  if (!pkg.value?.id) return
  router.push({
    path: cPath('checkout'),
    query: { type: 'package', packageId: pkg.value.id }
  })
}

async function addPackageToCart() {
  if (!pkg.value?.items?.length) return
  try {
    for (const it of pkg.value.items) {
      await addToCart(it.productId, it.quantity || 1)
    }
    ElMessage.success('套餐商品已加入订货清单（按单品批发价结算）')
    router.push(cPath('cart'))
  } catch (e) {
    ElMessage.error(e.message || '加购失败')
  }
}
</script>

<style scoped>
.package-detail {
  padding: 24px 0 48px;
}

.hero {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
  margin: 20px 0 24px;
}

.banner {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 12px;
}

.info h1 {
  margin: 0 0 8px;
  font-size: 26px;
}

.sub {
  color: #64748b;
  margin-bottom: 16px;
}

.price-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.price {
  font-size: 32px;
  color: #dc2626;
  font-weight: 700;
}

.orig {
  text-decoration: line-through;
  color: #94a3b8;
}

.item-row {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.item-row :deep(.item-thumb) {
  width: 64px;
  height: 64px;
  object-fit: contain;
  border-radius: 8px;
  background: #f8fafc;
}

.item-info h4 {
  margin: 0 0 4px;
}

.item-info p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
