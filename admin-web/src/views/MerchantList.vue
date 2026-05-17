<template>
  <div class="merchant-list page-container">
    <el-card class="page-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="商家名/法人/电话"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openFormDialog()">新增供应商</el-button>
        </el-form-item>
      </el-form>

      <div class="table-wrap">
      <el-table :data="tableData" v-loading="loading" border stripe :height="tableHeight" class="admin-data-table">
        <el-table-column prop="id" label="ID" :min-width="64" />
        <el-table-column
          prop="merchantName"
          label="商家名称"
          :min-width="colWidths.merchantName"
          show-overflow-tooltip
        />
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="150" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="130">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="90" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusTag(row.auditStatus)" size="small">
              {{ getAuditStatusName(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openFormDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <template v-if="row.auditStatus === 0">
              <el-button link type="success" @click="quickAudit(row, 1)">通过</el-button>
              <el-button link type="danger" @click="handleAudit(row)">驳回</el-button>
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

    <el-dialog v-model="detailVisible" title="供应商详情" width="700px">
      <el-descriptions v-if="currentMerchant" :column="2" border>
        <el-descriptions-item label="商家名称">{{ currentMerchant.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusTag(currentMerchant.auditStatus)" size="small">
            {{ getAuditStatusName(currentMerchant.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="法人">{{ currentMerchant.legalPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号">{{ currentMerchant.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ currentMerchant.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="店铺地址" :span="2">{{ currentMerchant.shopAddress }}</el-descriptions-item>
        <el-descriptions-item label="经营范围" :span="2">{{ currentMerchant.businessScope }}</el-descriptions-item>
        <el-descriptions-item label="店铺描述" :span="2">{{ currentMerchant.description }}</el-descriptions-item>
        <el-descriptions-item label="保证金">¥{{ currentMerchant.deposit }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ currentMerchant.rating }}</el-descriptions-item>
        <el-descriptions-item label="销量">{{ currentMerchant.salesCount }}</el-descriptions-item>
        <el-descriptions-item label="结算账户">
          {{ currentMerchant.settlementAccountType === 1 ? '银行卡' : '支付宝' }}
        </el-descriptions-item>
        <el-descriptions-item label="开户行">{{ currentMerchant.bankName }}</el-descriptions-item>
        <el-descriptions-item label="账户名">{{ currentMerchant.settlementAccountName }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ currentMerchant.settlementAccountNo }}</el-descriptions-item>
        <el-descriptions-item v-if="currentMerchant.auditRemark" label="审核备注" :span="2">
          {{ currentMerchant.auditRemark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑供应商' : '新增供应商'"
      width="620px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item v-if="!form.id" label="关联账号" prop="userId">
          <el-select v-model="form.userId" placeholder="选择供应商类型用户" filterable style="width: 100%">
            <el-option
              v-for="u in supplierUsers"
              :key="u.id"
              :label="`${u.nickname || u.phone} (${u.phone})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商家名称" prop="merchantName">
          <el-input v-model="form.merchantName" placeholder="企业/店铺全称" />
        </el-form-item>
        <el-form-item label="法人姓名" prop="legalPerson">
          <el-input v-model="form.legalPerson" />
        </el-form-item>
        <el-form-item label="营业执照号">
          <el-input v-model="form.businessLicense" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="form.shopAddress" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="经营范围">
          <el-input v-model="form.businessScope" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="保证金">
          <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="form.auditStatus" style="width: 100%">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleFormSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" :title="auditDialogTitle" width="640px" destroy-on-close>
      <div v-loading="auditDetailLoading" class="audit-dialog-body">
        <section v-if="auditMerchantInfo" class="audit-merchant-info">
          <h4 class="audit-section-title">供应商基本信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="商家名称" :span="2">
              {{ auditMerchantInfo.merchantName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="法人姓名">
              {{ auditMerchantInfo.legalPerson || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ auditMerchantInfo.contactPhone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="营业执照号">
              {{ auditMerchantInfo.businessLicense || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系邮箱">
              {{ auditMerchantInfo.contactEmail || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="店铺地址" :span="2">
              {{ auditMerchantInfo.shopAddress || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="经营范围" :span="2">
              {{ auditMerchantInfo.businessScope || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="保证金">
              {{ formatDeposit(auditMerchantInfo.deposit) }}
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatDateTime(auditMerchantInfo.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </section>

        <el-divider />

        <section class="audit-form-section">
          <h4 class="audit-section-title">审核操作</h4>
          <el-form :model="auditForm" label-width="88px">
            <el-form-item label="审核结果">
              <el-radio-group v-model="auditForm.auditStatus">
                <el-radio :label="1">通过</el-radio>
                <el-radio :label="2">拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审核备注">
              <el-input
                v-model="auditForm.auditRemark"
                type="textarea"
                :rows="3"
                :placeholder="auditForm.auditStatus === 2 ? '请填写驳回原因（建议必填）' : '选填'"
              />
            </el-form-item>
          </el-form>
        </section>
      </div>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAudit" :loading="auditLoading">
          {{ auditForm.auditStatus === 1 ? '确认通过' : '确认驳回' }}
        </el-button>
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

const { tableHeight } = useTableHeight(310)
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMerchantList,
  auditMerchant,
  getMerchantDetail,
  createMerchant,
  updateMerchant,
  deleteMerchant
} from '@/api/merchant'
import { getAdminUserList } from '@/api/admin'

const loading = ref(false)
const auditLoading = ref(false)
const formLoading = ref(false)
const detailVisible = ref(false)
const auditVisible = ref(false)
const formVisible = ref(false)
const formRef = ref(null)
const currentMerchant = ref(null)
const auditMerchantInfo = ref(null)
const auditDetailLoading = ref(false)
const supplierUsers = ref([])

const searchForm = reactive({ keyword: '', auditStatus: null })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])

const colWidths = computed(() => ({
  merchantName: estimateColMinWidth('商家名称', maxCellSample(tableData.value, 'merchantName'), { min: 140 })
}))

const auditForm = reactive({ merchantId: null, auditStatus: 1, auditRemark: '' })

const auditDialogTitle = computed(() =>
  auditForm.auditStatus === 1 ? '审核通过 · 供应商入驻' : '审核驳回 · 供应商入驻'
)

const defaultForm = () => ({
  id: null,
  userId: null,
  merchantName: '',
  legalPerson: '',
  businessLicense: '',
  contactPhone: '',
  contactEmail: '',
  shopAddress: '',
  businessScope: '',
  description: '',
  deposit: 0,
  auditStatus: 0,
  rating: 5,
  salesCount: 0,
  settlementAccountType: 1,
  settlementAccountName: '',
  bankName: '',
  settlementAccountNo: ''
})

const form = reactive(defaultForm())

const formRules = {
  userId: [{ required: true, message: '请选择关联账号', trigger: 'change' }],
  merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  legalPerson: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
  loadSupplierUsers()
})

const loadSupplierUsers = async () => {
  const res = await getAdminUserList({ userType: 4, page: 1, pageSize: 100 })
  supplierUsers.value = res.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMerchantList({
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
  autoKeys: ['auditStatus'],
  onSearch: handleSearch
})

const handleReset = () => {
  Object.assign(searchForm, { keyword: '', auditStatus: null })
  handleSearch()
}

function formatDeposit(val) {
  if (val == null || val === '') return '-'
  const n = Number(val)
  return Number.isFinite(n) ? `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}` : '-'
}

const openAuditDialog = async (row, defaultStatus = 1) => {
  auditForm.merchantId = row.id
  auditForm.auditStatus = defaultStatus
  auditForm.auditRemark = defaultStatus === 1 ? '审核通过' : ''
  auditMerchantInfo.value = null
  auditVisible.value = true
  auditDetailLoading.value = true
  try {
    auditMerchantInfo.value = await getMerchantDetail(row.id)
  } catch {
    auditMerchantInfo.value = { ...row }
  } finally {
    auditDetailLoading.value = false
  }
}

const quickAudit = (row, auditStatus) => {
  openAuditDialog(row, auditStatus)
}

const handleViewDetail = async (row) => {
  currentMerchant.value = await getMerchantDetail(row.id)
  detailVisible.value = true
}

const openFormDialog = async (row) => {
  await loadSupplierUsers()
  if (row) {
    const detail = await getMerchantDetail(row.id)
    Object.assign(form, defaultForm(), {
      id: detail.id,
      userId: detail.userId,
      merchantName: detail.merchantName,
      legalPerson: detail.legalPerson,
      businessLicense: detail.businessLicense,
      contactPhone: detail.contactPhone,
      contactEmail: detail.contactEmail,
      shopAddress: detail.shopAddress,
      businessScope: detail.businessScope,
      description: detail.description,
      deposit: detail.deposit,
      auditStatus: detail.auditStatus,
      rating: detail.rating,
      salesCount: detail.salesCount
    })
  } else {
    Object.assign(form, defaultForm())
  }
  formVisible.value = true
}

const handleFormSubmit = async () => {
  if (!formRef.value) return
  if (!form.id && !form.userId) {
    ElMessage.warning('请选择关联账号')
    return
  }
  try {
    if (form.id) {
      await formRef.value.validateField(['merchantName', 'legalPerson', 'contactPhone'])
    } else {
      await formRef.value.validate()
    }
  } catch {
    return
  }
  formLoading.value = true
  try {
    const payload = { ...form }
    delete payload.id
    if (form.id) {
      await updateMerchant(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createMerchant(payload)
      ElMessage.success('添加成功')
    }
    formVisible.value = false
    fetchData()
    loadSupplierUsers()
  } catch {
    /* request interceptor */
  } finally {
    formLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除供应商「${row.merchantName}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteMerchant(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

const handleAudit = (row) => {
  openAuditDialog(row, 2)
}

const handleConfirmAudit = async () => {
  if (auditForm.auditStatus === 2 && !auditForm.auditRemark?.trim()) {
    ElMessage.warning('驳回时请填写审核备注')
    return
  }
  auditLoading.value = true
  try {
    await auditMerchant(auditForm.merchantId, {
      auditStatus: auditForm.auditStatus,
      auditRemark: auditForm.auditRemark
    })
    ElMessage.success('审核成功')
    auditVisible.value = false
    fetchData()
  } finally {
    auditLoading.value = false
  }
}

const getAuditStatusName = (status) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '未知')
const getAuditStatusTag = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || '')
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}

.audit-dialog-body {
  min-height: 120px;
}

.audit-section-title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.audit-merchant-info :deep(.el-descriptions__label) {
  width: 96px;
  font-weight: 500;
}

.audit-form-section {
  margin-top: 4px;
}
</style>
