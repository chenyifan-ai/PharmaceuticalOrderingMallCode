<template>
  <div class="product-edit-page page-container" v-loading="pageLoading">
    <el-card class="page-card edit-card">
      <header class="page-toolbar">
        <div class="toolbar-left">
          <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
          <span class="page-title">{{ isCreate ? '添加药品' : '编辑药品' }}</span>
          <span v-if="!isCreate && form.id" class="page-sub">ID: {{ form.id }}</span>
        </div>
        <div class="toolbar-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </header>

      <ProductFormPanel ref="formPanelRef" layout="page" :form="form" :rules="formRules" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ProductFormPanel from '@/components/product/ProductFormPanel.vue'
import { defaultProductForm, mapProductToForm } from '@/utils/admin-product'
import { getAdminProductDetail, createAdminProduct, updateAdminProduct } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const submitting = ref(false)
const formPanelRef = ref(null)
const form = reactive(defaultProductForm())

const isCreate = computed(() => route.name === 'ProductCreate')

const formRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  prescriptionType: [{ required: true, message: '请选择处方类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  wholesalePrice: [{ required: true, message: '请输入批发价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

onMounted(async () => {
  if (!isCreate.value) {
    pageLoading.value = true
    try {
      const detail = await getAdminProductDetail(route.params.id)
      Object.assign(form, mapProductToForm(detail))
    } catch {
      ElMessage.error('商品不存在')
      router.replace('/products')
    } finally {
      pageLoading.value = false
    }
  } else {
    Object.assign(form, defaultProductForm())
  }
})

function goBack() {
  if (isCreate.value) {
    router.push('/products')
  } else {
    router.push(`/products/${route.params.id}`)
  }
}

async function handleSubmit() {
  const valid = await formPanelRef.value?.validate()
  if (!valid) {
    ElMessage.warning('请完善必填项')
    return
  }

  submitting.value = true
  try {
    const payload = { ...form }
    delete payload.id
    if (isCreate.value) {
      const created = await createAdminProduct(payload)
      ElMessage.success('添加成功')
      if (created?.id) {
        router.replace(`/products/${created.id}`)
      } else {
        router.push('/products')
      }
    } else {
      await updateAdminProduct(form.id, payload)
      ElMessage.success('保存成功')
      router.push(`/products/${form.id}`)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.product-edit-page {
  padding-bottom: 24px;
}

.edit-card :deep(.el-card__body) {
  padding-top: 16px;
  overflow: visible;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
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
}

.page-sub {
  font-size: 13px;
  color: #909399;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}
</style>
