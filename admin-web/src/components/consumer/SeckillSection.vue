<template>
  <section v-if="items.length" class="home-seckill">
    <header class="section-head">
      <div class="title-wrap">
        <span class="badge">秒杀</span>
        <h2>限时抢购</h2>
      </div>
      <div v-if="countdownText" class="countdown">{{ countdownText }}</div>
    </header>
    <div class="seckill-scroll">
      <article
        v-for="item in items"
        :key="item.id"
        class="seckill-card"
        @click="$emit('item-click', item)"
      >
        <div class="img-wrap">
          <ProductImage
            :src="item.product?.mainImage"
            :alt="item.product?.productName || item.title"
            img-class="seckill-thumb"
          />
        </div>
        <h3 class="name">{{ item.product?.productName || item.title }}</h3>
        <div class="price-row">
          <span class="seckill-price">¥{{ item.seckillPrice }}</span>
          <span class="orig-price">¥{{ item.originalPrice }}</span>
        </div>
        <el-progress
          :percentage="item.progressPercent || 0"
          :stroke-width="6"
          :show-text="false"
          color="#ef4444"
        />
        <p class="stock-tip">已抢 {{ item.progressPercent || 0 }}% · 剩 {{ item.stock }} 件</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import ProductImage from '@/components/ProductImage.vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  endTime: { type: String, default: '' }
})

defineEmits(['item-click'])

const countdownText = ref('')

let timer = null

function updateCountdown() {
  const end = props.items[0]?.endTime
  if (!end) {
    countdownText.value = ''
    return
  }
  const diff = new Date(end).getTime() - Date.now()
  if (diff <= 0) {
    countdownText.value = '已结束'
    return
  }
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  countdownText.value = `距结束 ${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

onMounted(() => {
  updateCountdown()
  timer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.home-seckill {
  margin: 0;
  padding: 22px;
  background: linear-gradient(135deg, #fff1f2 0%, #fff 55%);
  border-radius: var(--c-radius-lg, 16px);
  border: 1px solid #fecdd3;
  box-shadow: var(--c-shadow);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-wrap h2 {
  margin: 0;
  font-size: 20px;
  color: #991b1b;
}

.badge {
  background: linear-gradient(135deg, #f43f5e, #e11d48);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 700;
}

.countdown {
  font-size: 14px;
  color: #dc2626;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.seckill-scroll {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.seckill-card {
  flex: 0 0 160px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  border: 1px solid #fee2e2;
  transition: box-shadow 0.2s;
}

.seckill-card:hover {
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
}

.img-wrap {
  width: 100%;
  aspect-ratio: 1;
  background: #f8fafc;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 8px;
}

.img-wrap :deep(.seckill-thumb) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.name {
  font-size: 13px;
  margin: 0 0 8px;
  line-height: 1.3;
  height: 34px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 8px;
}

.seckill-price {
  color: #dc2626;
  font-size: 18px;
  font-weight: 700;
}

.orig-price {
  color: #94a3b8;
  font-size: 12px;
  text-decoration: line-through;
}

.stock-tip {
  margin: 6px 0 0;
  font-size: 11px;
  color: #64748b;
}
</style>
