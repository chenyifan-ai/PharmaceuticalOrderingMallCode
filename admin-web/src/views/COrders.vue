<template>
  <div class="consumer-page orders-page">
    <div class="consumer-container">
      <header class="consumer-page-header">
        <h2>我的订单</h2>
        <p class="subtitle">查看采购订单状态与物流进度</p>
      </header>

      <div class="orders-tabs-wrap consumer-card">
        <el-tabs v-model="activeStatus" @tab-change="handleTabChange" class="orders-tabs">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待付款" name="0" />
          <el-tab-pane label="待审核" name="1" />
          <el-tab-pane label="待发货" name="2" />
          <el-tab-pane label="已发货" name="3" />
          <el-tab-pane label="已完成" name="4" />
        </el-tabs>
      </div>

      <div v-loading="loading" class="orders-list">
        <article
          v-for="order in orderList"
          :key="order.id"
          class="consumer-card consumer-order-card order-card"
        >
          <div class="order-head">
            <div class="head-left">
              <span class="order-no">{{ order.orderNo }}</span>
              <span class="order-time" v-if="order.createTime">{{ formatTime(order.createTime) }}</span>
            </div>
            <el-tag :type="orderStatusTag(order.status)" effect="light" round>
              {{ orderStatusLabel(order.status) }}
            </el-tag>
          </div>

          <div class="order-items" v-if="order.orderItems?.length">
            <div
              v-for="item in order.orderItems"
              :key="item.id"
              class="order-item"
            >
              <div class="thumb">
                <ProductImage :src="item.productImage" :alt="item.productName" img-class="order-item-thumb" />
              </div>
              <div class="item-info">
                <h4>{{ item.productName }}</h4>
                <p class="spec">{{ item.specification }}</p>
                <p class="qty">×{{ item.quantity }} · ¥{{ item.price }}</p>
              </div>
            </div>
          </div>

          <div v-if="hasInvoiceInfo(order)" class="order-invoice-brief">
            <span class="label">发票：</span>
            <span class="text">{{ invoiceSummary(order) }}</span>
          </div>

          <div class="order-foot">
            <div class="order-total">
              共 {{ order.orderItems?.length || 0 }} 件
              <span class="total-price">¥{{ order.totalAmount }}</span>
            </div>
            <div class="order-actions">
              <el-button
                v-if="order.status === 0"
                type="primary"
                size="small"
                round
                @click="router.push(cPath(`pay/${order.id}`))"
              >
                去支付
              </el-button>
              <el-button v-if="order.status === 0" size="small" round @click="handleCancel(order)">
                取消
              </el-button>
              <el-button
                v-if="order.status === 3"
                type="success"
                size="small"
                round
                @click="handleConfirm(order.id)"
              >
                确认收货
              </el-button>
              <el-button size="small" round @click="viewDetail(order.id)">详情</el-button>
            </div>
          </div>
        </article>

        <div v-if="orderList.length === 0 && !loading" class="consumer-card consumer-empty-wrap">
          <el-empty description="暂无订单">
            <el-button type="primary" @click="router.push(cPath('products'))">去采购</el-button>
          </el-empty>
        </div>
        </div>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="consumer-pagination"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, cancelOrder, confirmReceive } from '@/api/consumer/order'
import { orderStatusLabel, orderStatusTag } from '@/utils/consumer'
import { hasInvoiceInfo, invoiceSummary } from '@/utils/invoice'
import { cPath } from '@/utils/consumer-path'
import ProductImage from '@/components/ProductImage.vue'

const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const activeStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(fetchData)

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (activeStatus.value !== 'all') {
      params.status = parseInt(activeStatus.value, 10)
    }
    const res = await getMyOrders(params)
    orderList.value = res?.list ?? []
    total.value = res?.total ?? 0
  } catch {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', { type: 'warning' })
    await cancelOrder(order.id, '用户取消')
    ElMessage.success('订单已取消')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消失败')
  }
}

async function handleConfirm(id) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    await confirmReceive(id)
    ElMessage.success('确认收货成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '确认失败')
  }
}

function viewDetail(id) {
  router.push(cPath(`order/${id}`))
}

function handleTabChange() {
  currentPage.value = 1
  fetchData()
}
</script>

<style scoped>
.orders-page {
  padding-bottom: 48px;
}

.orders-tabs-wrap {
  padding: 4px 16px 0;
  margin-bottom: 16px;
}

.orders-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.orders-list {
  min-height: 200px;
}

.order-card {
  margin-bottom: 16px;
  padding: 0;
  overflow: hidden;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.head-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  font-family: ui-monospace, monospace;
}

.order-time {
  font-size: 12px;
  color: #94a3b8;
}

.order-items {
  padding: 12px 20px;
}

.order-item {
  display: flex;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px solid #f8fafc;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item .thumb {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  background: #f8fafc;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
}

.item-info .spec,
.item-info .qty {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.order-invoice-brief {
  padding: 0 20px 10px;
  font-size: 13px;
  color: #64748b;
}

.order-invoice-brief .label {
  color: #94a3b8;
}

.order-invoice-brief .text {
  color: #475569;
}

.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 20px;
  border-top: 1px solid #f1f5f9;
}

.order-total {
  font-size: 14px;
  color: #64748b;
}

.total-price {
  margin-left: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #dc2626;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.consumer-pagination {
  margin-top: 24px;
  justify-content: center;
}
</style>
