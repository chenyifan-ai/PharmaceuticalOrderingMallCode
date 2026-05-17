<template>
  <div class="refund-audit-list page-container">
    <el-card class="page-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审核" name="6" />
        <el-tab-pane label="已退款" name="7" />
      </el-tabs>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item v-if="isAdmin" label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="订单号/收货人/手机号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="160" show-overflow-tooltip />
        <el-table-column v-if="isAdmin" prop="merchantName" label="供应商" min-width="120" show-overflow-tooltip />
        <el-table-column prop="receiverName" label="收货人" min-width="90" />
        <el-table-column prop="receiverPhone" label="联系电话" min-width="120" />
        <el-table-column prop="payAmount" label="实付金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 6">
              <el-button link type="success" @click="handleAudit(row, true)">同意退款</el-button>
              <el-button link type="danger" @click="handleAudit(row, false)">驳回</el-button>
            </template>
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

    <el-dialog v-model="detailVisible" title="退款申请详情" width="720px" destroy-on-close>
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(currentOrder.status)" size="small">
              {{ statusLabel(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ refundReason }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentOrder.orderItems?.length" class="order-items">
          <h4>商品明细</h4>
          <el-table :data="currentOrder.orderItems" border size="small">
            <el-table-column prop="productName" label="商品" min-width="160" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="price" label="单价" width="90">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="currentOrder.status === 6" class="dialog-actions">
          <el-button type="success" @click="handleAudit(currentOrder, true)">同意退款</el-button>
          <el-button type="danger" @click="handleAudit(currentOrder, false)">驳回</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { isAdminUser } from '@/utils/consumer'
import { formatDateTime } from '@/utils/format'
import {
  getAdminOrderList,
  getAdminOrderDetail,
  getOrderStatusHistory,
  processAdminRefund
} from '@/api/admin'
import { getMerchantOrders, processMerchantRefund } from '@/api/order'

const userStore = useUserStore()
const isAdmin = computed(() => isAdminUser(userStore.userInfo?.userType))

const REFUND_STATUS = {
  6: { label: '退款中', tag: 'warning' },
  7: { label: '已退款', tag: 'success' }
}

function statusLabel(s) {
  return REFUND_STATUS[s]?.label ?? '未知'
}
function statusTag(s) {
  return REFUND_STATUS[s]?.tag ?? 'info'
}

const loading = ref(false)
const tableData = ref([])
const activeTab = ref('6')
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const detailVisible = ref(false)
const currentOrder = ref(null)
const refundReason = ref('-')

onMounted(fetchData)

function handleTabChange() {
  pagination.page = 1
  fetchData()
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  pagination.page = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const status = Number(activeTab.value)
    const params = { page: pagination.page, pageSize: pagination.pageSize, status }
    if (isAdmin.value && searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const res = isAdmin.value
      ? await getAdminOrderList(params)
      : await getMerchantOrders(params)
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadRefundReason(orderId) {
  try {
    const history = await getOrderStatusHistory(orderId)
    const log = (history || []).find((h) => h.newStatus === 6)
    refundReason.value = log?.remark || '-'
  } catch {
    refundReason.value = '-'
  }
}

async function openDetail(row) {
  if (isAdmin.value) {
    currentOrder.value = await getAdminOrderDetail(row.id)
  } else {
    currentOrder.value = { ...row }
  }
  await loadRefundReason(row.id)
  detailVisible.value = true
}

async function handleAudit(row, approved) {
  let remark = ''
  if (!approved) {
    const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请填写驳回原因'
    })
    remark = value
  } else {
    await ElMessageBox.confirm('确认同意退款？款项将按支付渠道原路退回。', '同意退款', {
      type: 'warning'
    })
  }
  if (isAdmin.value) {
    await processAdminRefund(row.id, { approved, remark })
  } else {
    await processMerchantRefund(row.id, { approved, remark })
  }
  ElMessage.success(approved ? '已同意退款' : '已驳回')
  detailVisible.value = false
  fetchData()
}
</script>

<style scoped>
.search-form {
  margin-bottom: 12px;
}
.order-items {
  margin-top: 16px;
}
.order-items h4 {
  margin: 0 0 8px;
  font-size: 14px;
}
.dialog-actions {
  margin-top: 16px;
  text-align: right;
}
</style>
