<template>
  <div class="consumer-page c-prescriptions">
    <div class="consumer-container">
      <header class="consumer-page-header">
        <h2>我的处方</h2>
        <p class="subtitle">查看处方审核状态与关联订单</p>
      </header>

      <el-card v-loading="loading" class="consumer-card">
        <el-table :data="list" border stripe>
          <el-table-column prop="prescriptionNo" label="处方编号" min-width="160" />
          <el-table-column prop="auditStatus" label="审核状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.auditStatus)" size="small">{{ statusLabel(row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderId" label="关联订单" width="100">
            <template #default="{ row }">{{ row.orderId || '—' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="page-pagination"
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </el-card>
    </div>

    <el-dialog v-model="detailVisible" title="处方详情" width="560px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="处方编号">{{ current.prescriptionNo }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="statusTag(current.auditStatus)" size="small">{{ statusLabel(current.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="current.auditRemark" label="审核备注">{{ current.auditRemark }}</el-descriptions-item>
        <el-descriptions-item label="处方图片">
          <img v-if="imageUrl" :src="imageUrl" class="rx-img" alt="处方" />
          <span v-else>—</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPrescriptionList, getPrescriptionDetail } from '@/api/consumer/prescription'
import { formatDateTime } from '@/utils/format'
import { resolveProductImageUrl } from '@/utils/productImage'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const current = ref(null)

const imageUrl = computed(() => {
  if (!current.value?.imageUrls) return ''
  try {
    const arr = JSON.parse(current.value.imageUrls)
    return resolveProductImageUrl(Array.isArray(arr) ? arr[0] : current.value.imageUrls)
  } catch {
    return resolveProductImageUrl(current.value.imageUrls)
  }
})

function statusLabel(s) {
  return { 0: '待审核', 1: '已通过', 2: '已驳回' }[s] ?? '未知'
}
function statusTag(s) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[s] ?? 'info'
}

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getPrescriptionList({ page: page.value, pageSize: pageSize.value })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  current.value = await getPrescriptionDetail(row.id)
  detailVisible.value = true
}
</script>

<style scoped>
.c-prescriptions .page-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.rx-img {
  max-width: 100%;
  max-height: 320px;
  border-radius: 8px;
}
</style>
