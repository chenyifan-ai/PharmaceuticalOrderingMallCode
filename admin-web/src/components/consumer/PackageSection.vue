<template>
  <section v-if="packages.length" class="home-packages">
    <header class="section-head">
      <span class="badge">套餐</span>
      <h2>活动组合套餐</h2>
    </header>
    <div class="package-grid">
      <article
        v-for="pkg in packages"
        :key="pkg.id"
        class="package-card"
        @click="$emit('package-click', pkg)"
      >
        <div class="banner">
          <ProductImage :src="pkg.bannerImage" :alt="pkg.packageName" img-class="banner-img" />
          <span v-if="pkg.discountPercent" class="discount-tag">省{{ pkg.discountPercent }}%</span>
        </div>
        <div class="body">
          <h3>{{ pkg.packageName }}</h3>
          <p class="sub">{{ pkg.subtitle }}</p>
          <div class="items-preview" v-if="pkg.items?.length">
            <ProductImage
              v-for="(it, i) in pkg.items.slice(0, 3)"
              :key="i"
              :src="it.image"
              :alt="it.productName"
              img-class="mini-thumb"
            />
            <span v-if="pkg.items.length > 3" class="more">+{{ pkg.items.length - 3 }}</span>
          </div>
          <div class="foot">
            <span class="price">¥{{ pkg.packagePrice }}</span>
            <span class="orig">¥{{ pkg.originalPrice }}</span>
            <el-button type="primary" size="small" round @click.stop="$emit('buy-package', pkg)">
              立即购买
            </el-button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import ProductImage from '@/components/ProductImage.vue'

defineProps({
  packages: { type: Array, default: () => [] }
})

defineEmits(['package-click', 'buy-package'])
</script>

<style scoped>
.home-packages {
  margin: 0;
  padding: 22px;
  background: var(--c-surface, #fff);
  border-radius: var(--c-radius-lg, 16px);
  border: 1px solid var(--c-border);
  box-shadow: var(--c-shadow);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
}

.badge {
  background: linear-gradient(135deg, var(--c-primary, #6366f1), var(--c-primary-light, #818cf8));
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 700;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.package-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.package-card:hover {
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.banner {
  position: relative;
  height: 120px;
  background: #e2e8f0;
}

.banner :deep(.banner-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.discount-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #f59e0b;
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 600;
}

.body {
  padding: 14px 16px 16px;
}

.body h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.sub {
  margin: 0 0 10px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
}

.items-preview {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
}

.items-preview :deep(.mini-thumb) {
  width: 40px;
  height: 40px;
  object-fit: contain;
  border-radius: 6px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.more {
  font-size: 12px;
  color: #94a3b8;
}

.foot {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.price {
  color: #dc2626;
  font-size: 20px;
  font-weight: 700;
}

.orig {
  color: #94a3b8;
  font-size: 13px;
  text-decoration: line-through;
  flex: 1;
}
</style>
