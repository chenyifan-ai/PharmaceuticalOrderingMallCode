<template>
  <div class="invoice-preview" v-if="invoice">
    <div class="invoice-paper">
      <header class="invoice-head">
        <div class="head-left">
          <p class="invoice-type">增值税电子普通发票</p>
          <h2>发票预览</h2>
        </div>
        <div class="head-right">
          <p><span>发票代码</span>{{ invoice.invoiceNo ? codePart : '待开具' }}</p>
          <p><span>发票号码</span>{{ invoice.invoiceNo || '待开具' }}</p>
          <p><span>开票日期</span>{{ issueDate }}</p>
        </div>
      </header>

      <section class="invoice-block buyer">
        <div class="block-title">购买方</div>
        <div class="block-body">
          <p><label>名称</label><span class="cell-ellipsis" :title="invoice.invoiceTitle">{{ invoice.invoiceTitle }}</span></p>
          <p><label>纳税人识别号</label><span>{{ invoice.taxNumber || '—' }}</span></p>
        </div>
      </section>

      <section class="invoice-table-wrap">
        <table class="invoice-table">
          <thead>
            <tr>
              <th>货物或应税劳务名称</th>
              <th>规格型号</th>
              <th>单位</th>
              <th>数量</th>
              <th>金额</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="cell-ellipsis" :title="invoice.invoiceContent">{{ invoice.invoiceContent || '医药商品' }}</td>
              <td>—</td>
              <td>批</td>
              <td>1</td>
              <td class="amount">¥{{ formatAmount(invoice.amount) }}</td>
            </tr>
          </tbody>
        </table>
        <p class="total-line">
          <span>价税合计（大写）</span>
          <strong>{{ amountUpper }}</strong>
          <span class="num">（小写）¥{{ formatAmount(invoice.amount) }}</span>
        </p>
      </section>

      <section class="invoice-block seller">
        <div class="block-title">销售方</div>
        <div class="block-body">
          <p><label>名称</label><span>医药订货平台（演示）</span></p>
          <p><label>订单号</label><span>{{ invoice.orderNo }}</span></p>
        </div>
      </section>

      <footer class="invoice-foot">
        <el-tag :type="statusTag" size="small">{{ statusName }}</el-tag>
        <span v-if="invoice.receiverName">收件：{{ invoice.receiverName }} {{ invoice.receiverPhone }}</span>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps({
  invoice: { type: Object, default: null }
})

const statusMap = {
  0: { name: '待开票', tag: 'warning' },
  1: { name: '已开票', tag: 'success' },
  2: { name: '已寄送', tag: 'primary' },
  3: { name: '已作废', tag: 'info' }
}

const statusName = computed(() => statusMap[props.invoice?.status]?.name ?? '未知')
const statusTag = computed(() => statusMap[props.invoice?.status]?.tag ?? '')

const issueDate = computed(() => {
  if (props.invoice?.invoiceTime) return formatDate(props.invoice.invoiceTime)
  return formatDate(new Date())
})

const codePart = computed(() => {
  const no = props.invoice?.invoiceNo || ''
  return no.length > 8 ? no.slice(0, 8) : no
})

function formatAmount(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function amountToChinese(n) {
  const num = Number(n)
  if (!Number.isFinite(num) || num <= 0) return '零元整'
  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const units = ['', '拾', '佰', '仟']
  const bigUnits = ['', '万', '亿']
  const intPart = Math.floor(num)
  const dec = Math.round((num - intPart) * 100)
  let result = ''
  if (intPart === 0) {
    result = '零'
  } else {
    const s = String(intPart)
    for (let i = 0; i < s.length; i++) {
      const d = +s[i]
      const pos = s.length - 1 - i
      const u = units[pos % 4]
      const bu = pos % 4 === 0 ? bigUnits[Math.floor(pos / 4)] : ''
      if (d === 0) {
        if (!result.endsWith('零') && pos !== 0) result += '零'
      } else {
        result += digits[d] + u + bu
      }
    }
  }
  result += '元'
  if (dec > 0) {
    const j = Math.floor(dec / 10)
    const f = dec % 10
    if (j > 0) result += digits[j] + '角'
    if (f > 0) result += digits[f] + '分'
  } else {
    result += '整'
  }
  return result
}

const amountUpper = computed(() => amountToChinese(props.invoice?.amount))
</script>

<style scoped>
.invoice-preview {
  padding: 8px 0;
}

.invoice-paper {
  border: 2px solid #c9a227;
  border-radius: 4px;
  padding: 20px 24px;
  background: linear-gradient(180deg, #fffef8 0%, #fff 100%);
  color: #4a3728;
  font-size: 13px;
}

.invoice-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px dashed #d4b896;
  padding-bottom: 12px;
  margin-bottom: 16px;
}

.invoice-type {
  margin: 0;
  font-size: 12px;
  color: #8b4513;
  letter-spacing: 2px;
}

.invoice-head h2 {
  margin: 4px 0 0;
  font-size: 20px;
  color: #8b2500;
}

.head-right p {
  margin: 4px 0;
  text-align: right;
  font-size: 12px;
}

.head-right span {
  color: #888;
  margin-right: 8px;
}

.invoice-block {
  margin-bottom: 12px;
}

.block-title {
  font-weight: 600;
  color: #8b2500;
  margin-bottom: 6px;
  font-size: 12px;
}

.block-body p {
  margin: 4px 0;
  display: flex;
  gap: 8px;
}

.block-body label {
  flex-shrink: 0;
  width: 96px;
  color: #888;
}

.invoice-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 8px;
}

.invoice-table th,
.invoice-table td {
  border: 1px solid #d4b896;
  padding: 8px 10px;
  text-align: left;
}

.invoice-table th {
  background: #faf6ee;
  font-weight: 600;
  font-size: 12px;
}

.amount {
  color: #c41e3a;
  font-weight: 600;
  white-space: nowrap;
}

.total-line {
  text-align: right;
  margin: 8px 0 16px;
  font-size: 13px;
}

.total-line strong {
  color: #8b2500;
  margin: 0 12px;
}

.total-line .num {
  color: #c41e3a;
  font-weight: 600;
}

.invoice-foot {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px dashed #d4b896;
  font-size: 12px;
  color: #666;
}
</style>
