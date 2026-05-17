<template>
  <div class="consumer-page home-page">
    <div class="home-page-bg" aria-hidden="true">
      <div class="home-bg-mesh" />
      <div class="home-bg-orb home-bg-orb--indigo" />
      <div class="home-bg-orb home-bg-orb--violet" />
      <div class="home-bg-orb home-bg-orb--rose" />
      <div class="home-bg-wave" />
    </div>

    <section class="home-banner-section">
      <div class="consumer-container">
        <HomeBannerCarousel
          v-if="promoSlides.length"
          :banners="promoSlides"
          @banner-click="onBannerClick"
        />
        <div v-else-if="!loading" class="banner-placeholder">
          <p>精选套餐 · 限时秒杀 · 一站采购</p>
        </div>
      </div>
    </section>

    <section class="consumer-container home-sections" v-loading="loading">
      <SeckillSection
        v-if="seckills.length"
        :items="seckills"
        @item-click="onSeckillClick"
      />

      <PackageSection
        v-if="packages.length"
        :packages="packages"
        @package-click="onPackageClick"
        @buy-package="onBuyPackage"
      />

      <div v-if="!loading && !seckills.length && !packages.length" class="consumer-empty-wrap">
        <el-empty description="暂无活动，敬请期待" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData } from '@/api/consumer/home'
import HomeBannerCarousel from '@/components/consumer/HomeBannerCarousel.vue'
import SeckillSection from '@/components/consumer/SeckillSection.vue'
import PackageSection from '@/components/consumer/PackageSection.vue'
import { cPath } from '@/utils/consumer-path'

const router = useRouter()
const loading = ref(false)
const banners = ref([])
const seckills = ref([])
const packages = ref([])

/** 轮播：后台配置 + 套餐/秒杀活动自动生成 */
const promoSlides = computed(() => {
  const list = [...(banners.value || [])]
  const seen = new Set(list.map((b) => `${b.linkType}-${b.linkValue}`))

  for (const pkg of packages.value.slice(0, 2)) {
    const key = `PACKAGE-${pkg.id}`
    if (!seen.has(key)) {
      list.push({
        id: `pkg-${pkg.id}`,
        title: pkg.packageName,
        subtitle: pkg.subtitle || '组合套餐 · 省心省钱',
        imageUrl: pkg.bannerImage,
        linkType: 'PACKAGE',
        linkValue: String(pkg.id)
      })
      seen.add(key)
    }
  }

  for (const sk of seckills.value.slice(0, 2)) {
    const key = `SECKILL-${sk.id}`
    if (!seen.has(key)) {
      list.push({
        id: `sk-${sk.id}`,
        title: sk.title || sk.product?.productName || '限时秒杀',
        subtitle: `秒杀价 ¥${sk.seckillPrice} · 数量有限`,
        imageUrl: sk.product?.mainImage,
        linkType: 'SECKILL',
        linkValue: String(sk.id)
      })
      seen.add(key)
    }
  }

  return list
})

onMounted(fetchHome)

async function fetchHome() {
  loading.value = true
  try {
    const data = await getHomeData()
    banners.value = data?.banners ?? []
    seckills.value = data?.seckills ?? []
    packages.value = data?.packages ?? []
  } catch {
    banners.value = []
    seckills.value = []
    packages.value = []
  } finally {
    loading.value = false
  }
}

function onBannerClick(item) {
  if (!item?.linkType || item.linkType === 'NONE') return
  const val = item.linkValue
  if (item.linkType === 'PACKAGE' && val) {
    router.push(cPath(`package/${val}`))
    return
  }
  if (item.linkType === 'SECKILL' && val) {
    const sk = seckills.value.find((s) => String(s.id) === String(val))
    if (sk?.productId) {
      router.push({
        path: cPath(`product/${sk.productId}`),
        query: { seckillId: sk.id, seckillPrice: sk.seckillPrice }
      })
    }
    return
  }
  if (item.linkType === 'PRODUCT' && val) {
    router.push(cPath(`product/${val}`))
  }
}

function onSeckillClick(item) {
  if (!item?.productId) return
  router.push({
    path: cPath(`product/${item.productId}`),
    query: { seckillId: item.id, seckillPrice: item.seckillPrice }
  })
}

function onPackageClick(pkg) {
  router.push(cPath(`package/${pkg.id}`))
}

function onBuyPackage(pkg) {
  router.push({
    path: cPath('checkout'),
    query: { type: 'package', packageId: pkg.id }
  })
}
</script>

<style scoped>
.home-page {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  background: transparent;
  padding-bottom: 48px;
}

.home-page-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background: linear-gradient(
    165deg,
    #eef2ff 0%,
    #f5f7ff 28%,
    #f8fafc 52%,
    #faf5ff 78%,
    #fff5f7 100%
  );
}

.home-bg-mesh {
  position: absolute;
  inset: 0;
  opacity: 0.55;
  background-image: radial-gradient(circle at 1px 1px, rgba(99, 102, 241, 0.09) 1px, transparent 0);
  background-size: 26px 26px;
  mask-image: linear-gradient(180deg, #000 0%, #000 45%, transparent 92%);
}

.home-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(72px);
  will-change: transform;
}

.home-bg-orb--indigo {
  width: min(52vw, 480px);
  height: min(52vw, 480px);
  top: -18%;
  left: -12%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.42) 0%, transparent 68%);
  animation: home-orb-drift 18s ease-in-out infinite;
}

.home-bg-orb--violet {
  width: min(44vw, 400px);
  height: min(44vw, 400px);
  top: 8%;
  right: -10%;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.36) 0%, transparent 70%);
  animation: home-orb-drift 22s ease-in-out infinite reverse;
}

.home-bg-orb--rose {
  width: min(48vw, 440px);
  height: min(48vw, 440px);
  bottom: -8%;
  left: 28%;
  background: radial-gradient(circle, rgba(244, 63, 94, 0.22) 0%, transparent 72%);
  animation: home-orb-drift 26s ease-in-out infinite;
  animation-delay: -6s;
}

.home-bg-wave {
  position: absolute;
  left: 0;
  right: 0;
  top: 38%;
  height: 220px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 120' preserveAspectRatio='none'%3E%3Cpath fill='%23ffffff' fill-opacity='0.35' d='M0,64 C320,120 480,24 720,64 C960,104 1120,40 1440,72 L1440,120 L0,120 Z'/%3E%3Cpath fill='%23ffffff' fill-opacity='0.2' d='M0,80 C360,32 520,96 720,56 C920,16 1080,88 1440,48 L1440,120 L0,120 Z'/%3E%3C/svg%3E")
    center bottom / 100% 100% no-repeat;
  opacity: 0.9;
}

@keyframes home-orb-drift {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(12px, -16px) scale(1.04);
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-bg-orb {
    animation: none;
  }
}

.home-banner-section,
.home-sections {
  position: relative;
  z-index: 1;
}

.home-banner-section {
  padding: 20px 0 8px;
  background: linear-gradient(
    180deg,
    rgba(238, 242, 255, 0.75) 0%,
    rgba(248, 250, 252, 0.2) 100%
  );
}

.banner-placeholder {
  height: 220px;
  border-radius: var(--c-radius-lg, 16px);
  background: linear-gradient(135deg, var(--c-primary, #6366f1) 0%, var(--c-primary-light, #818cf8) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  box-shadow: var(--c-shadow, 0 8px 32px rgba(99, 102, 241, 0.2));
}

.home-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-top: 20px;
}

.home-sections :deep(.home-seckill),
.home-sections :deep(.home-packages) {
  margin: 0;
}
</style>
