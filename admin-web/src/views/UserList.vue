<template>
  <div class="user-list page-container">
    <el-card class="page-card">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="手机号/昵称/姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="searchForm.userType" placeholder="全部" clearable style="width: 140px">
            <el-option label="消费者" :value="1" />
            <el-option label="药店" :value="2" />
            <el-option label="医院" :value="3" />
            <el-option label="供应商" :value="4" />
            <el-option label="管理员" :value="5" />
            <el-option label="药师" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="冻结" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openCreateDialog">+ 新增用户</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <div class="table-wrap">
      <el-table :data="tableData" v-loading="loading" border stripe :height="tableHeight" class="admin-data-table">
        <el-table-column prop="id" label="ID" :min-width="64" />
        <el-table-column prop="phone" label="手机号" :min-width="colWidths.phone" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" :min-width="colWidths.nickname" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" :min-width="colWidths.realName" show-overflow-tooltip />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getUserTypeTag(row.userType)" size="small">
              {{ getUserTypeName(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ { 0: '未知', 1: '男', 2: '女' }[row.gender] || '未知' }}</template>
        </el-table-column>
        <el-table-column prop="realNameStatus" label="实名认证" width="90">
          <template #default="{ row }">
            <el-tag :type="row.realNameStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.realNameStatus === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 0 ? 'warning' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 0 ? '冻结' : '解冻' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 用户详情 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="520px">
      <el-descriptions v-if="detailUser" :column="2" border>
        <el-descriptions-item label="ID">{{ detailUser.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailUser.nickname }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ detailUser.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">
          <el-tag :type="getUserTypeTag(detailUser.userType)" size="small">
            {{ getUserTypeName(detailUser.userType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ { 0: '未知', 1: '男', 2: '女' }[detailUser.gender] || '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="实名认证">
          {{ detailUser.realNameStatus === 1 ? '已认证' : '未认证' }}
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          {{ detailUser.status === 0 ? '正常' : '冻结' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDateTime(detailUser.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ formatDateTime(detailUser.lastLoginTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择" style="width: 100%">
            <el-option label="消费者" :value="1" />
            <el-option label="药店" :value="2" />
            <el-option label="医院" :value="3" />
            <el-option label="供应商" :value="4" />
            <el-option label="管理员" :value="5" />
            <el-option label="药师" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">冻结</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
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
  getAdminUserList,
  getAdminUserDetail,
  createAdminUser,
  updateAdminUser,
  deleteAdminUser,
  toggleUserStatus
} from '@/api/admin'

const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const detailUser = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  keyword: '',
  userType: null,
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref([])

const colWidths = computed(() => ({
  phone: estimateColMinWidth('手机号', maxCellSample(tableData.value, 'phone'), { min: 120 }),
  nickname: estimateColMinWidth('昵称', maxCellSample(tableData.value, 'nickname'), { min: 100 }),
  realName: estimateColMinWidth('真实姓名', maxCellSample(tableData.value, 'realName'), { min: 96 })
}))

const form = reactive({
  id: null,
  phone: '',
  nickname: '',
  realName: '',
  userType: 1,
  gender: 0,
  email: '',
  status: 0
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const getUserTypeName = (type) => {
  const map = { 1: '消费者', 2: '药店', 3: '医院', 4: '供应商', 5: '管理员', 6: '药师' }
  return map[type] || '未知'
}

const getUserTypeTag = (type) => {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'danger', 5: 'info', 6: 'primary' }
  return map[type] || ''
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const res = await getAdminUserList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) {
    console.error('获取用户列表失败', e)
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
  autoKeys: ['userType', 'status'],
  onSearch: handleSearch
})

const handleReset = () => {
  Object.assign(searchForm, { keyword: '', userType: null, status: null })
  handleSearch()
}

const handleViewDetail = async (row) => {
  detailUser.value = await getAdminUserDetail(row.id)
  detailVisible.value = true
}

const openCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    phone: '',
    nickname: '',
    realName: '',
    userType: 1,
    gender: 0,
    email: '',
    status: 0
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    phone: row.phone,
    nickname: row.nickname,
    realName: row.realName,
    userType: row.userType,
    gender: row.gender,
    email: row.email,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateAdminUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createAdminUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error('操作失败', e)
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = (row) => {
  const actionText = row.status === 0 ? '冻结' : '解冻'
  ElMessageBox.confirm(`确定要${actionText}该用户吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await toggleUserStatus(row.id)
    ElMessage.success(`${actionText}成功`)
    fetchData()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？删除后数据不可恢复！', '警告', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteAdminUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}
</style>
