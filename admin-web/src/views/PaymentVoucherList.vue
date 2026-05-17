<template>
  <div class="payment-voucher-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" class="search-form">
        <el-form-item>
          <el-button type="primary" @click="fetchData">刷新</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="paymentNo" label="支付流水号" min-width="160" />
        <el-table-column prop="orderId" label="订单ID" width="90" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="voucherUrl" label="凭证" min-width="200" show-overflow-tooltip />
        <el-table-column prop="transferRemark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="review(row, true)">通过</el-button>
            <el-button link type="danger" @click="review(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="page-pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingVouchers, reviewVoucher } from '@/api/admin/payment'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const res = await getPendingVouchers({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

async function review(row, approved) {
  let rejectReason = ''
  if (!approved) {
    const { value } = await ElMessageBox.prompt('驳回原因', '审核驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    rejectReason = value
  } else {
    await ElMessageBox.confirm('确认通过该付款凭证？', '审核通过', { type: 'warning' })
  }
  await reviewVoucher(row.id, { approved, rejectReason })
  ElMessage.success(approved ? '已通过' : '已驳回')
  fetchData()
}
</script>

<style scoped>
.search-form { margin-bottom: 12px; }
</style>
