<template>
  <div class="product-detail-page page-container" v-loading="loading">
    <el-card class="page-card detail-card">
      <header class="page-toolbar">
        <div class="toolbar-left">
          <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
          <span class="page-title">商品详情</span>
          <span v-if="product" class="page-sub">ID: {{ product.id }}</span>
        </div>
        <div v-if="product" class="toolbar-actions">
          <el-button type="primary" @click="goEdit">编辑商品</el-button>
          <template v-if="product.status === 2">
            <el-button type="success" @click="handleAudit(1)">审核通过</el-button>
            <el-button type="danger" @click="openReject">驳回</el-button>
          </template>
          <el-button v-if="product.status === 1" type="warning" @click="handleOffline">下架</el-button>
          <el-button v-if="product.status === 0" type="success" @click="handleOnline">上架</el-button>
        </div>
      </header>

      <ProductDetailPanel v-if="product" :product="product" />
    </el-card>

    <el-dialog v-model="rejectVisible" title="驳回商品" width="420px">
      <el-input v-model="rejectRemark" type="textarea" :rows="4" placeholder="请填写驳回原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProductDetailPanel from '@/components/product/ProductDetailPanel.vue'
import {
  getAdminProductDetail,
  auditProduct,
  offlineProduct,
  onlineProduct
} from '@/api/admin'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const product = ref(null)
const rejectVisible = ref(false)
const rejectRemark = ref('')
const rejectLoading = ref(false)

onMounted(loadProduct)

async function loadProduct() {
  loading.value = true
  try {
    product.value = await getAdminProductDetail(route.params.id)
  } catch {
    ElMessage.error('商品不存在')
    router.replace('/products')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/products')
}

function goEdit() {
  router.push(`/products/${route.params.id}/edit`)
}

function handleAudit(status) {
  ElMessageBox.confirm('确定通过该商品审核吗？', '审核', { type: 'warning' })
    .then(async () => {
      await auditProduct(product.value.id, status)
      ElMessage.success('操作成功')
      loadProduct()
    })
    .catch(() => {})
}

function openReject() {
  rejectRemark.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  rejectLoading.value = true
  try {
    await auditProduct(product.value.id, 0, rejectRemark.value)
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadProduct()
  } finally {
    rejectLoading.value = false
  }
}

function handleOffline() {
  ElMessageBox.confirm('确定下架该商品吗？', '下架', { type: 'warning' })
    .then(async () => {
      await offlineProduct(product.value.id)
      ElMessage.success('下架成功')
      loadProduct()
    })
    .catch(() => {})
}

function handleOnline() {
  ElMessageBox.confirm('确定上架该商品吗？', '上架', { type: 'info' })
    .then(async () => {
      await onlineProduct(product.value.id)
      ElMessage.success('上架成功')
      loadProduct()
    })
    .catch(() => {})
}
</script>

<style scoped>
.detail-card :deep(.el-card__body) {
  padding-top: 16px;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-sub {
  font-size: 13px;
  color: #909399;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
