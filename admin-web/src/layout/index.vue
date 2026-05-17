<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '72px' : '220px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon">药</span>
        <transition name="fade">
          <span v-if="!isCollapse" class="logo-text">医药订货管理</span>
        </transition>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="admin-menu"
        >
          <el-menu-item v-for="item in menuRoutes" :key="item.path" :index="'/' + item.path">
            <el-icon><component :is="item.meta.icon" /></el-icon>
            <template #title>{{ item.meta.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-wrap">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <div class="page-heading">
            <h1 class="page-title">{{ currentTitle }}</h1>
            <p v-if="pageSubtitle" class="page-subtitle">{{ pageSubtitle }}</p>
          </div>
        </div>
        <div class="header-right">
          <el-button v-if="isAdmin" type="primary" plain round size="small" class="mall-btn" @click="goToConsumerMall">
            采购商城
          </el-button>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">{{ avatarLetter }}</el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.phone || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ roleLabel }}</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content" :class="{ 'is-dashboard': isDashboard }">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { isAdminUser } from '@/utils/consumer'
import { goToConsumerMall } from '@/utils/consumer-nav'

const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)

const isAdmin = computed(() => isAdminUser(userStore.userInfo?.userType))

const activeMenu = computed(() => route.path)
const isDashboard = computed(() => route.path === '/dashboard' || route.name === 'Dashboard')

const menuRoutes = computed(() => {
  const userType = userStore.userInfo?.userType
  const role = userType === 4 ? 'merchant' : userType === 5 ? 'admin' : null
  return (
    route.matched[0]?.children?.filter((r) => {
      if (!r.meta?.title || r.meta?.hidden) return false
      const roles = r.meta.roles
      if (!roles || !role) return true
      return roles.includes(role)
    }) || []
  )
})

const currentTitle = computed(() => {
  const matched = route.matched.filter((r) => r.meta?.title && !r.meta?.hidden)
  return matched[matched.length - 1]?.meta?.title || '管理后台'
})

const pageSubtitle = computed(() => {
  if (isDashboard.value) return '实时数据大屏'
  return ''
})

const roleLabel = computed(() => {
  const t = userStore.userInfo?.userType
  if (t === 5) return '平台管理员'
  if (t === 4) return '供应商'
  return '用户'
})

const avatarLetter = computed(() => {
  const n = userStore.userInfo?.nickname || userStore.userInfo?.phone || 'U'
  return n.charAt(0).toUpperCase()
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => userStore.logout())
      .catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--admin-bg, #f4f4f5);
}

.sidebar {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, var(--admin-sidebar-from, #1e1b4b) 0%, var(--admin-sidebar-to, #312e81) 100%);
  box-shadow: 2px 0 16px rgba(30, 27, 75, 0.2);
  transition: width 0.28s ease;
  overflow: hidden;
}

.logo {
  flex-shrink: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  width: 36px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--admin-primary, #6366f1), #a78bfa);
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.menu-scroll {
  flex: 1;
}

.admin-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.72);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  --el-menu-active-color: #fff;
}

.admin-menu:not(.el-menu--collapse) {
  width: 220px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.4), rgba(99, 102, 241, 0.08));
  border-right: 3px solid var(--admin-primary, #6366f1);
}

.main-wrap {
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px 0 16px;
  background: var(--admin-surface, #fff);
  border-bottom: 1px solid var(--admin-border, #e4e4e7);
  box-shadow: var(--admin-shadow);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  color: #606266;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.page-heading {
  min-width: 0;
}

.page-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.3;
}

.page-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, var(--admin-primary, #6366f1), #a78bfa);
  color: #fff;
  font-size: 14px;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #303133;
}

.main-content {
  padding: 20px;
  background: var(--admin-bg, #f4f4f5);
}

.main-content.is-dashboard {
  padding: 0;
  background: #01060f;
  overflow: hidden;
  min-width: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
