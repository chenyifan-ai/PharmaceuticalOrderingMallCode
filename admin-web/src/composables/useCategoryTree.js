import { ref, onMounted } from 'vue'
import { getCategoryTree } from '@/api/category'
import { findCategoryName } from '@/utils/category'

const treeCache = ref(null)
let loadPromise = null

export function useCategoryTree() {
  const loading = ref(false)

  async function load() {
    if (treeCache.value) return treeCache.value
    if (loadPromise) return loadPromise
    loading.value = true
    loadPromise = getCategoryTree()
      .then((data) => {
        treeCache.value = Array.isArray(data) ? data : []
        return treeCache.value
      })
      .catch(() => {
        treeCache.value = []
        return []
      })
      .finally(() => {
        loading.value = false
        loadPromise = null
      })
    return loadPromise
  }

  function categoryName(categoryId) {
    return findCategoryName(treeCache.value || [], categoryId)
  }

  onMounted(load)

  return { categoryTree: treeCache, loading, load, categoryName }
}
