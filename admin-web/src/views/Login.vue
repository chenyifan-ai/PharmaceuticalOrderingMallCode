<template>
  <div class="login-container">
    <!-- 背景动画粒子 -->
    <div class="particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <div class="login-wrapper">
      <el-card class="login-card">
        <div class="login-header">
          <div class="logo-icon">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <h2 class="login-title">医药订货平台</h2>
          <p class="login-subtitle">管理端登录</p>
        </div>

        <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="phone">
            <el-input
              v-model="loginForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登 录 中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 默认账号提示 -->
        <div class="account-tips">
          <div class="tips-header">
            <el-icon><InfoFilled /></el-icon>
            <span>测试账号（点击自动填充）</span>
          </div>
          <div class="account-list">
            <div class="account-item admin" @click="fillAccount('13800000000', 'admin123')">
              <el-icon><User /></el-icon>
              <span class="role">管理员</span>
              <span class="phone">13800000000</span>
            </div>
            <div class="account-item merchant" @click="fillAccount('13800000001', 'admin123')">
              <el-icon><Shop /></el-icon>
              <span class="role">商家</span>
              <span class="phone">13800000001</span>
            </div>
            <div class="account-item user" @click="fillAccount('13800000002', 'admin123')">
              <el-icon><Avatar /></el-icon>
              <span class="role">采购方</span>
              <span class="phone">13800000002</span>
            </div>
          </div>
        </div>
      </el-card>

      <p class="portal-link">
        <a href="/consumer.html">进入采购端商城 →</a>
      </p>

      <div class="footer">
        <p>© 2026 医药订货平台. All Rights Reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

// 默认填充管理员测试账号
const loginForm = reactive({
  phone: '13800000000',
  password: 'admin123'
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.loginAction(loginForm)
        ElMessage.success('登录成功')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 点击测试账号自动填充
const fillAccount = (phone, password) => {
  loginForm.phone = phone
  loginForm.password = password
  ElMessage.success(`已填充账号: ${phone}`)
}

// 生成粒子样式
const getParticleStyle = (i) => {
  const size = Math.random() * 10 + 5
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = Math.random() * 10 + 10
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 50%, #a78bfa 100%);
  overflow: hidden;
}

/* 粒子背景 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  bottom: -20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float-up infinite linear;
}

@keyframes float-up {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) rotate(360deg);
    opacity: 0;
  }
}

.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: 20px;
}

.login-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
  animation: slide-up 0.6s ease-out;
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 50%, #a78bfa 100%);
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.logo-icon .el-icon {
  font-size: 40px;
  color: #fff;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  margin-top: 30px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.login-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 50%, #a78bfa 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.5);
}

.account-tips {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-bottom: 15px;
}

.tips-header .el-icon {
  color: #6366f1;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.account-item:hover {
  transform: translateX(5px);
}

.account-item.admin {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(245, 108, 108, 0.05) 100%);
  border-color: rgba(245, 108, 108, 0.2);
}

.account-item.admin:hover {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.08) 100%);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

.account-item.admin .el-icon {
  color: #f56c6c;
}

.account-item.merchant {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(230, 162, 60, 0.05) 100%);
  border-color: rgba(230, 162, 60, 0.2);
}

.account-item.merchant:hover {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15) 0%, rgba(230, 162, 60, 0.08) 100%);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.2);
}

.account-item.merchant .el-icon {
  color: #e6a23c;
}

.account-item.user {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0.05) 100%);
  border-color: rgba(103, 194, 58, 0.2);
}

.account-item.user:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.15) 0%, rgba(103, 194, 58, 0.08) 100%);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
}

.account-item.user .el-icon {
  color: #67c23a;
}

.account-item .el-icon {
  font-size: 20px;
}

.account-item .role {
  font-weight: 500;
  color: #333;
  min-width: 50px;
}

.account-item .phone {
  margin-left: auto;
  color: #999;
  font-size: 13px;
}

.portal-link {
  text-align: center;
  margin-top: 20px;
}

.portal-link a {
  color: rgba(255, 255, 255, 0.95);
  font-size: 14px;
  text-decoration: none;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  transition: background 0.2s;
}

.portal-link a:hover {
  background: rgba(255, 255, 255, 0.15);
}

.footer {
  text-align: center;
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}
</style>
