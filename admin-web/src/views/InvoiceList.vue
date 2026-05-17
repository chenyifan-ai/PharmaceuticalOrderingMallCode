<template>
  <div class="invoice-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="订单号/抬头/发票号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="发票状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px">
            <el-option label="待开票" :value="0" />
            <el-option label="已开票" :value="1" />
            <el-option label="已寄送" :value="2" />
            <el-option label="已作废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
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
          <el-table-column
            prop="invoiceNo"
            label="发票号"
            :min-width="colWidths.invoiceNo"
            show-overflow-tooltip
          />
          <el-table-column
            prop="orderNo"
            label="订单号"
            :min-width="colWidths.orderNo"
            show-overflow-tooltip
          />
          <el-table-column
            prop="invoiceTitle"
            label="发票抬头"
            :min-width="colWidths.invoiceTitle"
            show-overflow-tooltip
          />
          <el-table-column prop="amount" label="发票金额" :min-width="100">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="invoiceType" label="类型" :min-width="88">
            <template #default="{ row }">
              <el-tag :type="row.invoiceType === 1 ? 'success' : 'warning'" size="small">
                {{ row.invoiceType === 1 ? '个人' : '企业' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" :min-width="96">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" :min-width="colWidths.createTime">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" :min-width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
              <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
              <el-button v-if="row.status === 0" link type="success" @click="handleIssue(row)">开票</el-button>
              <el-button v-if="row.status === 1" link type="warning" @click="handleSend(row)">寄送</el-button>
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

    <el-dialog v-model="previewVisible" title="发票预览" width="720px" destroy-on-close>
      <InvoicePreview :invoice="previewInvoice" />
    </el-dialog>

    <el-dialog v-model="detailVisible" title="发票详情" width="600px">
      <el-descriptions :column="2" border v-if="currentInvoice">
        <el-descriptions-item label="发票号">{{ currentInvoice.invoiceNo || '待开具' }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentInvoice.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头">{{ currentInvoice.invoiceTitle }}</el-descriptions-item>
        <el-descriptions-item label="纳税人识别号">{{ currentInvoice.taxNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">
          {{ currentInvoice.invoiceType === 1 ? '个人' : '企业' }}
        </el-descriptions-item>
        <el-descriptions-item label="发票内容">{{ currentInvoice.invoiceContent }}</el-descriptions-item>
        <el-descriptions-item label="发票金额">¥{{ currentInvoice.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentInvoice.status)">
            {{ getStatusName(currentInvoice.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收件人">{{ currentInvoice.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentInvoice.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收件地址" :span="2">{{ currentInvoice.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item v-if="currentInvoice.logisticsCompany" label="物流公司">
          {{ currentInvoice.logisticsCompany }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentInvoice.logisticsNo" label="物流单号">
          {{ currentInvoice.logisticsNo }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="sendVisible" title="寄送发票" width="400px">
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="物流公司">
          <el-select v-model="sendForm.logisticsCompany" placeholder="请选择" style="width: 100%">
            <el-option label="顺丰快递" value="顺丰快递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通快递" value="圆通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="EMS" value="EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="sendForm.logisticsNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSend" :loading="sendLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { formatDateTime } from '@/utils/format'
import { useTableHeight } from '@/composables/useTableHeight'
import { useListFilter } from '@/composables/useListFilter'
import { estimateColMinWidth, maxCellSample } from '@/utils/table'
import { ElMessage, ElMessageBox } from 'element-plus'
import InvoicePreview from '@/components/invoice/InvoicePreview.vue'
import { getAdminInvoices, issueInvoice, sendInvoice } from '@/api/invoice'

const { tableHeight } = useTableHeight(300)

const loading = ref(false)
const sendLoading = ref(false)
const detailVisible = ref(false)
const previewVisible = ref(false)
const sendVisible = ref(false)
const currentInvoice = ref(null)
const previewInvoice = ref(null)

const searchForm = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])

const sendForm = reactive({
  invoiceId: null,
  logisticsCompany: '',
  logisticsNo: ''
})

const colWidths = computed(() => ({
  invoiceNo: estimateColMinWidth('发票号', maxCellSample(tableData.value, 'invoiceNo'), { min: 120 }),
  orderNo: estimateColMinWidth('订单号', maxCellSample(tableData.value, 'orderNo'), { min: 140 }),
  invoiceTitle: estimateColMinWidth('发票抬头', maxCellSample(tableData.value, 'invoiceTitle'), { min: 120 }),
  createTime: estimateColMinWidth('申请时间', '2026-05-17 12:00:00', { min: 160 })
}))

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminInvoices({
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

function handlePreview(row) {
  previewInvoice.value = row
  previewVisible.value = true
}

function handleViewDetail(row) {
  currentInvoice.value = row
  detailVisible.value = true
}

function handleIssue(row) {
  ElMessageBox.confirm(`确定为订单 ${row.orderNo} 开具发票吗？`, '开票确认', { type: 'warning' })
    .then(async () => {
      await issueInvoice(row.id)
      ElMessage.success('开票成功')
      fetchData()
    })
    .catch(() => {})
}

function handleSend(row) {
  sendForm.invoiceId = row.id
  sendForm.logisticsCompany = ''
  sendForm.logisticsNo = ''
  sendVisible.value = true
}

async function handleConfirmSend() {
  if (!sendForm.logisticsCompany || !sendForm.logisticsNo) {
    ElMessage.warning('请填写完整的物流信息')
    return
  }
  sendLoading.value = true
  try {
    await sendInvoice(sendForm.invoiceId, {
      logisticsCompany: sendForm.logisticsCompany,
      logisticsNo: sendForm.logisticsNo
    })
    ElMessage.success('寄送成功')
    sendVisible.value = false
    fetchData()
  } finally {
    sendLoading.value = false
  }
}

function getStatusName(status) {
  return { 0: '待开票', 1: '已开票', 2: '已寄送', 3: '已作废' }[status] || '未知'
}

function getStatusTag(status) {
  return { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info' }[status] || ''
}
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}
</style>
