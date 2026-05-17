<template>
  <div class="product-form-panel" :class="{ 'is-page': layout === 'page' }">
    <aside v-if="layout === 'page'" class="form-preview">
      <div class="preview-card">
        <ProductImage
          :src="form.mainImage"
          :alt="form.productName || '商品主图'"
          img-class="preview-img"
        />
        <h3 class="preview-name">{{ form.productName || '未填写商品名称' }}</h3>
        <p v-if="form.genericName" class="preview-generic">{{ form.genericName }}</p>
        <div class="preview-price">{{ formatPrice(form.wholesalePrice) }}</div>
        <div class="preview-tags">
          <el-tag size="small" :type="productStatusTag(form.status)">
            {{ productStatusLabel(form.status) }}
          </el-tag>
          <el-tag size="small" :type="prescriptionTag(form.prescriptionType)" effect="plain">
            {{ prescriptionLabel(form.prescriptionType) }}
          </el-tag>
        </div>
        <ul class="preview-meta">
          <li><span>库存</span><strong :class="stockHintClass">{{ form.stock ?? 0 }}</strong></li>
          <li><span>分类</span><strong>{{ previewCategoryName || '-' }}</strong></li>
        </ul>
      </div>
    </aside>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="form-body">
      <el-tabs v-model="activeTab" class="form-tabs">
        <el-tab-pane label="图片资料" name="images">
          <el-card shadow="never" class="form-section">
            <ProductImagesEditor
              v-model:main-image="form.mainImage"
              v-model:images="form.images"
              v-model:detail-images="form.detailImages"
            />
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="基本信息" name="basic">
          <el-card shadow="never" class="form-section">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="商品名称" prop="productName">
                  <el-input
                    v-model="form.productName"
                    placeholder="请输入商品名称"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="通用名">
                  <el-input v-model="form.genericName" placeholder="通用名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品分类" prop="categoryId">
                  <el-cascader
                    v-model="categoryPath"
                    :options="cascaderOptions"
                    :props="{ checkStrictly: true }"
                    clearable
                    filterable
                    placeholder="选择分类"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="处方类型" prop="prescriptionType">
                  <el-select v-model="form.prescriptionType" style="width: 100%">
                    <el-option label="OTC（非处方）" value="OTC" />
                    <el-option label="处方药" value="PRESCRIPTION" />
                    <el-option label="双轨制" value="DUAL_TRACK" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="品牌">
                  <el-input v-model="form.brand" placeholder="品牌名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规格">
                  <el-input v-model="form.specification" placeholder="如 0.5g*24粒" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="剂型">
                  <el-input v-model="form.dosageForm" placeholder="片剂、胶囊等" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="生产厂家">
                  <el-input v-model="form.manufacturer" placeholder="生产厂家全称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="批准文号">
                  <el-input v-model="form.approvalNumber" placeholder="国药准字" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="条形码">
                  <el-input v-model="form.barcode" placeholder="商品条形码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="上架状态">
                  <el-select v-model="form.status" style="width: 100%">
                    <el-option label="已上架" :value="1" />
                    <el-option label="已下架" :value="0" />
                    <el-option label="待审核" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排序权重">
                  <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="运营标记">
                  <div class="flag-switches">
                    <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" active-text="热销" />
                    <el-switch v-model="form.isNew" :active-value="1" :inactive-value="0" active-text="新品" />
                    <el-switch
                      v-model="form.isRecommend"
                      :active-value="1"
                      :inactive-value="0"
                      active-text="推荐"
                    />
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="价格库存" name="price">
          <el-card shadow="never" class="form-section">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="批发价" prop="wholesalePrice">
                  <el-input-number v-model="form.wholesalePrice" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="市场价">
                  <el-input-number v-model="form.marketPrice" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="库存" prop="stock">
                  <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最小起订">
                  <el-input-number v-model="form.minOrderQuantity" :min="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最大限购">
                  <el-input-number v-model="form.maxOrderQuantity" :min="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="有效期(月)">
                  <el-input-number v-model="form.validityPeriod" :min="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="重量(kg)">
                  <el-input-number v-model="form.weight" :min="0" :precision="3" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体积(m³)">
                  <el-input-number v-model="form.volume" :min="0" :precision="4" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="药品说明" name="medical">
          <el-card shadow="never" class="form-section">
            <el-form-item label="适应症">
              <el-input v-model="form.indications" type="textarea" :rows="3" placeholder="适应症或功效说明" />
            </el-form-item>
            <el-form-item label="用法用量">
              <el-input v-model="form.usage" type="textarea" :rows="3" placeholder="用法用量说明" />
            </el-form-item>
            <el-form-item label="禁忌">
              <el-input v-model="form.contraindications" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="不良反应">
              <el-input v-model="form.adverseReactions" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="注意事项">
              <el-input v-model="form.precautions" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="储存条件">
              <el-input v-model="form.storageCondition" placeholder="如 阴凉干燥处保存" />
            </el-form-item>
            <el-form-item label="商品描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简短卖点或摘要" />
            </el-form-item>
            <el-form-item label="说明书">
              <el-input v-model="form.instruction" type="textarea" :rows="5" placeholder="支持 HTML 富文本" />
            </el-form-item>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ProductImagesEditor from '@/components/ProductImagesEditor.vue'
import ProductImage from '@/components/ProductImage.vue'
import { useCategoryTree } from '@/composables/useCategoryTree'
import { toCascaderOptions, findCategoryPath, findCategoryName } from '@/utils/category'
import {
  formatPrice,
  productStatusLabel,
  productStatusTag,
  prescriptionLabel,
  prescriptionTag,
  PRODUCT_FORM_FIELD_TAB
} from '@/utils/admin-product'

const props = defineProps({
  form: { type: Object, required: true },
  rules: { type: Object, default: () => ({}) },
  layout: { type: String, default: 'compact' }
})

const formRef = ref(null)
const activeTab = ref('images')

const { categoryTree, load } = useCategoryTree()
load()

const cascaderOptions = computed(() => toCascaderOptions(categoryTree.value || []))

const categoryPath = computed({
  get() {
    return findCategoryPath(categoryTree.value || [], props.form.categoryId)
  },
  set(path) {
    props.form.categoryId = path?.length ? path[path.length - 1] : null
  }
})

const previewCategoryName = computed(() =>
  findCategoryName(categoryTree.value || [], props.form.categoryId)
)

const stockHintClass = computed(() => {
  const s = props.form.stock ?? 0
  if (s <= 0) return 'stock-none'
  if (s < 10) return 'stock-low'
  return ''
})

function focusFirstErrorTab(fields) {
  const keys = Object.keys(fields || {})
  for (const key of keys) {
    const tab = PRODUCT_FORM_FIELD_TAB[key]
    if (tab) {
      activeTab.value = tab
      break
    }
  }
}

async function validate() {
  try {
    await formRef.value?.validate()
    return true
  } catch (fields) {
    focusFirstErrorTab(fields)
    return false
  }
}

function resetFields() {
  formRef.value?.resetFields()
}

defineExpose({ validate, resetFields, formRef, activeTab })
</script>

<style scoped>
.product-form-panel {
  width: 100%;
}

.product-form-panel.is-page {
  display: grid;
  grid-template-columns: minmax(240px, 280px) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.form-preview {
  position: sticky;
  top: 12px;
}

.preview-card {
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.preview-card :deep(.preview-img) {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.preview-name {
  margin: 12px 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
}

.preview-generic {
  margin: 0 0 8px;
  font-size: 13px;
  color: #64748b;
}

.preview-price {
  font-size: 22px;
  font-weight: 700;
  color: #dc2626;
  margin-bottom: 10px;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.preview-meta {
  list-style: none;
  margin: 0;
  padding: 12px 0 0;
  border-top: 1px dashed #e2e8f0;
}

.preview-meta li {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 4px 0;
  color: #64748b;
}

.preview-meta strong {
  color: #334155;
}

.stock-none {
  color: #ef4444;
}

.stock-low {
  color: #f59e0b;
}

.form-body {
  min-width: 0;
}

.form-tabs :deep(.el-tabs__content) {
  padding-top: 12px;
}

.form-section {
  border: 1px solid #ebeef5;
}

.form-section :deep(.el-card__body) {
  padding: 20px 20px 4px;
  overflow: visible;
}

.product-form-panel.is-page .form-section :deep(.el-card__body) {
  padding: 16px 18px 8px;
}

.flag-switches {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

@media (max-width: 900px) {
  .product-form-panel.is-page {
    grid-template-columns: 1fr;
  }

  .form-preview {
    position: static;
    max-width: 320px;
  }
}
</style>
