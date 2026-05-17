<template>
  <el-carousel
    v-if="banners.length"
    height="220px"
    class="home-banner-carousel"
    indicator-position="outside"
    :interval="5000"
    arrow="hover"
  >
    <el-carousel-item v-for="item in banners" :key="item.id">
      <div class="banner-slide" @click="onBannerClick(item)">
        <img :src="resolveProductImageUrl(item.imageUrl)" :alt="item.title" class="banner-img" />
        <div class="banner-overlay" />
        <div class="banner-caption">
          <span v-if="linkBadge(item)" class="banner-tag">{{ linkBadge(item) }}</span>
          <h3>{{ item.title }}</h3>
          <p v-if="item.subtitle">{{ item.subtitle }}</p>
        </div>
      </div>
    </el-carousel-item>
  </el-carousel>
</template>

<script setup>
import { resolveProductImageUrl } from '@/utils/productImage'

defineProps({
  banners: { type: Array, default: () => [] }
})

const emit = defineEmits(['banner-click'])

function linkBadge(item) {
  const map = { SECKILL: '限时秒杀', PACKAGE: '活动套餐', PRODUCT: '热销药品' }
  return map[item.linkType] || ''
}

function onBannerClick(item) {
  emit('banner-click', item)
}
</script>

<style scoped>
.home-banner-carousel {
  border-radius: var(--c-radius-lg, 16px);
  overflow: hidden;
  box-shadow: var(--c-shadow-hover, 0 12px 40px rgba(99, 102, 241, 0.18));
}

.home-banner-carousel :deep(.el-carousel__container) {
  border-radius: var(--c-radius-lg, 16px);
}

.home-banner-carousel :deep(.el-carousel__indicator button) {
  background: rgba(255, 255, 255, 0.5);
}

.home-banner-carousel :deep(.el-carousel__indicator.is-active button) {
  background: #fff;
}

.banner-slide {
  position: relative;
  height: 220px;
  cursor: pointer;
  background: linear-gradient(135deg, #e0e7ff 0%, #f1f5f9 100%);
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, rgba(30, 27, 75, 0.72) 0%, rgba(30, 27, 75, 0.2) 55%, transparent 100%);
  pointer-events: none;
}

.banner-caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 28px 32px;
  color: #fff;
  z-index: 1;
}

.banner-tag {
  display: inline-block;
  margin-bottom: 8px;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  border-radius: 20px;
  background: rgba(244, 63, 94, 0.9);
}

.banner-caption h3 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.banner-caption p {
  margin: 0;
  font-size: 14px;
  opacity: 0.92;
  max-width: 480px;
}
</style>
