<template>
  <img
    :src="displaySrc"
    :alt="alt"
    :class="imgClass"
    loading="lazy"
    @error="onError"
  />
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { resolveProductImageUrl, productImagePlaceholder } from '@/utils/productImage'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  imgClass: { type: String, default: '' }
})

const failed = ref(false)

const displaySrc = computed(() => {
  if (failed.value) {
    return productImagePlaceholder()
  }
  return resolveProductImageUrl(props.src)
})

watch(
  () => props.src,
  () => {
    failed.value = false
  }
)

function onError() {
  failed.value = true
}
</script>
