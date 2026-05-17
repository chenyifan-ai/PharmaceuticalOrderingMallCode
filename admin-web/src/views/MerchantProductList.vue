<template>
  <div class="merchant-product-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" :value="2" />
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openFormDialog()">发布商品</el-button>
        </el-form-item>
      </el-form>

      <div class="table-wrap">
        <el-table :data="tableData" v-loading="loading" border stripe :height="tableHeight">
          <el-table-column label="图片" width="72">
            <template #default="{ row }">
              <el-image
                :src="resolveProductImageUrl(row.mainImage)"
                fit="cover"
                style="width: 48px; height: 48px; border-radius: 6px"
              />
            </template>
          </el-table-column>
          <el-table-column
            prop="productName"
            label="商品名称"
            :min-width="colWidths.productName"
            show-overflow-tooltip
          />
          <el-table-column prop="wholesalePrice" label="批发价" width="90">
            <template #default="{ row }">¥{{ row.wholesalePrice }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" size="small">{{ statusName(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openFormDialog(row)">编辑</el-button>
              <el-button v-if="row.status === 1" link type="warning" @click="handleOffline(row)">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        class="page-pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </el-card>

    <el-drawer
      v-model="formVisible"
      :title="form.id ? '编辑商品' : '发布商品'"
      size="880px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ProductFormPanel ref="formPanelRef" :form="form" :rules="formRules" />
      <div class="drawer-footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          {{ form.id ? '保存' : '提交审核' }}
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useListFilter } from '@/composables/useListFilter'
import { estimateColMinWidth, maxCellSample } from '@/utils/table'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTableHeight } from '@/composables/useTableHeight'
import ProductFormPanel from '@/components/product/ProductFormPanel.vue'
import { resolveProductImageUrl } from '@/utils/productImage'
import { defaultProductForm, mapProductToForm, prescriptionLabel, prescriptionTag } from '@/utils/admin-product'
import { getMerchantProducts, publishProduct, updateProduct, offlineProduct } from '@/api/product'

const { tableHeight } = useTableHeight(300)

const loading = ref(false)
const formLoading = ref(false)
const formVisible = ref(false)
const formPanelRef = ref(null)
const tableData = ref([])

const colWidths = computed(() => ({
  productName: estimateColMinWidth('商品名称', maxCellSample(tableData.value, 'productName'), { min: 140 })
}))

const searchForm = reactive({ status: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const form = reactive(defaultProductForm())

const formRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  prescriptionType: [{ required: true, message: '请选择处方类型', trigger: 'change' }],
  wholesalePrice: [{ required: true, message: '请输入批发价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getMerchantProducts({
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: searchForm.status
    })
    tableData.value = res.list
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

useListFilter({
  searchForm,
  autoKeys: ['status'],
  onSearch: handleSearch
})

function handleReset() {
  searchForm.status = null
  handleSearch()
}

function openFormDialog(row) {
  if (row) {
    Object.assign(form, mapProductToForm(row))
    form.categoryId = row.categoryId || 1
  } else {
    Object.assign(form, defaultProductForm())
  }
  formVisible.value = true
}

async function handleSubmit() {
  const valid = await formPanelRef.value?.validate()
  if (!valid) return

  formLoading.value = true
  try {
    const payload = { ...form }
    delete payload.id
    if (form.id) {
      await updateProduct({ id: form.id, ...payload })
      ElMessage.success('更新成功')
    } else {
      await publishProduct(payload)
      ElMessage.success('已提交审核')
    }
    formVisible.value = false
    fetchData()
  } finally {
    formLoading.value = false
  }
}

function handleOffline(row) {
  ElMessageBox.confirm(`确定下架「${row.productName}」吗？`, '下架', { type: 'warning' })
    .then(async () => {
      await offlineProduct(row.id)
      ElMessage.success('下架成功')
      fetchData()
    })
    .catch(() => {})
}

const statusName = (s) => ({ 0: '已下架', 1: '已上架', 2: '待审核' }[s] || '未知')
const statusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'warning' }[s] || '')
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
