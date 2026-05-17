<template>
  <div class="consumer-login">
    <div class="login-panel">
      <div class="brand">
        <el-icon class="brand-icon"><FirstAidKit /></el-icon>
        <h1>医药订货商城</h1>
        <p>药店 · 诊所 · 医院 采购端</p>
      </div>

      <el-card class="login-card" shadow="hover">
        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="手机号"
              :prefix-icon="Phone"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
            登录采购端
          </el-button>
        </el-form>

        <div class="demo-account" @click="fillDemo">
          <el-icon><InfoFilled /></el-icon>
          <span>测试采购账号：13800000002 / admin123（点击填充）</span>
        </div>
      </el-card>

      <p class="admin-link">
        <a href="/index.html">进入管理后台</a>
        <span class="sep">|</span>
        <a :href="legacyPath">旧版路径 /c/products</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { FirstAidKit, InfoFilled, Phone, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { isConsumerUser } from '@/utils/consumer'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  phone: '13800000002',
  password: 'admin123'
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const legacyPath = computed(() => `${window.location.origin}/c/products`)

async function handleLogin() {
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.loginAction(form, { consumerOnly: true })
      if (!isConsumerUser(userStore.userInfo?.userType)) {
        ElMessage.warning('请使用采购方账号登录')
        userStore.logout()
      }
    } finally {
      loading.value = false
    }
  })
}

function fillDemo() {
  form.phone = '13800000002'
  form.password = 'admin123'
  ElMessage.success('已填充采购方测试账号')
}
</script>

<style scoped>
.consumer-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(160deg, #0c4a6e 0%, #1e6bb8 45%, #38bdf8 100%);
}

.login-panel {
  width: 100%;
  max-width: 420px;
}

.brand {
  text-align: center;
  color: #fff;
  margin-bottom: 28px;
}

.brand-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.brand h1 {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 8px;
}

.brand p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.login-card {
  border-radius: 16px;
  padding: 8px 4px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.demo-account {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 12px;
  background: #f0f9ff;
  border-radius: 8px;
  font-size: 13px;
  color: #0369a1;
  cursor: pointer;
}

.demo-account:hover {
  background: #e0f2fe;
}

.admin-link {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
}

.admin-link a {
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
}

.admin-link a:hover {
  text-decoration: underline;
}

.admin-link .sep {
  margin: 0 10px;
  opacity: 0.6;
}
</style>
