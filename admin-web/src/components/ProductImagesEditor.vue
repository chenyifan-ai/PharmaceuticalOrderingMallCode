<template>
  <div class="product-images-editor">
    <section class="image-section main-section">
      <h4 class="section-title">主图 <span class="required">*</span></h4>
      <p class="section-hint">建议 800×800，用于列表与详情首图</p>
      <div class="main-uploader-wrap">
        <ImageUploader v-model="mainImageModel" />
      </div>
    </section>

    <section class="image-section">
      <div class="section-head">
        <h4 class="section-title">轮播图</h4>
        <span class="section-count">{{ galleryList.length }} / {{ maxGallery }}</span>
      </div>
      <p class="section-hint">展示在商品详情顶部，可拖拽排序（按添加顺序）</p>
      <div class="gallery-grid">
        <div v-for="(url, index) in galleryList" :key="'g-' + index" class="gallery-card">
          <ImageUploader :model-value="url" @update:model-value="(v) => updateGallery(index, v)" />
          <el-button type="danger" link size="small" class="remove-btn" @click="removeGallery(index)">
            移除
          </el-button>
        </div>
        <button
          v-if="galleryList.length < maxGallery"
          type="button"
          class="add-card"
          @click="addGallery"
        >
          <span class="add-icon">+</span>
          <span>添加轮播图</span>
        </button>
      </div>
    </section>

    <section class="image-section">
      <div class="section-head">
        <h4 class="section-title">详情图</h4>
        <span class="section-count">{{ detailList.length }} / {{ maxDetail }}</span>
      </div>
      <p class="section-hint">商品详情页图文介绍区域</p>
      <div class="gallery-grid detail-grid">
        <div v-for="(url, index) in detailList" :key="'d-' + index" class="gallery-card">
          <ImageUploader :model-value="url" @update:model-value="(v) => updateDetail(index, v)" />
          <el-button type="danger" link size="small" class="remove-btn" @click="removeDetail(index)">
            移除
          </el-button>
        </div>
        <button
          v-if="detailList.length < maxDetail"
          type="button"
          class="add-card"
          @click="addDetail"
        >
          <span class="add-icon">+</span>
          <span>添加详情图</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ImageUploader from '@/components/ImageUploader.vue'
import { parseProductImageList, stringifyProductImageList } from '@/utils/productImage'

const props = defineProps({
  mainImage: { type: String, default: '' },
  images: { type: String, default: '' },
  detailImages: { type: String, default: '' },
  maxGallery: { type: Number, default: 5 },
  maxDetail: { type: Number, default: 8 }
})

const emit = defineEmits(['update:mainImage', 'update:images', 'update:detailImages'])

const mainImageModel = computed({
  get: () => props.mainImage,
  set: (v) => emit('update:mainImage', v || '')
})

const galleryList = computed({
  get: () => parseProductImageList(props.images),
  set: (list) => emit('update:images', stringifyProductImageList(list))
})

const detailList = computed({
  get: () => parseProductImageList(props.detailImages),
  set: (list) => emit('update:detailImages', stringifyProductImageList(list))
})

function updateGallery(index, url) {
  const next = [...galleryList.value]
  next[index] = url
  galleryList.value = next
}

function removeGallery(index) {
  galleryList.value = galleryList.value.filter((_, i) => i !== index)
}

function addGallery() {
  galleryList.value = [...galleryList.value, '']
}

function updateDetail(index, url) {
  const next = [...detailList.value]
  next[index] = url
  detailList.value = next
}

function removeDetail(index) {
  detailList.value = detailList.value.filter((_, i) => i !== index)
}

function addDetail() {
  detailList.value = [...detailList.value, '']
}
</script>

<style scoped>
.product-images-editor {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.image-section {
  padding: 16px 18px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 10px;
}

.main-section {
  background: linear-gradient(135deg, #f8fafc 0%, #f0f9ff 100%);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.section-title .required {
  color: #f56c6c;
}

.section-count {
  font-size: 12px;
  color: #909399;
}

.section-hint {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
}

.main-uploader-wrap {
  width: 140px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(152px, 1fr));
  gap: 16px;
  align-items: start;
}

.detail-grid {
  grid-template-columns: repeat(auto-fill, minmax(152px, 1fr));
}

.gallery-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 10px;
  background: #fff;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  transition: border-color 0.2s;
  min-width: 0;
}

.gallery-card :deep(.image-uploader) {
  width: 100%;
  display: flex;
  justify-content: center;
}

.gallery-card :deep(.uploader) {
  max-width: 100%;
}

.gallery-card:hover {
  border-color: #409eff;
}

.remove-btn {
  margin-top: 6px;
}

.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 152px;
  min-width: 0;
  padding: 12px;
  border: 1px dashed #c0c4cc;
  border-radius: 8px;
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.add-card:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

.add-icon {
  font-size: 28px;
  line-height: 1;
  margin-bottom: 6px;
}
</style>
