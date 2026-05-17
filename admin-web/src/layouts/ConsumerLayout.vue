<template>
  <div class="consumer-layout consumer-layout-root">
    <header class="consumer-header">
      <div class="header-inner">
        <router-link :to="cPath('home')" class="brand">
          <el-icon :size="22"><FirstAidKit /></el-icon>
          <span>医药订货商城</span>
        </router-link>
        <nav class="nav-links">
          <router-link :to="cPath('home')">首页</router-link>
          <router-link :to="cPath('products')">商品</router-link>
          <router-link :to="cPath('cart')" class="nav-cart">
            订货清单
            <el-badge v-if="cartCount > 0" :value="cartCount" :max="99" />
          </router-link>
          <router-link :to="cPath('orders')">我的订单</router-link>
        </nav>
        <div class="header-right">
          <el-button v-if="isAdmin" class="console-btn" round @click="goAdminConsole">控制台</el-button>
          <el-tooltip content="消息中心">
            <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
              <el-button class="icon-btn" circle @click="router.push(cPath('messages'))">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </el-tooltip>
          <el-dropdown trigger="click" @command="onUserCommand">
            <span class="user-trigger">
              <el-avatar :size="32" class="user-avatar">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="addresses">收货地址</el-dropdown-item>
                <el-dropdown-item command="qualification">企业资质</el-dropdown-item>
                <el-dropdown-item command="prescriptions">我的处方</el-dropdown-item>
                <el-dropdown-item command="purchase-stats">采购统计</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" divided command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="consumer-main">
      <router-view />
    </main>
    <footer class="consumer-footer">
      <p>© 2026 医药订货平台 · 合规采购 · 专业服务</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, FirstAidKit, User, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCartList } from '@/api/consumer/cart'
import { getUnreadCount } from '@/api/consumer/message'
import { isAdminUser } from '@/utils/consumer'
import { cPath, isConsumerStandalone } from '@/utils/consumer-path'
import { goToAdminConsole } from '@/utils/admin-nav'

function goAdminConsole() {
  goToAdminConsole()
}

const router = useRouter()
const userStore = useUserStore()
const cartCount = ref(0)
const unreadCount = ref(0)

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.phone || '用户')
const avatarText = computed(() => (displayName.value || '用').slice(0, 1))
const isAdmin = computed(() => isAdminUser(userStore.userInfo?.userType))

async function refreshCounts() {
  try {
    const list = await getCartList()
    cartCount.value = Array.isArray(list) ? list.length : 0
  } catch {
    cartCount.value = 0
  }
  try {
    const n = await getUnreadCount()
    unreadCount.value = typeof n === 'number' ? n : Number(n?.count ?? n ?? 0)
  } catch {
    unreadCount.value = 0
  }
}

provide('refreshConsumerCounts', refreshCounts)

function onUserCommand(cmd) {
  const map = {
    profile: cPath('profile'),
    addresses: cPath('addresses'),
    qualification: cPath('qualification'),
    prescriptions: cPath('prescriptions'),
    'purchase-stats': cPath('purchase-stats'),
    admin: isConsumerStandalone() ? '/index.html' : '/dashboard',
    logout: null
  }
  if (cmd === 'logout') {
    userStore.logout()
    return
  }
  if (cmd === 'admin') {
    goToAdminConsole()
    return
  }
  if (map[cmd]) router.push(map[cmd])
}

onMounted(refreshCounts)
</script>

<style scoped>
.consumer-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--c-bg, #f0f4f8);
}

.consumer-header {
  background: var(--c-gradient, linear-gradient(135deg, #4f46e5 0%, #6366f1 50%, #818cf8 100%));
  color: #fff;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.25);
  position: sticky;
  top: 0;
  z-index: 200;
}

.header-inner {
  max-width: var(--c-container, 1200px);
  margin: 0 auto;
  padding: 0 20px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  text-decoration: none;
  font-weight: 600;
  font-size: 17px;
  white-space: nowrap;
}

.nav-links {
  display: flex;
  gap: 8px;
  flex: 1;
}

.nav-links a {
  color: rgba(255, 255, 255, 0.88);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.nav-links a:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.nav-links a.router-link-active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  color: #fff !important;
  border: none !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.console-btn {
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.5) !important;
  background: rgba(255, 255, 255, 0.12) !important;
}

.console-btn:hover {
  background: rgba(255, 255, 255, 0.22) !important;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
  font-size: 14px;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  transition: background 0.2s;
}

.user-trigger:hover {
  background: rgba(255, 255, 255, 0.12);
}

.user-avatar {
  background: rgba(255, 255, 255, 0.25) !important;
  color: #fff !important;
  font-size: 14px !important;
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.consumer-main {
  flex: 1;
  width: 100%;
}

.consumer-footer {
  text-align: center;
  padding: 14px;
  color: var(--c-text-secondary, #64748b);
  font-size: 12px;
  background: var(--c-surface, #fff);
  border-top: 1px solid var(--c-border, #e2e8f0);
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
