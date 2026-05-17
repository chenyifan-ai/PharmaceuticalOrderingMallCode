<template>
  <div class="settlement-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商">
          <el-select
            v-model="searchForm.merchantId"
            placeholder="全部"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="m in merchantOptions"
              :key="m.id"
              :label="m.merchantName"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待结算" :value="0" />
            <el-option label="已结算" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openGenerate">生成结算单</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="settlementNo" label="结算单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="供应商" min-width="140" show-overflow-tooltip />
        <el-table-column label="结算周期" min-width="200">
          <template #default="{ row }">
            {{ row.periodStart || '-' }} ~ {{ row.periodEnd || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="90" align="center" />
        <el-table-column prop="totalAmount" label="订单总额" width="110">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="platformFee" label="平台服务费" width="110">
          <template #default="{ row }">¥{{ row.platformFee }}</template>
        </el-table-column>
        <el-table-column prop="settleAmount" label="应结金额" width="110">
          <template #default="{ row }">
            <span class="settle-amount">¥{{ row.settleAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已结算' : '待结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              link
              type="primary"
              @click="handleConfirm(row)"
            >
              确认结算
            </el-button>
            <span v-else class="text-muted">{{ formatDateTime(row.settleTime) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="page-pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="generateVisible" title="生成结算单" width="480px" destroy-on-close>
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="100px">
        <el-form-item label="供应商" prop="merchantId">
          <el-select v-model="generateForm.merchantId" placeholder="请选择" filterable style="width: 100%">
            <el-option
              v-for="m in merchantOptions"
              :key="m.id"
              :label="m.merchantName"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="periodStart">
          <el-date-picker
            v-model="generateForm.periodStart"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选填"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="periodEnd">
          <el-date-picker
            v-model="generateForm.periodEnd"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选填"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" :loading="generateLoading" @click="submitGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { getSettlementList, generateSettlement, confirmSettlement } from '@/api/admin/settlement'
import { getMerchantList } from '@/api/merchant'

const loading = ref(false)
const tableData = ref([])
const merchantOptions = ref([])
const searchForm = reactive({ merchantId: null, status: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const generateVisible = ref(false)
const generateLoading = ref(false)
const generateFormRef = ref()
const generateForm = reactive({
  merchantId: null,
  periodStart: '',
  periodEnd: ''
})
const generateRules = {
  merchantId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

onMounted(() => {
  loadMerchants()
  fetchData()
})

async function loadMerchants() {
  const res = await getMerchantList({ page: 1, pageSize: 500, status: 1 })
  merchantOptions.value = res.list || []
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.merchantId = null
  searchForm.status = null
  pagination.page = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchForm.merchantId) params.merchantId = searchForm.merchantId
    if (searchForm.status != null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    const res = await getSettlementList(params)
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function openGenerate() {
  generateForm.merchantId = null
  generateForm.periodStart = ''
  generateForm.periodEnd = ''
  generateVisible.value = true
}

async function submitGenerate() {
  await generateFormRef.value.validate()
  generateLoading.value = true
  try {
    const payload = { merchantId: generateForm.merchantId }
    if (generateForm.periodStart) payload.periodStart = generateForm.periodStart
    if (generateForm.periodEnd) payload.periodEnd = generateForm.periodEnd
    await generateSettlement(payload)
    ElMessage.success('结算单已生成')
    generateVisible.value = false
    fetchData()
  } finally {
    generateLoading.value = false
  }
}

async function handleConfirm(row) {
  await ElMessageBox.confirm(
    `确认向供应商「${row.merchantName || row.merchantId}」结算 ¥${row.settleAmount}？`,
    '确认结算',
    { type: 'warning' }
  )
  await confirmSettlement(row.id)
  ElMessage.success('已确认结算')
  fetchData()
}
</script>

<style scoped>
.search-form {
  margin-bottom: 12px;
}
.settle-amount {
  color: #e6a23c;
  font-weight: 600;
}
.text-muted {
  color: #909399;
  font-size: 12px;
}
</style>
