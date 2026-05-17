<template>
  <div class="c-order-detail">
    <div class="container">
      <el-page-header @back="goBack" title="订单详情" />
      
      <el-card v-loading="loading" class="detail-card">
        <div v-if="order" class="order-info">
          <div class="order-header">
            <div>
              <span class="label">订单号：</span>
              <span class="value">{{ order.orderNo }}</span>
            </div>
            <el-tag :type="orderStatusTag(order.status)">{{ orderStatusLabel(order.status) }}</el-tag>
          </div>

          <el-divider />

          <div class="section">
            <h4>收货信息</h4>
            <div class="info-row">
              <span class="label">收货人：</span>
              <span class="value">{{ order.receiverName }}</span>
            </div>
            <div class="info-row">
              <span class="label">联系电话：</span>
              <span class="value">{{ order.receiverPhone }}</span>
            </div>
            <div class="info-row">
              <span class="label">收货地址：</span>
              <span class="value">{{ order.receiverAddress }}</span>
            </div>
          </div>

          <el-divider />

          <div class="section">
            <h4>商品信息</h4>
            <div v-for="item in order.orderItems" :key="item.id" class="order-item">
              <ProductImage :src="item.productImage" alt="" img-class="order-item-thumb" />
              <div class="item-info">
                <h5>{{ item.productName }}</h5>
                <p class="spec">{{ item.specification }}</p>
                <div class="item-bottom">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="quantity">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-divider />

          <OrderInvoiceSection :order="order" title="发票信息" />

          <el-divider v-if="logisticsTrace.length" />

          <div v-if="logisticsTrace.length" class="section">
            <h4>物流轨迹</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(node, idx) in logisticsTrace"
                :key="idx"
                :timestamp="formatDateTime(node.time)"
                placement="top"
              >
                <p class="trace-status">{{ node.status }}</p>
                <p class="trace-desc">{{ node.description }}</p>
              </el-timeline-item>
            </el-timeline>
          </div>

          <el-divider />

          <div class="section">
            <h4>订单金额</h4>
            <div class="info-row">
              <span class="label">商品总额：</span>
              <span class="value">¥{{ order.totalAmount }}</span>
            </div>
            <div class="info-row" v-if="order.discountAmount">
              <span class="label">优惠金额：</span>
              <span class="value">-¥{{ order.discountAmount }}</span>
            </div>
            <div class="info-row total">
              <span class="label">实付金额：</span>
              <span class="value highlight">¥{{ order.payAmount }}</span>
            </div>
          </div>

          <el-divider />

          <div class="section">
            <h4>订单信息</h4>
            <div class="info-row">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatDateTime(order.createTime) }}</span>
            </div>
            <div class="info-row" v-if="order.payTime">
              <span class="label">支付时间：</span>
              <span class="value">{{ order.payTime }}</span>
            </div>
            <div class="info-row" v-if="order.logisticsCompany">
              <span class="label">物流：</span>
              <span class="value">{{ order.logisticsCompany }} {{ order.logisticsNo }}</span>
            </div>
            <div class="info-row" v-if="order.shipTime">
              <span class="label">发货时间：</span>
              <span class="value">{{ order.shipTime }}</span>
            </div>
            <div class="info-row" v-if="order.finishTime">
              <span class="label">完成时间：</span>
              <span class="value">{{ order.finishTime }}</span>
            </div>
          </div>

          <div class="action-buttons" v-if="order.status === 0 || order.status === 3">
            <el-button v-if="order.status === 0" type="primary" @click="router.push(cPath(`pay/${order.id}`))">去支付</el-button>
            <el-button v-if="order.status === 0" @click="handleCancel">取消订单</el-button>
            <el-button v-if="order.status === 3" type="success" @click="handleConfirm">确认收货</el-button>
            <el-button v-if="order.status === 3" type="warning" @click="handleRefund">申请退款</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, cancelOrder, confirmReceive, getOrderLogistics, applyOrderRefund } from '@/api/consumer/order'
import { formatDateTime } from '@/utils/format'
import { orderStatusLabel, orderStatusTag } from '@/utils/consumer'
import { cPath } from '@/utils/consumer-path'
import ProductImage from '@/components/ProductImage.vue'
import OrderInvoiceSection from '@/components/OrderInvoiceSection.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const order = ref(null)
const logisticsTrace = ref([])

onMounted(() => {
  fetchOrderDetail()
})

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
    try {
      logisticsTrace.value = await getOrderLogistics(route.params.id)
    } catch {
      logisticsTrace.value = []
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(order.value.id, '用户取消')
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const handleRefund = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请填写退款原因', '申请退款', {
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    })
    await applyOrderRefund(order.value.id, value)
    ElMessage.success('退款申请已提交')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '操作失败')
    }
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmReceive(order.value.id)
    ElMessage.success('确认收货成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '确认失败')
    }
  }
}

const goBack = () => {
  router.back()
}

const getStatusName = (status) => {
  const map = {
    0: '待付款', 1: '待审核', 2: '待发货',
    3: '已发货', 4: '已完成', 5: '已取消'
  }
  return map[status] || '未知'
}

const getStatusTag = (status) => {
  const map = {
    0: 'warning', 1: 'info', 2: 'primary',
    3: 'success', 4: '', 5: 'info'
  }
  return map[status] || ''
}
</script>

<style scoped>
.c-order-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.detail-card {
  margin-top: 20px;
  border-radius: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section h4 {
  margin-bottom: 15px;
  color: #333;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.info-row.total {
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.label {
  width: 100px;
  color: #999;
  flex-shrink: 0;
}

.value {
  color: #333;
}

.value.highlight {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.order-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  background: #f9f9f9;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-info h5 {
  margin: 0 0 5px 0;
  color: #333;
}

.spec {
  color: #999;
  font-size: 13px;
  margin: 0 0 10px 0;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.quantity {
  color: #999;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>
