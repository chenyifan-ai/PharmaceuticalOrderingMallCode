<template>
  <div class="consumer-page addresses-page">
    <div class="consumer-container">
      <header class="consumer-page-header addresses-header">
        <div>
          <h2>收货地址</h2>
          <p class="subtitle">管理采购收货地址，支持智能识别</p>
        </div>
        <el-button type="primary" round @click="handleAdd">新增地址</el-button>
      </header>

      <el-card v-loading="loading" class="consumer-card address-list-card" shadow="never">
        <el-empty v-if="addressList.length === 0" description="暂无收货地址">
          <el-button type="primary" @click="handleAdd">添加地址</el-button>
        </el-empty>
        <div v-else class="address-list">
          <article
            v-for="item in addressList"
            :key="item.id"
            class="address-item"
            :class="{ default: item.isDefault }"
          >
            <div class="address-info">
              <div class="info-top">
                <span class="name">{{ item.name }}</span>
                <span class="phone">{{ item.phone }}</span>
                <el-tag v-if="item.isDefault" type="success" size="small" effect="dark">默认</el-tag>
              </div>
              <p class="info-detail">
                {{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}
              </p>
            </div>
            <div class="address-actions">
              <el-button
                v-if="!item.isDefault"
                link
                type="primary"
                @click="handleSetDefault(item.id)"
              >
                设为默认
              </el-button>
              <el-button link type="primary" @click="handleEdit(item)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </article>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑地址' : '新增地址'"
      width="560px"
      @close="resetForm"
    >
      <div class="smart-fill-block">
        <p class="smart-title">智能填写</p>
        <el-input
          v-model="pasteText"
          type="textarea"
          :rows="3"
          placeholder="粘贴完整收货信息，例如：张三 13800138000 广东省广州市天河区xx路xx号"
        />
        <div class="smart-actions">
          <el-button type="primary" plain :loading="parsing" @click="handleSmartParse">
            识别并填充
          </el-button>
          <el-button plain @click="fillFromProfile">使用账户信息</el-button>
        </div>
        </div>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="88px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="formData.name" placeholder="收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="省市区" prop="regionCodes" required>
          <el-cascader
            v-model="regionCodes"
            :options="regionOptions"
            :props="cascaderProps"
            placeholder="请选择省 / 市 / 区"
            filterable
            clearable
            style="width: 100%"
            @change="onRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="formData.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="formData.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getAddressList,
  getRegionOptions,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  parseAddressText,
  fromApiAddress,
  toApiAddress
} from '@/api/consumer/address'

const userStore = useUserStore()
const loading = ref(false)
const parsing = ref(false)
const addressList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const pasteText = ref('')
const regionOptions = ref([])
const regionCodes = ref([])
const cascaderProps = { value: 'value', label: 'label', children: 'children' }

const formData = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const formRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  regionCodes: [
    {
      validator: (_rule, _val, callback) => {
        if (regionCodes.value?.length === 3) {
          callback()
        } else {
          callback(new Error('请选择省市区'))
        }
      },
      trigger: 'change'
    }
  ],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

onMounted(() => {
  fetchAddressList()
  loadRegions()
})

async function loadRegions() {
  try {
    regionOptions.value = (await getRegionOptions()) || []
  } catch {
    regionOptions.value = []
  }
}

function onRegionChange(val) {
  if (val?.length === 3) {
    formData.value.province = val[0]
    formData.value.city = val[1]
    formData.value.district = val[2]
  } else {
    formData.value.province = ''
    formData.value.city = ''
    formData.value.district = ''
  }
}

function syncRegionCodesFromForm() {
  if (formData.value.province && formData.value.city && formData.value.district) {
    regionCodes.value = [formData.value.province, formData.value.city, formData.value.district]
  } else {
    regionCodes.value = []
  }
}

async function fetchAddressList() {
  loading.value = true
  try {
    const list = await getAddressList()
    addressList.value = (list || []).map(fromApiAddress)
  } catch {
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingId.value = null
  pasteText.value = ''
  resetForm()
  dialogVisible.value = true
}

function handleEdit(item) {
  editingId.value = item.id
  pasteText.value = ''
  formData.value = {
    name: item.name,
    phone: item.phone,
    province: item.province,
    city: item.city,
    district: item.district,
    detail: item.detail,
    isDefault: item.isDefault
  }
  syncRegionCodesFromForm()
  dialogVisible.value = true
}

async function handleSmartParse() {
  if (!pasteText.value.trim()) {
    ElMessage.warning('请先粘贴收货信息')
    return
  }
  parsing.value = true
  try {
    const data = await parseAddressText(pasteText.value.trim())
    applyParsed(data)
    ElMessage.success('已识别并填充')
  } catch (e) {
    ElMessage.error(e.message || '识别失败，请检查格式')
  } finally {
    parsing.value = false
  }
}

function applyParsed(data) {
  if (!data) return
  if (data.name) formData.value.name = data.name
  if (data.phone) formData.value.phone = data.phone
  if (data.province) formData.value.province = data.province
  if (data.city) formData.value.city = data.city
  if (data.district) formData.value.district = data.district
  if (data.detail) formData.value.detail = data.detail
  syncRegionCodesFromForm()
}

function fillFromProfile() {
  const u = userStore.userInfo || {}
  if (u.realName) formData.value.name = u.realName
  if (u.phone) formData.value.phone = u.phone
  if (u.realName || u.phone) {
    ElMessage.success('已填入账户姓名/手机号，请补充地址')
  } else {
    ElMessage.info('账户暂无姓名或手机号')
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    const payload = toApiAddress({ ...formData.value, id: editingId.value })
    if (editingId.value) {
      await updateAddress(payload)
      ElMessage.success('更新成功')
    } else {
      await addAddress(payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchAddressList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

async function handleSetDefault(id) {
  try {
    await setDefaultAddress(id)
    ElMessage.success('设置成功')
    fetchAddressList()
  } catch {
    ElMessage.error('设置失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', { type: 'warning' })
    await deleteAddress(id)
    ElMessage.success('删除成功')
    fetchAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function resetForm() {
  formData.value = {
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  }
  regionCodes.value = []
  formRef.value?.resetFields()
}
</script>

<style scoped>
.addresses-page {
  padding-bottom: 48px;
}

.addresses-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-list-card {
  padding: 8px 0;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s;
}

.address-item:last-child {
  border-bottom: none;
}

.address-item.default {
  background: #f0fdf4;
}

.info-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

.phone {
  color: #64748b;
}

.info-detail {
  margin: 0;
  line-height: 1.6;
  color: #475569;
}

.address-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  flex-shrink: 0;
}

.smart-fill-block {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px dashed #cbd5e1;
}

.smart-title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #0d6e9f;
}

.smart-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.region-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.region-item {
  flex: 1;
  margin-bottom: 0 !important;
}

.region-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
</style>
