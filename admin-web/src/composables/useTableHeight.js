import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 计算表格可用高度，使 el-table 填满页面剩余区域
 * @param {number} offset 顶部占用高度（header+padding+搜索栏+分页等）
 */
export function useTableHeight(offset = 300) {
  const tableHeight = ref(400)

  const calc = () => {
    tableHeight.value = Math.max(320, window.innerHeight - offset)
  }

  onMounted(() => {
    calc()
    window.addEventListener('resize', calc)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', calc)
  })

  return { tableHeight, recalcTableHeight: calc }
}
