<template>
  <div class="product-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待审核" :value="2" />
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="goCreate">添加药品</el-button>
        </el-form-item>
      </el-form>

      <div class="table-wrap">
        <el-table :data="tableData" v-loading="loading" border stripe :height="tableHeight" class="admin-data-table">
          <el-table-column prop="id" label="ID" :min-width="64" />
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
          <el-table-column prop="brand" label="品牌" :min-width="colWidths.brand" show-overflow-tooltip />
          <el-table-column prop="prescriptionType" label="处方类型" min-width="118">
            <template #default="{ row }">
              <el-tag size="small" :type="prescriptionTag(row.prescriptionType)">
                {{ prescriptionLabel(row.prescriptionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="wholesalePrice" label="批发价" width="90">
            <template #default="{ row }">¥{{ row.wholesalePrice }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="productStatusTag(row.status)" size="small">
                {{ productStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" :min-width="300" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="goDetail(row)">详情</el-button>
              <el-button link type="primary" @click="goEdit(row)">修改</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              <template v-if="row.status === 2">
                <el-button link type="success" @click="handleAudit(row, 1)">通过</el-button>
                <el-button link type="danger" @click="openRejectDialog(row)">驳回</el-button>
              </template>
              <el-button v-if="row.status === 1" link type="warning" @click="handleOffline(row)">下架</el-button>
              <el-button v-if="row.status === 0" link type="success" @click="handleOnline(row)">上架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        class="page-pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="rejectVisible" title="驳回商品" width="420px">
      <el-input v-model="rejectRemark" type="textarea" :rows="4" placeholder="请填写驳回原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="handleConfirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTableHeight } from '@/composables/useTableHeight'
import { useListFilter } from '@/composables/useListFilter'
import { estimateColMinWidth, maxCellSample } from '@/utils/table'
import { resolveProductImageUrl } from '@/utils/productImage'
import {
  productStatusLabel,
  productStatusTag,
  prescriptionLabel,
  prescriptionTag
} from '@/utils/admin-product'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminProductList,
  auditProduct,
  offlineProduct,
  onlineProduct,
  deleteAdminProduct
} from '@/api/admin'

const router = useRouter()
const { tableHeight } = useTableHeight(300)

const loading = ref(false)
const rejectLoading = ref(false)
const rejectVisible = ref(false)
const rejectRow = ref(null)
const rejectRemark = ref('')

const searchForm = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])

const colWidths = computed(() => ({
  productName: estimateColMinWidth('商品名称', maxCellSample(tableData.value, 'productName'), { min: 120 }),
  brand: estimateColMinWidth('品牌', maxCellSample(tableData.value, 'brand'), { min: 80 })
}))

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminProductList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
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
  Object.assign(searchForm, { keyword: '', status: null })
  handleSearch()
}

function goDetail(row) {
  router.push(`/products/${row.id}`)
}

function goEdit(row) {
  router.push(`/products/${row.id}/edit`)
}

function goCreate() {
  router.push('/products/create')
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除药品「${row.productName}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteAdminProduct(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

function handleAudit(row, status) {
  ElMessageBox.confirm('确定通过该商品审核吗？', '提示', { type: 'warning' })
    .then(async () => {
      await auditProduct(row.id, status)
      ElMessage.success('操作成功')
      fetchData()
    })
    .catch(() => {})
}

function openRejectDialog(row) {
  rejectRow.value = row
  rejectRemark.value = ''
  rejectVisible.value = true
}

async function handleConfirmReject() {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  rejectLoading.value = true
  try {
    await auditProduct(rejectRow.value.id, 0, rejectRemark.value)
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchData()
  } finally {
    rejectLoading.value = false
  }
}

function handleOffline(row) {
  ElMessageBox.confirm('确定要下架该商品吗？', '违规下架', { type: 'warning' })
    .then(async () => {
      await offlineProduct(row.id)
      ElMessage.success('下架成功')
      fetchData()
    })
    .catch(() => {})
}

function handleOnline(row) {
  ElMessageBox.confirm(`确定上架「${row.productName}」吗？`, '商品上架', { type: 'info' })
    .then(async () => {
      await onlineProduct(row.id)
      ElMessage.success('上架成功')
      fetchData()
    })
    .catch(() => {})
}
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}
</style>
