<template>
  <div class="product-image-gallery">
    <div class="main-wrap">
      <ProductImage :src="activeSrc" :alt="alt" img-class="main-img" />
    </div>
    <div v-if="thumbs.length > 1" class="thumbs">
      <button
        v-for="(url, i) in thumbs"
        :key="i"
        type="button"
        class="thumb-btn"
        :class="{ active: i === activeIndex }"
        @click="activeIndex = i"
      >
        <ProductImage :src="url" :alt="alt" img-class="thumb-img" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import ProductImage from '@/components/ProductImage.vue'
import { getProductGallery } from '@/utils/productImage'

const props = defineProps({
  product: { type: Object, default: null },
  alt: { type: String, default: '' }
})

const activeIndex = ref(0)

const thumbs = computed(() => getProductGallery(props.product).filter(Boolean))

const activeSrc = computed(() => thumbs.value[activeIndex.value] || '')

watch(
  () => props.product,
  () => {
    activeIndex.value = 0
  }
)
</script>

<style scoped>
.product-image-gallery {
  width: 100%;
}

.main-wrap {
  aspect-ratio: 1;
  background: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-wrap :deep(.main-img) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.thumbs {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.thumb-btn {
  width: 64px;
  height: 64px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #f1f5f9;
}

.thumb-btn.active {
  border-color: var(--el-color-primary, #409eff);
}

.thumb-btn :deep(.thumb-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
