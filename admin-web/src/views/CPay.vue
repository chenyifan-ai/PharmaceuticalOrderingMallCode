<template>
  <div class="consumer-page pay-page">
    <div class="consumer-container">
      <header class="consumer-page-header">
        <h2>订单支付</h2>
        <p class="subtitle">请选择支付方式完成付款</p>
      </header>

      <el-card v-loading="loading" class="pay-card consumer-card" shadow="never">
        <template v-if="order">
          <div class="order-summary">
            <div class="summary-row">
              <span class="label">订单编号</span>
              <span class="value mono">{{ order.orderNo }}</span>
            </div>
            <div class="summary-row amount-row">
              <span class="label">应付金额</span>
              <span class="amount">¥{{ order.payAmount ?? order.totalAmount }}</span>
            </div>
          </div>

          <div class="pay-methods-block">
            <h3>支付方式</h3>
            <el-radio-group v-model="payMethod" class="pay-methods">
              <label
                v-for="m in payMethods"
                :key="m.value"
                class="pay-method-item"
                :class="{ active: payMethod === m.value }"
              >
                <el-radio :label="m.value" class="pay-radio">
                  <span class="method-icon">{{ m.icon }}</span>
                  <span class="method-name">{{ m.label }}</span>
                  <span class="method-desc">{{ m.desc }}</span>
                </el-radio>
              </label>
            </el-radio-group>
          </div>

          <div v-if="payMethod === 4" class="voucher-block">
            <h3>对公转账信息</h3>
            <p class="transfer-tip">户名：医药订货平台 · 开户行：中国工商银行某某支行 · 账号：6222 **** **** 1234</p>
            <el-input v-model="transferRemark" type="textarea" :rows="2" placeholder="转账备注（选填）" />
            <el-input v-model="voucherUrl" placeholder="付款凭证图片 URL" class="voucher-input" />
          </motion>

          <div class="pay-actions">
            <el-button size="large" @click="router.back()">返回</el-button>
            <el-button type="primary" size="large" :loading="paying" @click="handlePay">
              {{ payMethod === 4 ? '提交付款凭证' : `立即支付 ¥${order.payAmount ?? order.totalAmount}` }}
            </el-button>
          </motion>
        </template>
        <el-empty v-else-if="!loading" description="订单不存在或已失效" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/consumer/order'
import { createPayment, confirmPayment, submitPaymentVoucher } from '@/api/consumer/payment'
import { cPath } from '@/utils/consumer-path'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const order = ref(null)
const payMethod = ref(4)
const voucherUrl = ref('')
const transferRemark = ref('')

const payMethods = [
  { value: 4, label: '对公转账', desc: '企业对公账户转账', icon: '公' },
  { value: 1, label: '微信支付', desc: '预计 1–3 分钟到账', icon: '微' },
  { value: 2, label: '支付宝', desc: '安全快捷在线支付', icon: '支' }
]

onMounted(async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.orderId)
  } catch {
    ElMessage.error('加载订单失败')
    router.back()
  } finally {
    loading.value = false
  }
})

async function handlePay() {
  paying.value = true
  try {
    const payment = await createPayment({
      orderId: order.value.id,
      paymentMethod: payMethod.value
    })
    if (payMethod.value === 4) {
      if (!voucherUrl.value?.trim()) {
        ElMessage.warning('请填写付款凭证图片地址')
        return
      }
      await submitPaymentVoucher({
        paymentId: payment.id,
        voucherUrl: voucherUrl.value.trim(),
        transferRemark: transferRemark.value
      })
      ElMessage.success('凭证已提交，请等待财务审核')
    } else {
      await confirmPayment({ paymentId: payment.id })
      ElMessage.success('支付成功')
    }
    router.push(cPath(`order/${order.value.id}`))
  } catch (e) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.pay-page {
  padding-bottom: 48px;
}

.pay-card {
  max-width: 640px;
  margin: 0 auto;
  border-radius: 16px;
  padding: 8px 4px 24px;
}

.order-summary {
  padding: 20px 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #f8fafc 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}

.summary-row .label {
  color: var(--c-text-secondary, #64748b);
}

.summary-row .value.mono {
  font-family: ui-monospace, monospace;
  color: var(--c-text, #1e293b);
  word-break: break-all;
  text-align: right;
  max-width: 65%;
}

.amount-row {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px dashed #cbd5e1;
}

.amount {
  font-size: 32px;
  font-weight: 700;
  color: #dc2626;
}

.pay-methods-block h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: var(--c-text, #1e293b);
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.pay-method-item {
  display: block;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 4px 16px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.pay-method-item.active {
  border-color: var(--el-color-primary, #0d6e9f);
  background: #f0f9ff;
}

.pay-radio {
  width: 100%;
  height: auto;
  padding: 12px 0;
  align-items: flex-start;
}

.pay-radio :deep(.el-radio__label) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
  padding-left: 8px;
}

.method-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #0d6e9f;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.method-name {
  font-weight: 600;
  color: #1e293b;
}

.method-desc {
  width: 100%;
  font-size: 12px;
  color: #94a3b8;
  margin-left: 44px;
}

.pay-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}
</style>
