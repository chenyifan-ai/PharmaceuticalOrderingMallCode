<template>
  <div class="stock-manage page-container">
    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card">
          <p class="summary-label">SKU 总数</p>
          <p class="summary-value">{{ summary.totalSku ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card warn">
          <p class="summary-label">库存偏低</p>
          <p class="summary-value">{{ summary.lowStockCount ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card danger">
          <p class="summary-label">缺货</p>
          <p class="summary-value">{{ summary.outOfStockCount ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card">
          <p class="summary-label">库存总量</p>
          <p class="summary-value">{{ summary.totalQuantity ?? 0 }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="商品名/品牌/厂家/批准文号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.stockFilter" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="偏低" value="LOW" />
            <el-option label="缺货" value="OUT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openInbound()">
            <el-icon class="btn-icon"><Plus /></el-icon>
            商品进库
          </el-button>
        </el-form-item>
      </el-form>

      <div class="table-wrap">
        <el-table
          :data="tableData"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          class="admin-data-table"
        >
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
            min-width="160"
            show-overflow-tooltip
          />
          <el-table-column prop="brand" label="品牌" min-width="100" show-overflow-tooltip />
          <el-table-column
            prop="manufacturer"
            label="生产厂家"
            min-width="140"
            show-overflow-tooltip
          />
          <el-table-column prop="specification" label="规格" min-width="100" show-overflow-tooltip />
          <el-table-column prop="stock" label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <strong :class="'stock-num-' + row.stockStatus">{{ row.stock }}</strong>
            </template>
          </el-table-column>
          <el-table-column prop="warningQuantity" label="预警值" width="88" align="center" />
          <el-table-column prop="sales" label="销量" width="80" align="center" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="stockStatusTag(row.stockStatus)" size="small">
                {{ stockStatusLabel(row.stockStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="320" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" @click="openInbound(row)">进库</el-button>
              <el-button link type="primary" @click="openAdjust(row)">调整库存</el-button>
              <el-button link type="warning" @click="openWarning(row)">预警设置</el-button>
              <el-button link type="info" @click="openLogs(row)">变动记录</el-button>
              <el-button link type="info" @click="openBatches(row)">批次</el-button>
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
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </el-card>

    <el-dialog v-model="inboundVisible" title="商品进库" width="520px" destroy-on-close>
      <el-form :model="inboundForm" label-width="100px">
        <el-form-item label="商品" required>
          <el-select
            v-model="inboundForm.productId"
            filterable
            remote
            reserve-keyword
            placeholder="搜索商品名称"
            :remote-method="searchProductsForInbound"
            :loading="productSearchLoading"
            :disabled="inboundProductLocked"
            style="width: 100%"
          >
            <el-option
              v-for="p in productOptions"
              :key="p.productId"
              :label="productOptionLabel(p)"
              :value="p.productId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进库数量" required>
          <el-input-number v-model="inboundForm.quantity" :min="1" :max="999999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="批号">
          <el-input v-model="inboundForm.batchNumber" placeholder="选填，填写后同步批次库存" clearable />
        </el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker
            v-model="inboundForm.productionDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选填"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="有效期至">
          <el-date-picker
            v-model="inboundForm.expiryDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选填"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="inboundForm.reason"
            type="textarea"
            :rows="2"
            placeholder="采购单号、供应商等"
          />
        </el-form-item>
        <p v-if="inboundPreview" class="inbound-preview">{{ inboundPreview }}</p>
      </el-form>
      <template #footer>
        <el-button @click="inboundVisible = false">取消</el-button>
        <el-button type="success" :loading="submitLoading" @click="submitInbound">确认进库</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustVisible" title="库存调整" width="440px" destroy-on-close>
      <p v-if="currentRow" class="dialog-product">{{ currentRow.productName }}</p>
      <el-form :model="adjustForm" label-width="96px">
        <el-form-item label="调整类型" required>
          <el-radio-group v-model="adjustForm.changeType">
            <el-radio v-for="t in CHANGE_TYPES" :key="t.value" :label="t.value">{{ t.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="adjustQtyLabel" required>
          <el-input-number v-model="adjustForm.quantity" :min="0" :max="999999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAdjust">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="warningVisible" title="库存预警设置" width="400px" destroy-on-close>
      <p v-if="currentRow" class="dialog-product">{{ currentRow.productName }}</p>
      <el-form label-width="96px">
        <el-form-item label="预警阈值" required>
          <el-input-number v-model="warningQty" :min="0" :max="99999" style="width: 100%" />
          <p class="form-hint">库存低于等于该值时将标记为「库存偏低」</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="warningVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitWarning">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logsVisible" :title="logsTitle" width="720px" destroy-on-close>
      <el-table :data="logList" v-loading="logsLoading" border size="small" max-height="400">
        <el-table-column prop="createTime" label="时间" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ changeTypeLabel(row.changeType) }}</template>
        </el-table-column>
        <el-table-column prop="quantityBefore" label="调整前" width="80" align="center" />
        <el-table-column prop="quantityChange" label="变动" width="80" align="center" />
        <el-table-column prop="quantityAfter" label="调整后" width="80" align="center" />
        <el-table-column prop="reason" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
      </el-table>
      <el-pagination
        v-if="logPagination.total > 0"
        class="page-pagination"
        v-model:current-page="logPagination.page"
        v-model:page-size="logPagination.pageSize"
        :total="logPagination.total"
        layout="total, prev, pager, next"
        small
        @current-change="fetchLogs"
      />
    </el-dialog>

    <el-dialog v-model="batchVisible" :title="batchTitle" width="640px" destroy-on-close>
      <el-table :data="batchList" v-loading="batchLoading" border size="small">
        <el-table-column prop="batchNumber" label="批号" min-width="120" />
        <el-table-column prop="productionDate" label="生产日期" width="120" />
        <el-table-column prop="expiryDate" label="有效期至" width="120" />
        <el-table-column prop="stock" label="批次库存" width="100" align="center" />
        <el-table-column prop="lockedStock" label="锁定" width="80" align="center" />
      </el-table>
      <el-empty v-if="!batchLoading && batchList.length === 0" description="暂无批次数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'
import { useTableHeight } from '@/composables/useTableHeight'
import { useListFilter } from '@/composables/useListFilter'
import { resolveProductImageUrl } from '@/utils/productImage'
import {
  getStockSummary,
  getStockList,
  adjustStock,
  inboundStock,
  updateStockWarning,
  getStockLogs,
  getStockBatches
} from '@/api/stock'
import {
  stockStatusLabel,
  stockStatusTag,
  CHANGE_TYPES,
  changeTypeLabel
} from '@/utils/stock'

const { tableHeight } = useTableHeight(380)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const summary = ref({})

const searchForm = reactive({ keyword: '', stockFilter: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const inboundVisible = ref(false)
const inboundProductLocked = ref(false)
const productSearchLoading = ref(false)
const productOptions = ref([])
const inboundForm = reactive({
  productId: null,
  quantity: 1,
  batchNumber: '',
  productionDate: '',
  expiryDate: '',
  reason: ''
})

const adjustVisible = ref(false)
const warningVisible = ref(false)
const logsVisible = ref(false)
const batchVisible = ref(false)
const currentRow = ref(null)
const adjustForm = reactive({ productId: null, changeType: 1, quantity: 1, reason: '' })
const warningQty = ref(10)

const logsLoading = ref(false)
const logList = ref([])
const logPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const logsTitle = computed(() =>
  currentRow.value ? `库存变动记录 · ${currentRow.value.productName}` : '库存变动记录'
)

const batchLoading = ref(false)
const batchList = ref([])
const batchTitle = computed(() =>
  currentRow.value ? `批次库存 · ${currentRow.value.productName}` : '批次库存'
)

const adjustQtyLabel = computed(() => {
  if (adjustForm.changeType === 3) return '目标库存'
  return adjustForm.changeType === 2 ? '出库数量' : '入库数量'
})

const inboundPreview = computed(() => {
  const p = productOptions.value.find((x) => x.productId === inboundForm.productId)
  if (!p || !inboundForm.quantity) return ''
  const after = (p.stock ?? 0) + inboundForm.quantity
  return `进库后库存：${p.stock ?? 0} → ${after}`
})

onMounted(() => {
  fetchSummary()
  fetchList()
})

async function fetchSummary() {
  try {
    summary.value = (await getStockSummary()) || {}
  } catch {
    summary.value = {}
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getStockList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      stockFilter: searchForm.stockFilter ?? undefined
    })
    tableData.value = res?.list ?? []
    pagination.total = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
  fetchSummary()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.stockFilter = null
  handleSearch()
}

useListFilter({
  searchForm,
  autoKeys: ['stockFilter'],
  onSearch: handleSearch
})

function productOptionLabel(p) {
  const parts = [p.productName]
  if (p.manufacturer) parts.push(p.manufacturer)
  if (p.specification) parts.push(p.specification)
  parts.push(`库存 ${p.stock ?? 0}`)
  return parts.join(' · ')
}

function resetInboundForm() {
  inboundForm.productId = null
  inboundForm.quantity = 1
  inboundForm.batchNumber = ''
  inboundForm.productionDate = ''
  inboundForm.expiryDate = ''
  inboundForm.reason = ''
}

function openInbound(row) {
  resetInboundForm()
  inboundProductLocked.value = !!row
  if (row) {
    inboundForm.productId = row.productId
    productOptions.value = [row]
  } else {
    productOptions.value = [...tableData.value]
  }
  inboundVisible.value = true
}

async function searchProductsForInbound(keyword) {
  if (!keyword) {
    productOptions.value = [...tableData.value]
    return
  }
  productSearchLoading.value = true
  try {
    const res = await getStockList({ keyword, page: 1, pageSize: 50 })
    productOptions.value = res?.list ?? []
  } finally {
    productSearchLoading.value = false
  }
}

async function submitInbound() {
  if (!inboundForm.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  if (!inboundForm.quantity || inboundForm.quantity < 1) {
    ElMessage.warning('进库数量至少为 1')
    return
  }
  submitLoading.value = true
  try {
    await inboundStock({
      productId: inboundForm.productId,
      quantity: inboundForm.quantity,
      batchNumber: inboundForm.batchNumber || undefined,
      productionDate: inboundForm.productionDate || undefined,
      expiryDate: inboundForm.expiryDate || undefined,
      reason: inboundForm.reason || undefined
    })
    ElMessage.success('进库成功')
    inboundVisible.value = false
    fetchList()
    fetchSummary()
  } finally {
    submitLoading.value = false
  }
}

function openAdjust(row) {
  currentRow.value = row
  adjustForm.productId = row.productId
  adjustForm.changeType = 1
  adjustForm.quantity = 1
  adjustForm.reason = ''
  adjustVisible.value = true
}

function openWarning(row) {
  currentRow.value = row
  warningQty.value = row.warningQuantity ?? 10
  warningVisible.value = true
}

function openLogs(row) {
  currentRow.value = row
  logPagination.page = 1
  logsVisible.value = true
  fetchLogs()
}

function openBatches(row) {
  currentRow.value = row
  batchVisible.value = true
  fetchBatches()
}

async function submitAdjust() {
  if (!adjustForm.productId) return
  submitLoading.value = true
  try {
    await adjustStock({ ...adjustForm })
    ElMessage.success('库存已更新')
    adjustVisible.value = false
    fetchList()
    fetchSummary()
  } finally {
    submitLoading.value = false
  }
}

async function submitWarning() {
  if (!currentRow.value) return
  submitLoading.value = true
  try {
    await updateStockWarning({
      productId: currentRow.value.productId,
      warningQuantity: warningQty.value
    })
    ElMessage.success('预警值已保存')
    warningVisible.value = false
    fetchList()
    fetchSummary()
  } finally {
    submitLoading.value = false
  }
}

async function fetchLogs() {
  if (!currentRow.value) return
  logsLoading.value = true
  try {
    const res = await getStockLogs(currentRow.value.productId, {
      page: logPagination.page,
      pageSize: logPagination.pageSize
    })
    logList.value = res?.list ?? []
    logPagination.total = res?.total ?? 0
  } finally {
    logsLoading.value = false
  }
}

async function fetchBatches() {
  if (!currentRow.value) return
  batchLoading.value = true
  try {
    batchList.value = (await getStockBatches(currentRow.value.productId)) || []
  } finally {
    batchLoading.value = false
  }
}
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}

.summary-card {
  text-align: center;
  border-radius: 12px;
}

.summary-card.warn :deep(.el-card__body) {
  background: linear-gradient(135deg, #fffbeb, #fff);
}

.summary-card.danger :deep(.el-card__body) {
  background: linear-gradient(135deg, #fef2f2, #fff);
}

.summary-label {
  margin: 0 0 8px;
  font-size: 13px;
  color: #64748b;
}

.summary-value {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.search-form {
  margin-bottom: 10px;
}

.stock-num-OUT {
  color: #dc2626;
}

.stock-num-LOW {
  color: #d97706;
}

.dialog-product {
  margin: 0 0 16px;
  font-weight: 600;
  color: #334155;
}

.form-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.btn-icon {
  margin-right: 4px;
  vertical-align: -2px;
}

.inbound-preview {
  margin: 0;
  padding: 10px 12px;
  font-size: 13px;
  color: #059669;
  background: #ecfdf5;
  border-radius: 8px;
}
</style>
