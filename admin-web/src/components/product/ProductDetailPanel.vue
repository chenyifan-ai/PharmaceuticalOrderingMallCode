<template>
  <div v-if="product" class="product-detail-panel">
    <div class="detail-layout">
      <aside class="detail-gallery">
        <ProductImageGallery :product="product" :alt="product.productName" />
        <div class="price-card">
          <div class="price-row">
            <span class="label">批发价</span>
            <span class="wholesale">{{ formatPrice(product.wholesalePrice) }}</span>
          </div>
          <div v-if="product.marketPrice" class="price-row sub">
            <span class="label">市场价</span>
            <span class="market">{{ formatPrice(product.marketPrice) }}</span>
          </div>
        </div>
        <ul class="quick-stats">
          <li>
            <span>库存</span>
            <strong :class="stockClass">{{ product.stock ?? 0 }}</strong>
          </li>
          <li>
            <span>销量</span>
            <strong>{{ product.sales ?? 0 }}</strong>
          </li>
          <li>
            <span>起订量</span>
            <strong>{{ product.minOrderQuantity ?? 1 }}</strong>
          </li>
        </ul>
      </aside>

      <main class="detail-main">
        <header class="detail-header">
          <div>
            <h2 class="title">{{ product.productName }}</h2>
            <p v-if="product.genericName" class="generic">通用名：{{ product.genericName }}</p>
            <p v-if="product.description" class="desc-brief">{{ product.description }}</p>
          </div>
          <div class="tags">
            <el-tag :type="productStatusTag(product.status)" effect="dark">
              {{ productStatusLabel(product.status) }}
            </el-tag>
            <el-tag :type="prescriptionTag(product.prescriptionType)" effect="plain">
              {{ prescriptionLabel(product.prescriptionType) }}
            </el-tag>
            <el-tag v-if="product.isHot === 1" type="danger" effect="plain">热销</el-tag>
            <el-tag v-if="product.isNew === 1" type="success" effect="plain">新品</el-tag>
            <el-tag v-if="product.isRecommend === 1" type="warning" effect="plain">推荐</el-tag>
          </div>
        </header>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="基础信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品ID">{{ product.id }}</el-descriptions-item>
              <el-descriptions-item label="商品分类">{{ categoryLabel }}</el-descriptions-item>
              <el-descriptions-item label="品牌">{{ product.brand || '-' }}</el-descriptions-item>
              <el-descriptions-item label="排序权重">{{ product.sort ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{ product.specification || '-' }}</el-descriptions-item>
              <el-descriptions-item label="剂型">{{ product.dosageForm || '-' }}</el-descriptions-item>
              <el-descriptions-item label="生产厂家" :span="2">{{ product.manufacturer || '-' }}</el-descriptions-item>
              <el-descriptions-item label="批准文号">{{ product.approvalNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="条形码">{{ product.barcode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="供应商ID">{{ product.supplierId ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(product.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(product.updateTime) }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="销售库存" name="sales">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="批发价">{{ formatPrice(product.wholesalePrice) }}</el-descriptions-item>
              <el-descriptions-item label="市场价">{{ formatPrice(product.marketPrice) }}</el-descriptions-item>
              <el-descriptions-item label="库存">
                <span :class="stockClass">{{ product.stock }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="销量">{{ product.sales ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="最小起订">{{ product.minOrderQuantity ?? 1 }}</el-descriptions-item>
              <el-descriptions-item label="最大限购">{{ product.maxOrderQuantity ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="重量">{{ product.weight != null ? product.weight + ' kg' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="体积">{{ product.volume != null ? product.volume + ' m³' : '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="药品说明" name="medical">
            <div class="text-blocks">
              <section v-if="product.indications">
                <h4>适应症 / 功效</h4>
                <p>{{ product.indications }}</p>
              </section>
              <section v-if="product.usage">
                <h4>用法用量</h4>
                <p>{{ product.usage }}</p>
              </section>
              <section v-if="product.contraindications">
                <h4>禁忌</h4>
                <p>{{ product.contraindications }}</p>
              </section>
              <section v-if="product.adverseReactions">
                <h4>不良反应</h4>
                <p>{{ product.adverseReactions }}</p>
              </section>
              <section v-if="product.precautions">
                <h4>注意事项</h4>
                <p>{{ product.precautions }}</p>
              </section>
              <section v-if="product.storageCondition">
                <h4>储存条件</h4>
                <p>{{ product.storageCondition }}</p>
              </section>
              <section v-if="product.validityPeriod">
                <h4>有效期</h4>
                <p>{{ product.validityPeriod }} 个月</p>
              </section>
              <section v-if="product.instruction" class="instruction">
                <h4>说明书</h4>
                <div class="html-body" v-html="product.instruction" />
              </section>
              <el-empty v-if="!hasMedicalInfo" description="暂无药品说明信息" :image-size="80" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="详情图" name="images">
            <div v-if="detailImages.length" class="detail-images-grid">
              <ProductImage
                v-for="(url, i) in detailImages"
                :key="i"
                :src="url"
                :alt="product.productName"
                img-class="detail-img"
              />
            </div>
            <el-empty v-else description="暂无详情图" :image-size="80" />
          </el-tab-pane>

          <el-tab-pane label="审核记录" name="audit">
            <el-alert
              v-if="product.auditRemark"
              type="warning"
              :title="product.auditRemark"
              show-icon
              :closable="false"
              class="audit-alert"
            />
            <el-empty v-else description="暂无审核备注" :image-size="80" />
          </el-tab-pane>
        </el-tabs>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ProductImageGallery from '@/components/ProductImageGallery.vue'
import ProductImage from '@/components/ProductImage.vue'
import { parseProductImageList } from '@/utils/productImage'
import { formatDateTime } from '@/utils/format'
import { useCategoryTree } from '@/composables/useCategoryTree'
import { findCategoryName } from '@/utils/category'
import {
  formatPrice,
  productStatusLabel,
  productStatusTag,
  prescriptionLabel,
  prescriptionTag
} from '@/utils/admin-product'

const props = defineProps({
  product: { type: Object, default: null }
})

const activeTab = ref('basic')
const { categoryTree, load } = useCategoryTree()
load()

const detailImages = computed(() => parseProductImageList(props.product?.detailImages))

const categoryLabel = computed(() => {
  const id = props.product?.categoryId
  if (id == null) return '-'
  return findCategoryName(categoryTree.value || [], id) || `ID ${id}`
})

const stockClass = computed(() => {
  const s = props.product?.stock ?? 0
  if (s <= 0) return 'stock-none'
  if (s < 10) return 'stock-low'
  return ''
})

const hasMedicalInfo = computed(() => {
  const p = props.product
  if (!p) return false
  return !!(
    p.indications ||
    p.usage ||
    p.contraindications ||
    p.adverseReactions ||
    p.precautions ||
    p.storageCondition ||
    p.validityPeriod ||
    p.instruction
  )
})
</script>

<style scoped>
.product-detail-panel {
  padding: 0 4px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 28px;
  align-items: start;
}

.detail-gallery {
  position: sticky;
  top: 0;
}

.price-card {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #f8fafc 100%);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.price-row.sub {
  margin-top: 8px;
}

.price-row .label {
  font-size: 13px;
  color: #64748b;
}

.wholesale {
  font-size: 26px;
  font-weight: 700;
  color: #dc2626;
}

.market {
  font-size: 14px;
  color: #94a3b8;
  text-decoration: line-through;
}

.quick-stats {
  list-style: none;
  margin: 12px 0 0;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.quick-stats li {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
  color: #64748b;
}

.quick-stats strong {
  color: #334155;
  font-size: 15px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
}

.generic {
  margin: 0 0 4px;
  font-size: 14px;
  color: #64748b;
}

.desc-brief {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  max-width: 640px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex-shrink: 0;
}

.detail-tabs :deep(.el-tabs__content) {
  padding-top: 16px;
}

.text-blocks section {
  margin-bottom: 20px;
}

.text-blocks h4 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.text-blocks p,
.html-body {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #475569;
  white-space: pre-wrap;
}

.detail-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.detail-images-grid :deep(.detail-img) {
  width: 100%;
  max-height: 280px;
  object-fit: contain;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.audit-alert {
  max-width: 720px;
}

.stock-none {
  color: #ef4444;
  font-weight: 600;
}

.stock-low {
  color: #f59e0b;
  font-weight: 600;
}

@media (max-width: 960px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-gallery {
    position: static;
    max-width: 360px;
  }
}
</style>
