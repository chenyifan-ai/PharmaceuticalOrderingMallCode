import { watch } from 'vue'

/**
 * B 端列表：下拉/选择类筛选项变更后自动查询；关键词清空时自动查询。
 * @param {object} options
 * @param {import('vue').Reactive<object>} options.searchForm
 * @param {string[]} options.autoKeys - 变更即触发搜索的字段（如 status、userType）
 * @param {() => void} options.onSearch - 通常 handleSearch（会重置 page=1）
 * @param {string} [options.keywordKey='keyword']
 */
export function useListFilter({ searchForm, autoKeys = [], onSearch, keywordKey = 'keyword' }) {
  if (autoKeys.length) {
    watch(
      () => autoKeys.map((k) => searchForm[k]),
      () => onSearch()
    )
  }

  watch(
    () => searchForm[keywordKey],
    (val, oldVal) => {
      if (val === '' && oldVal) {
        onSearch()
      }
    }
  )
}
