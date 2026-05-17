<template>
  <div class="consumer-page c-purchase-stats">
    <div class="consumer-container">
      <header class="consumer-page-header">
        <h2>采购统计</h2>
        <p class="subtitle">查看采购汇总与商品排行</p>
      </header>

      <el-row v-loading="loading" :gutter="16" class="stat-cards">
        <el-col :span="8">
          <el-card shadow="never">
            <div class="stat-num">{{ stats.orderCount ?? 0 }}</div>
            <div class="stat-label">有效订单</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <div class="stat-num">¥{{ stats.totalPayAmount ?? 0 }}</div>
            <div class="stat-label">采购总额</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <div class="stat-num">{{ stats.completedCount ?? 0 }}</div>
            <div class="stat-label">已完成</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="consumer-card" shadow="never">
        <template #header>
          <span>商品采购排行</span>
          <el-button type="primary" link @click="handleExport">导出 CSV</el-button>
        </template>
        <el-table :data="stats.topProducts || []" border stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPurchaseStats, exportPurchaseStats } from '@/api/consumer/purchase'

const loading = ref(false)
const stats = ref({ topProducts: [] })

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getPurchaseStats()
  } finally {
    loading.value = false
  }
})

async function handleExport() {
  try {
    const blob = await exportPurchaseStats()
    const url = URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = 'purchase-stats.csv'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('导出失败')
  }
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 20px;
}
.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-primary, #6366f1);
}
.stat-label {
  margin-top: 6px;
  color: var(--c-text-secondary, #64748b);
  font-size: 13px;
}
</style>
