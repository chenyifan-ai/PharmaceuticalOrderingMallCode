/** 将分类树转为 Cascader 选项 */
export function toCascaderOptions(nodes) {
  if (!nodes?.length) return []
  return nodes.map((node) => ({
    value: node.id,
    label: node.name,
    children: node.children?.length ? toCascaderOptions(node.children) : undefined
  }))
}

/** 扁平化分类树，便于按 ID 取名称 */
export function flattenCategories(nodes, list = []) {
  if (!nodes?.length) return list
  for (const node of nodes) {
    list.push(node)
    if (node.children?.length) flattenCategories(node.children, list)
  }
  return list
}

export function findCategoryName(tree, categoryId) {
  if (categoryId == null) return ''
  const flat = flattenCategories(tree)
  return flat.find((c) => c.id === categoryId)?.name ?? ''
}

export function findCategoryPath(tree, targetId, path = []) {
  if (targetId == null || !tree?.length) return []
  for (const node of tree) {
    const next = [...path, node.id]
    if (node.id === targetId) return next
    if (node.children?.length) {
      const found = findCategoryPath(node.children, targetId, next)
      if (found.length) return found
    }
  }
  return []
}
