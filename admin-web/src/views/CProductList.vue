<template>
  <div class="consumer-page products-page">
    <section class="consumer-hero consumer-hero-compact">
      <div class="consumer-container">
        <div class="hero-row">
          <div class="hero-text">
            <h1>药品目录</h1>
            <p>浏览全部药品 · 支持分类筛选与搜索</p>
          </div>
          <div class="search-row">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索药品名称、厂家..."
              :prefix-icon="Search"
              size="large"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" size="large" :icon="Search" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </div>
    </section>

    <section class="product-section">
      <div class="consumer-tabs-bar">
        <div class="consumer-container">
          <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="OTC药品" name="OTC" />
            <el-tab-pane label="处方药" name="PRESCRIPTION" />
            <el-tab-pane label="医疗器械" name="3" />
            <el-tab-pane label="保健品" name="4" />
          </el-tabs>
        </div>
      </div>

      <div class="consumer-container consumer-product-grid" v-loading="loading">
        <div class="grid-title">
          <h2>全部商品</h2>
          <span class="count" v-if="total > 0">共 {{ total }} 件</span>
        </div>

        <el-row :gutter="16">
          <el-col
            v-for="product in productList"
            :key="product.id"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
          >
            <article class="consumer-product-card" @click="goToDetail(product.id)">
              <div class="thumb">
                <ProductImage :src="product.mainImage" :alt="product.productName" img-class="product-thumb" />
                <el-tag
                  v-if="product.prescriptionType === 'OTC'"
                  class="tag"
                  type="success"
                  size="small"
                  effect="dark"
                >OTC</el-tag>
                <el-tag
                  v-else-if="product.prescriptionType === 'PRESCRIPTION'"
                  class="tag"
                  type="danger"
                  size="small"
                  effect="dark"
                >处方药</el-tag>
              </div>
              <div class="body">
                <h3 class="name">{{ product.productName }}</h3>
                <p class="meta">{{ product.specification }}</p>
                <p class="meta ellipsis">{{ product.manufacturer }}</p>
                <div class="foot">
                  <span class="price"><small>¥</small>{{ product.wholesalePrice }}</span>
                  <el-button type="primary" circle :icon="Plus" @click.stop="addToCart(product)" />
                </div>
              </div>
            </article>
          </el-col>
        </el-row>

        <div v-if="productList.length === 0 && !loading" class="consumer-empty-wrap">
          <el-empty description="暂无商品，试试其他关键词" />
        </div>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          class="consumer-pagination"
          @current-change="fetchData"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getProductList } from '@/api/consumer/product'
import ProductImage from '@/components/ProductImage.vue'
import { addToCart as addToCartApi } from '@/api/consumer/cart'
import { cPath } from '@/utils/consumer-path'

const router = useRouter()
const refreshCounts = inject('refreshConsumerCounts', null)

const loading = ref(false)
const productList = ref([])
const searchKeyword = ref('')
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    }
    if (activeCategory.value !== 'all') {
      const cat = activeCategory.value
      if (cat === 'OTC' || cat === 'PRESCRIPTION') {
        params.prescriptionType = cat
      } else {
        params.categoryId = Number(cat)
      }
    }
    const res = await getProductList(params)
    productList.value = res?.list ?? []
    total.value = res?.total ?? 0
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleCategoryChange() {
  currentPage.value = 1
  fetchData()
}

async function addToCart(product) {
  try {
    await addToCartApi(product.id, 1)
    ElMessage.success('已加入订货清单')
    refreshCounts?.()
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  }
}

function goToDetail(id) {
  router.push(cPath(`product/${id}`))
}
</script>

<style scoped>
.consumer-hero-compact {
  padding: 20px 20px 24px;
}

.hero-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.hero-text h1 {
  margin: 0 0 6px;
  font-size: 22px;
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.product-section {
  margin-top: 8px;
  background: var(--c-bg);
}

.grid-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.grid-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--c-text);
}

.grid-title .count {
  font-size: 13px;
  color: var(--c-text-secondary);
}

.meta.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.consumer-pagination {
  margin-top: 32px;
  justify-content: center;
}
</style>
