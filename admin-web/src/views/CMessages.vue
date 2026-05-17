<template>
  <div class="c-messages page-wrap">
    <div class="container">
      <div class="header">
        <h2>消息中心</h2>
        <el-button type="primary" link @click="markAllRead">全部已读</el-button>
      </div>
      <el-card v-loading="loading">
        <el-empty v-if="!messages.length" description="暂无消息" />
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-item"
          :class="{ unread: msg.isRead === 0 }"
          @click="readOne(msg)"
        >
          <div class="msg-title">{{ msg.title }}</div>
          <p class="msg-content">{{ msg.content }}</p>
          <span class="msg-time">{{ formatDateTime(msg.createTime) }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessageList, markMessageRead, markAllMessagesRead } from '@/api/consumer/message'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const messages = ref([])

async function fetchList() {
  loading.value = true
  try {
    const res = await getMessageList({ page: 1, pageSize: 50 })
    messages.value = res?.list ?? (Array.isArray(res) ? res : [])
  } finally {
    loading.value = false
  }
}

async function readOne(msg) {
  if (msg.isRead === 0) {
    await markMessageRead(msg.id)
    msg.isRead = 1
  }
}

async function markAllRead() {
  await markAllMessagesRead()
  ElMessage.success('已全部标记为已读')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.page-wrap { padding: 20px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 0 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header h2 { margin: 0; }
.msg-item { padding: 14px 0; border-bottom: 1px solid #eee; cursor: pointer; }
.msg-item.unread .msg-title { font-weight: 600; color: #303133; }
.msg-title { font-size: 15px; margin-bottom: 6px; }
.msg-content { color: #606266; font-size: 13px; margin: 0 0 6px; }
.msg-time { color: #909399; font-size: 12px; }
</style>
