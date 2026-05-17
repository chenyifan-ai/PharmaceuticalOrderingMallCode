<template>
  <div class="order-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="订单号/收货人/手机号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="待付款" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="已发货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-wrap">
      <el-table :data="tableData" v-loading="loading" border stripe :height="tableHeight" class="admin-data-table">
        <el-table-column prop="orderNo" label="订单号" :min-width="colWidths.orderNo" show-overflow-tooltip />
        <el-table-column prop="receiverName" label="收货人" :min-width="colWidths.receiverName" show-overflow-tooltip />
        <el-table-column prop="receiverPhone" label="联系电话" min-width="150" show-overflow-tooltip />
        <el-table-column prop="payAmount" label="实付金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column prop="orderType" label="处方类型" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.orderType === 1 ? 'success' : 'danger'" size="small">
              {{ row.orderType === 1 ? 'OTC' : '处方药' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发票" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="hasInvoiceInfo(row)">
              <el-tag
                v-if="row.invoice?.status != null"
                :type="invoiceStatusTag(row.invoice.status)"
                size="small"
                class="invoice-tag"
              >
                {{ invoiceStatusLabel(row.invoice.status) }}
              </el-tag>
              <el-tag
                v-else-if="row.invoiceStatus != null"
                :type="orderInvoiceStatusTag(row.invoiceStatus)"
                size="small"
                class="invoice-tag"
              >
                {{ orderInvoiceStatusLabel(row.invoiceStatus) }}
              </el-tag>
              <span class="invoice-brief">{{ invoiceSummary(row) }}</span>
            </template>
            <span v-else class="invoice-muted">未申请</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" :min-width="colWidths.createTime">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" :min-width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 2" link type="success" @click="openShipDialog(row)">发货</el-button>
            <el-button
              v-if="[0, 1, 2].includes(row.status)"
              link
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button link type="info" @click="handleViewHistory(row)">状态记录</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="800px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(currentOrder.status)" size="small">
              {{ getStatusName(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="商品总额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="运费">¥{{ currentOrder.freight || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.logisticsNo" label="物流" :span="2">
            {{ currentOrder.logisticsCompany }} - {{ currentOrder.logisticsNo }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.remark" label="备注" :span="2">
            {{ currentOrder.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <OrderInvoiceSection :order="currentOrder" :column="2" class="detail-invoice" />

        <h4 style="margin: 16px 0 8px;">商品明细</h4>
        <el-table :data="currentOrder.orderItems || []" border size="small">
          <el-table-column prop="productName" label="商品" min-width="140" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" width="100" />
          <el-table-column prop="price" label="单价" width="90">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="subtotal" label="小计" width="90">
            <template #default="{ row }">¥{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <el-dialog v-model="shipVisible" title="订单发货" width="420px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="物流公司" required>
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择" style="width: 100%">
            <el-option label="顺丰快递" value="顺丰快递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通快递" value="圆通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="EMS" value="EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="运单号" required>
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入运单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="handleConfirmShip">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyVisible" title="订单状态记录" width="560px">
      <el-timeline v-if="statusHistory.length">
        <el-timeline-item
          v-for="log in statusHistory"
          :key="log.id"
          :timestamp="formatDateTime(log.createTime)"
          placement="top"
        >
          {{ formatStatusTransition(log) }}
          <span v-if="log.remark && log.oldStatus !== log.newStatus" class="log-remark">（{{ log.remark }}）</span>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无状态记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { formatDateTime } from '@/utils/format'
import { useTableHeight } from '@/composables/useTableHeight'
import { useListFilter } from '@/composables/useListFilter'
import { estimateColMinWidth, maxCellSample } from '@/utils/table'

const { tableHeight } = useTableHeight(300)
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminOrderList,
  getAdminOrderDetail,
  adminShipOrder,
  adminCancelOrder,
  getOrderStatusHistory
} from '@/api/admin'
import OrderInvoiceSection from '@/components/OrderInvoiceSection.vue'
import {
  hasInvoiceInfo,
  invoiceSummary,
  invoiceStatusLabel,
  invoiceStatusTag,
  orderInvoiceStatusLabel,
  orderInvoiceStatusTag
} from '@/utils/invoice'

const loading = ref(false)
const shipLoading = ref(false)
const detailVisible = ref(false)
const shipVisible = ref(false)
const historyVisible = ref(false)
const currentOrder = ref(null)
const statusHistory = ref([])

const searchForm = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])

const colWidths = computed(() => ({
  orderNo: estimateColMinWidth('订单号', maxCellSample(tableData.value, 'orderNo'), { min: 140 }),
  receiverName: estimateColMinWidth('收货人', maxCellSample(tableData.value, 'receiverName'), { min: 88 }),
  createTime: estimateColMinWidth('下单时间', '2026-05-17 12:00:00', { min: 160 })
}))

const shipForm = reactive({
  orderId: null,
  logisticsCompany: '',
  logisticsNo: ''
})

onMounted(() => fetchData())

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAdminOrderList({
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

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

useListFilter({
  searchForm,
  autoKeys: ['status'],
  onSearch: handleSearch
})

function formatStatusTransition(log) {
  if (log.remark && log.oldStatus === log.newStatus) {
    return log.remark
  }
  return `${getStatusName(log.oldStatus)} → ${getStatusName(log.newStatus)}`
}

const handleReset = () => {
  Object.assign(searchForm, { keyword: '', status: null })
  handleSearch()
}

const handleViewDetail = async (row) => {
  currentOrder.value = await getAdminOrderDetail(row.id)
  detailVisible.value = true
}

const openShipDialog = (row) => {
  shipForm.orderId = row.id
  shipForm.logisticsCompany = ''
  shipForm.logisticsNo = ''
  shipVisible.value = true
}

const handleConfirmShip = async () => {
  if (!shipForm.logisticsCompany || !shipForm.logisticsNo) {
    ElMessage.warning('请填写完整物流信息')
    return
  }
  shipLoading.value = true
  try {
    await adminShipOrder(shipForm.orderId, {
      logisticsCompany: shipForm.logisticsCompany,
      logisticsNo: shipForm.logisticsNo
    })
    ElMessage.success('发货成功')
    shipVisible.value = false
    fetchData()
  } finally {
    shipLoading.value = false
  }
}

const handleCancel = (row) => {
  ElMessageBox.prompt('请输入取消原因（可选）', '取消订单', {
    confirmButtonText: '确定取消',
    cancelButtonText: '返回',
    inputPlaceholder: '取消原因',
    type: 'warning'
  })
    .then(async ({ value }) => {
      await adminCancelOrder(row.id, { reason: value || '管理员取消' })
      ElMessage.success('订单已取消')
      fetchData()
    })
    .catch(() => {})
}

const handleViewHistory = async (row) => {
  try {
    statusHistory.value = await getOrderStatusHistory(row.id) || []
  } catch {
    statusHistory.value = []
  }
  historyVisible.value = true
}

const getStatusName = (status) =>
  ({
    0: '待付款',
    1: '待审核',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '退款中',
    7: '已退款'
  }[status] || '未知')

const getStatusTag = (status) =>
  ({
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: '',
    5: 'info',
    6: 'warning',
    7: 'danger'
  }[status] || '')
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}

.log-remark {
  color: #909399;
  font-size: 12px;
}

.invoice-tag {
  margin-right: 6px;
  vertical-align: middle;
}

.invoice-brief {
  font-size: 12px;
  color: #606266;
}

.invoice-muted {
  font-size: 12px;
  color: #c0c4cc;
}

.detail-invoice {
  margin-top: 16px;
}
</style>
