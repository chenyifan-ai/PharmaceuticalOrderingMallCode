<template>
  <div class="order-invoice-section">
    <h4 class="section-title">{{ title }}</h4>
    <template v-if="hasInvoiceInfo(order)">
      <el-descriptions :column="column" border size="small" class="invoice-desc">
        <el-descriptions-item label="发票抬头">
          {{ displayTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="纳税人识别号">
          {{ displayTaxNo }}
        </el-descriptions-item>
        <el-descriptions-item v-if="inv" label="发票类型">
          <el-tag size="small" :type="inv.invoiceType === 1 ? 'success' : 'warning'">
            {{ invoiceTypeLabel(inv.invoiceType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开票状态">
          <el-tag size="small" :type="statusTag">{{ statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="inv?.invoiceNo" label="发票号码">
          {{ inv.invoiceNo }}
        </el-descriptions-item>
        <el-descriptions-item v-if="inv?.amount != null" label="发票金额">
          ¥{{ inv.amount }}
        </el-descriptions-item>
        <el-descriptions-item v-if="inv?.invoiceContent" label="发票内容" :span="column">
          {{ inv.invoiceContent }}
        </el-descriptions-item>
        <el-descriptions-item v-if="inv?.invoiceTime" label="开票时间">
          {{ formatDateTime(inv.invoiceTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="inv?.sendTime" label="寄送时间">
          {{ formatDateTime(inv.sendTime) }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="inv?.logisticsCompany || inv?.logisticsNo"
          label="发票物流"
          :span="column"
        >
          {{ inv.logisticsCompany || '—' }}
          <template v-if="inv.logisticsNo"> · {{ inv.logisticsNo }}</template>
        </el-descriptions-item>
        <el-descriptions-item
          v-if="inv?.receiverName || inv?.receiverAddress"
          label="发票收件"
          :span="column"
        >
          <span v-if="inv.receiverName">{{ inv.receiverName }}</span>
          <span v-if="inv.receiverPhone"> {{ inv.receiverPhone }}</span>
          <div v-if="inv.receiverAddress" class="sub-line">{{ inv.receiverAddress }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="inv?.remark" label="备注" :span="column">
          {{ inv.remark }}
        </el-descriptions-item>
      </el-descriptions>
    </template>
    <el-empty v-else description="该订单未填写发票信息" :image-size="64" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDateTime } from '@/utils/format'
import {
  hasInvoiceInfo,
  invoiceTypeLabel,
  invoiceStatusLabel,
  invoiceStatusTag,
  orderInvoiceStatusLabel,
  orderInvoiceStatusTag
} from '@/utils/invoice'

const props = defineProps({
  order: { type: Object, default: null },
  title: { type: String, default: '发票信息' },
  column: { type: Number, default: 2 }
})

const inv = computed(() => props.order?.invoice)

const displayTitle = computed(
  () => inv.value?.invoiceTitle || props.order?.invoiceTitle || '—'
)

const displayTaxNo = computed(
  () => inv.value?.taxNumber || props.order?.invoiceTaxNo || '—'
)

const statusText = computed(() => {
  if (inv.value != null && inv.value.status != null) {
    return invoiceStatusLabel(inv.value.status)
  }
  return orderInvoiceStatusLabel(props.order?.invoiceStatus)
})

const statusTag = computed(() => {
  if (inv.value != null && inv.value.status != null) {
    return invoiceStatusTag(inv.value.status)
  }
  return orderInvoiceStatusTag(props.order?.invoiceStatus)
})
</script>

<style scoped>
.order-invoice-section {
  margin-top: 4px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.invoice-desc {
  width: 100%;
}

.sub-line {
  margin-top: 4px;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}
</style>
