<template>
  <div class="c-qualification">
    <div class="container">
      <h2>企业资质</h2>

      <el-card v-loading="loading" class="qualification-card">
        <el-empty v-if="!qualification" description="暂无资质信息">
          <el-button type="primary" @click="handleEdit">提交资质</el-button>
        </el-empty>

        <div v-else class="qualification-info">
          <div class="status-section">
            <span class="label">审核状态：</span>
            <el-tag :type="qualificationStatusTag(qualification.qualificationStatus)">
              {{ qualificationStatusLabel(qualification.qualificationStatus) }}
            </el-tag>
            <span v-if="qualification.qualificationExpireDate" class="expire-tip">
              有效期至 {{ qualification.qualificationExpireDate }}
            </span>
          </div>

          <el-divider />

          <el-descriptions :column="2" border>
            <el-descriptions-item label="企业名称">{{ qualification.companyName }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ qualification.creditCode }}</el-descriptions-item>
            <el-descriptions-item label="法定代表人">{{ qualification.legalPerson || '-' }}</el-descriptions-item>
            <el-descriptions-item label="法人身份证">{{ qualification.legalPersonIdCard || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <div class="image-section">
            <h4>资质文件</h4>
            <div class="image-list">
              <div v-if="qualification.businessLicenseUrl" class="image-item">
                <span class="image-label">营业执照</span>
                <el-image
                  :src="qualification.businessLicenseUrl"
                  fit="cover"
                  class="preview-image"
                  :preview-src-list="[qualification.businessLicenseUrl]"
                />
              </div>
              <div v-if="qualification.drugOperationPermitUrl" class="image-item">
                <span class="image-label">药品经营许可证</span>
                <el-image
                  :src="qualification.drugOperationPermitUrl"
                  fit="cover"
                  class="preview-image"
                  :preview-src-list="[qualification.drugOperationPermitUrl]"
                />
              </div>
              <div v-if="qualification.medicalDevicePermitUrl" class="image-item">
                <span class="image-label">医疗器械经营许可证</span>
                <el-image
                  :src="qualification.medicalDevicePermitUrl"
                  fit="cover"
                  class="preview-image"
                  :preview-src-list="[qualification.medicalDevicePermitUrl]"
                />
              </div>
              <div v-if="qualification.gspCertificateUrl" class="image-item">
                <span class="image-label">GSP认证证书</span>
                <el-image
                  :src="qualification.gspCertificateUrl"
                  fit="cover"
                  class="preview-image"
                  :preview-src-list="[qualification.gspCertificateUrl]"
                />
              </div>
            </div>
          </div>

          <el-alert
            v-if="qualification.qualificationRejectReason"
            type="error"
            :title="'驳回原因：' + qualification.qualificationRejectReason"
            show-icon
            :closable="false"
            class="reject-alert"
          />

          <div class="action-section">
            <el-button
              v-if="qualification.qualificationStatus !== 1"
              type="primary"
              @click="handleEdit"
            >
              {{ qualification.qualificationStatus === 2 ? '重新提交' : '编辑资质' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="提交企业资质" width="720px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="140px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="companyName">
              <el-input v-model="formData.companyName" placeholder="与营业执照一致" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="formData.creditCode" placeholder="18位信用代码" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法定代表人" prop="legalPerson">
              <el-input v-model="formData.legalPerson" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人身份证号">
              <el-input v-model="formData.legalPersonIdCard" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资质到期日期" prop="qualificationExpireDate">
              <el-date-picker
                v-model="formData.qualificationExpireDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">资质证照（营业执照必填）</el-divider>
        <el-row :gutter="20" class="upload-row">
          <el-col :span="12" :sm="6">
            <el-form-item label="营业执照" prop="businessLicenseUrl" label-position="top">
              <ImageUploader v-model="formData.businessLicenseUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="12" :sm="6">
            <el-form-item label="药品经营许可证" label-position="top">
              <ImageUploader v-model="formData.drugOperationPermitUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="12" :sm="6">
            <el-form-item label="医疗器械经营许可证" label-position="top">
              <ImageUploader v-model="formData.medicalDevicePermitUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="12" :sm="6">
            <el-form-item label="GSP认证证书" label-position="top">
              <ImageUploader v-model="formData.gspCertificateUrl" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyQualification, submitQualification } from '@/api/consumer/qualification'
import ImageUploader from '@/components/ImageUploader.vue'
import {
  defaultQualificationForm,
  mapQualificationToForm,
  qualificationFormRules,
  qualificationStatusLabel,
  qualificationStatusTag
} from '@/utils/qualification'

const loading = ref(false)
const submitting = ref(false)
const qualification = ref(null)
const dialogVisible = ref(false)
const formRef = ref(null)
const formData = ref(defaultQualificationForm())
const formRules = qualificationFormRules

onMounted(fetchQualification)

async function fetchQualification() {
  loading.value = true
  try {
    qualification.value = await getMyQualification()
  } catch {
    qualification.value = null
  } finally {
    loading.value = false
  }
}

function handleEdit() {
  formData.value = qualification.value
    ? mapQualificationToForm(qualification.value)
    : defaultQualificationForm()
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    await submitQualification(formData.value)
    ElMessage.success('提交成功，请等待审核')
    dialogVisible.value = false
    await fetchQualification()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  formData.value = defaultQualificationForm()
  formRef.value?.resetFields()
}
</script>

<style scoped>
.c-qualification {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px 0;
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 20px;
}

.container h2 {
  margin: 0 0 20px;
  color: #1e293b;
}

.qualification-card {
  border-radius: 12px;
}

.status-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.status-section .label {
  color: #64748b;
}

.expire-tip {
  font-size: 13px;
  color: #909399;
}

.image-section h4 {
  margin: 0 0 16px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.image-item {
  text-align: center;
}

.image-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.preview-image {
  width: 100%;
  max-width: 160px;
  height: 160px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.reject-alert {
  margin-top: 16px;
}

.action-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}

.upload-row :deep(.el-form-item__label) {
  padding-bottom: 4px;
}
</style>
