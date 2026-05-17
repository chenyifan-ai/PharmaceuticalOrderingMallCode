<template>
  <div class="qualification-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
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
          <el-table-column prop="id" label="ID" :min-width="64" />
          <el-table-column
            prop="companyName"
            label="企业名称"
            :min-width="colWidths.companyName"
            show-overflow-tooltip
          />
          <el-table-column
            prop="creditCode"
            label="统一社会信用代码"
            :min-width="colWidths.creditCode"
            show-overflow-tooltip
          />
          <el-table-column prop="legalPerson" label="法人" :min-width="88" show-overflow-tooltip />
          <el-table-column prop="qualificationStatus" label="状态" :min-width="96">
            <template #default="{ row }">
              <el-tag :type="qualificationStatusTag(row.qualificationStatus)" size="small">
                {{ qualificationStatusLabel(row.qualificationStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="qualificationExpireDate" label="到期日" :min-width="110" />
          <el-table-column prop="createTime" label="提交时间" :min-width="colWidths.createTime">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" :min-width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
              <template v-if="row.qualificationStatus === 0">
                <el-button link type="success" @click="handleAudit(row, 1)">通过</el-button>
                <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
              </template>
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

    <el-dialog v-model="detailVisible" title="企业资质审核" width="820px" destroy-on-close>
      <template v-if="currentQualification">
        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="提交人">
            {{ currentQualification.submitterNickname || currentQualification.submitterRealName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentQualification.submitterPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="企业名称">{{ currentQualification.companyName }}</el-descriptions-item>
          <el-descriptions-item label="信用代码">{{ currentQualification.creditCode }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ currentQualification.legalPerson }}</el-descriptions-item>
          <el-descriptions-item label="法人身份证">{{ currentQualification.legalPersonIdCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="资质到期">{{ currentQualification.qualificationExpireDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="qualificationStatusTag(currentQualification.qualificationStatus)">
              {{ qualificationStatusLabel(currentQualification.qualificationStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(currentQualification.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentQualification.reviewTime" label="审核时间">
            {{ formatDateTime(currentQualification.reviewTime) }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentQualification.qualificationRejectReason"
            label="驳回原因"
            :span="2"
          >
            {{ currentQualification.qualificationRejectReason }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="cert-title">资质证照</h4>
        <div class="cert-grid">
          <div v-for="cert in certList" :key="cert.key" class="cert-item">
            <span class="cert-label">{{ cert.label }}</span>
            <el-image
              v-if="cert.url"
              :src="cert.url"
              fit="contain"
              class="cert-img"
              :preview-src-list="certPreviewList"
              :initial-index="cert.index"
            />
            <span v-else class="cert-empty">未上传</span>
          </div>
        </div>
      </template>
      <template v-if="currentQualification?.qualificationStatus === 0" #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleAudit(currentQualification, 1); detailVisible = false">
          审核通过
        </el-button>
        <el-button type="danger" @click="openRejectFromDetail">驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回资质" width="440px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请填写驳回原因，将展示给申请方" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="handleConfirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useListFilter } from '@/composables/useListFilter'
import { estimateColMinWidth, maxCellSample } from '@/utils/table'
import { formatDateTime } from '@/utils/format'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminQualificationList, getAdminQualificationDetail, reviewQualification } from '@/api/admin'
import {
  qualificationStatusLabel,
  qualificationStatusTag
} from '@/utils/qualification'

const { tableHeight } = useTableHeight(290)

const loading = ref(false)
const rejectLoading = ref(false)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentQualification = ref(null)
const currentRejectId = ref(null)

const searchForm = reactive({ status: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])
const rejectForm = reactive({ reason: '' })

const colWidths = computed(() => ({
  companyName: estimateColMinWidth('企业名称', maxCellSample(tableData.value, 'companyName'), { min: 140 }),
  creditCode: estimateColMinWidth('统一社会信用代码', maxCellSample(tableData.value, 'creditCode'), { min: 180 }),
  createTime: estimateColMinWidth('提交时间', '2026-05-17 12:00:00', { min: 160 })
}))

const certList = computed(() => {
  const q = currentQualification.value
  if (!q) return []
  const items = [
    { key: 'bl', label: '营业执照', url: q.businessLicenseUrl },
    { key: 'drug', label: '药品经营许可证', url: q.drugOperationPermitUrl },
    { key: 'md', label: '医疗器械经营许可证', url: q.medicalDevicePermitUrl },
    { key: 'gsp', label: 'GSP认证', url: q.gspCertificateUrl }
  ]
  let idx = 0
  return items.map((item) => ({
    ...item,
    index: item.url ? idx++ : -1
  }))
})

const certPreviewList = computed(() => certList.value.filter((c) => c.url).map((c) => c.url))

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminQualificationList({
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

useListFilter({ searchForm, autoKeys: ['status'], onSearch: handleSearch })

function handleReset() {
  searchForm.status = null
  handleSearch()
}

async function handleViewDetail(row) {
  currentQualification.value = await getAdminQualificationDetail(row.id)
  detailVisible.value = true
}

function handleAudit(row, status) {
  ElMessageBox.confirm('确认通过该企业资质审核？通过后将更新用户认证状态。', '审核通过', { type: 'warning' })
    .then(async () => {
      await reviewQualification(row.id, status)
      ElMessage.success('审核通过')
      fetchData()
    })
    .catch(() => {})
}

function handleReject(row) {
  currentRejectId.value = row.id
  rejectForm.reason = ''
  rejectVisible.value = true
}

function openRejectFromDetail() {
  if (currentQualification.value) {
    handleReject(currentQualification.value)
  }
}

async function handleConfirmReject() {
  if (!rejectForm.reason?.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  rejectLoading.value = true
  try {
    await reviewQualification(currentRejectId.value, 2, rejectForm.reason.trim())
    ElMessage.success('已驳回')
    rejectVisible.value = false
    detailVisible.value = false
    fetchData()
  } finally {
    rejectLoading.value = false
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}

.detail-desc {
  margin-bottom: 16px;
}

.cert-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
}

.cert-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.cert-item {
  text-align: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.cert-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.cert-img {
  width: 100%;
  max-height: 200px;
  border-radius: 6px;
}

.cert-empty {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
