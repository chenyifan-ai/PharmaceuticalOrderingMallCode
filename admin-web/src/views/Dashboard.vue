<template>
  <div class="data-screen">
    <div class="screen-bg">
      <div class="bg-grid"></div>
      <div class="bg-glow bg-glow-left"></div>
      <div class="bg-glow bg-glow-right"></div>
      <div class="bg-glow bg-glow-center"></div>
      <div class="bg-hex"></div>
      <div class="light-pillar light-pillar-l"></div>
      <div class="light-pillar light-pillar-r"></div>
      <div class="particles">
        <span v-for="i in 40" :key="i" class="particle" :style="particleStyle(i)"></span>
      </div>
      <div class="orbit-dots">
        <span v-for="n in 8" :key="n" class="orbit-dot" :style="{ '--orbit-i': n }"></span>
      </div>
    </div>
    <div class="screen-frame"></div>
    <div class="scan-line"></div>

    <!-- 顶部标题 -->
    <header class="screen-header">
      <div class="header-deco header-deco-left"></div>
      <div class="header-center">
        <h1 class="screen-title">医药订货系统 · 数据可视化大屏</h1>
        <p class="screen-subtitle">PHARMACY ORDER DATA VISUALIZATION</p>
      </div>
      <div class="header-deco header-deco-right"></div>
      <div class="header-time">
        <span class="time-label">系统时间</span>
        <span class="time-value">{{ currentTime }}</span>
      </div>
    </header>

    <!-- 三栏主体 -->
    <div class="screen-body">
      <!-- 左侧 -->
      <aside class="col col-left">
        <div class="panel">
          <div class="panel-corner tl"></div>
          <div class="panel-corner tr"></div>
          <div class="panel-corner bl"></div>
          <div class="panel-corner br"></div>
          <div class="panel-title"><span class="title-bar"></span>核心指标</div>
          <div class="gauge-row">
            <div ref="gauge1Ref" class="gauge-chart"></div>
            <div ref="gauge2Ref" class="gauge-chart"></div>
          </div>
        </div>

        <div class="panel panel-flex">
          <div class="panel-corner tl"></div>
          <div class="panel-corner tr"></div>
          <div class="panel-corner bl"></div>
          <div class="panel-corner br"></div>
          <div class="panel-title"><span class="title-bar"></span>品类销售占比</div>
          <div ref="categoryChartRef" class="chart-area"></div>
        </div>

        <div class="panel panel-flex">
          <div class="panel-corner tl"></div>
          <div class="panel-corner tr"></div>
          <div class="panel-corner bl"></div>
          <div class="panel-corner br"></div>
          <div class="panel-title"><span class="title-bar"></span>月度订单统计</div>
          <div ref="monthBarRef" class="chart-area"></div>
        </div>
      </aside>

      <!-- 中间 -->
      <main class="col col-center">
        <div class="center-tabs">
          <button
            v-for="tab in periodTabs"
            :key="tab.value"
            class="tab-btn"
            :class="{ active: salesPeriod === tab.value }"
            @click="changePeriod(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="center-stage">
          <div class="stage-grid"></div>
          <div class="stage-ring ring-1"></div>
          <div class="stage-ring ring-2"></div>
          <div class="stage-ring ring-3"></div>
          <div class="stage-platform"></div>
          <div class="hub-orb"></div>
          <div class="hub-cube">
            <div class="cube-face front"></div>
            <div class="cube-face back"></div>
            <div class="cube-face left"></div>
            <div class="cube-face right"></div>
            <div class="cube-face top"></div>
            <div class="cube-face bottom"></div>
          </div>
          <div class="flow-lines">
            <span v-for="n in 6" :key="n" class="flow-line" :style="{ '--i': n }"></span>
          </div>
        </div>

        <div class="center-kpis">
          <div v-for="(kpi, i) in centerKpis" :key="i" class="kpi-item">
            <div class="kpi-value">{{ kpi.value }}</div>
            <div class="kpi-label">{{ kpi.label }}</div>
            <div v-if="kpi.sub" class="kpi-sub">{{ kpi.sub }}</div>
          </div>
        </div>

        <div class="panel panel-orders">
          <div class="panel-corner tl"></div>
          <div class="panel-corner tr"></div>
          <div class="panel-corner bl"></div>
          <div class="panel-corner br"></div>
          <div class="panel-title panel-title-row">
            <span class="title-bar"></span>
            <span class="title-text">实时订单滚动</span>
            <span v-if="recentOrders.length" class="order-live-badge">
              <i class="live-dot" aria-hidden="true"></i>
              LIVE · {{ recentOrders.length }}
            </span>
          </div>
          <div class="order-scroll">
            <div class="order-scroll-frame">
            <div class="order-scroll-header">
              <span class="col-idx">#</span>
              <span class="col-no">订单号 / 时间</span>
              <span class="col-customer">客户</span>
              <span class="col-product">商品</span>
              <span class="col-amount">金额</span>
              <span class="col-status">状态</span>
            </div>
            <div class="order-scroll-body">
              <div v-if="!recentOrders.length" class="order-scroll-empty">
                <span class="empty-icon">◇</span>
                <span>暂无订单数据</span>
              </div>
              <div
                v-else
                class="order-scroll-track"
                :style="{ '--scroll-duration': scrollDuration + 's' }"
              >
              <div
                v-for="(order, idx) in scrollOrders"
                :key="order.orderNo + '-' + idx"
                class="order-scroll-item"
                :class="getStatusClass(order.status)"
              >
                <span class="col-idx">{{ (idx % recentOrders.length) + 1 }}</span>
                <span class="col-no" :title="order.orderNo">
                  <span class="order-no-text cell-ellipsis">{{ order.orderNo }}</span>
                  <span v-if="order.createTime" class="order-time">{{ formatOrderTime(order.createTime) }}</span>
                </span>
                <span class="col-customer cell-ellipsis" :title="order.receiverName">
                  {{ order.receiverName || '—' }}
                </span>
                <span class="col-product cell-ellipsis" :title="order.productName">
                  {{ order.productName || '—' }}
                </span>
                <span class="col-amount">
                  <em>¥</em>{{ formatPayAmount(order.payAmount) }}
                </span>
                <span class="col-status">
                  <span class="status-pill" :class="getStatusClass(order.status)">
                    <i class="status-dot" aria-hidden="true"></i>
                    {{ getStatusName(order.status) }}
                  </span>
                </span>
                </div>
              </div>
            </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 右侧 -->
      <aside class="col col-right">
        <div class="panel panel-flex-lg">
          <div class="panel-corner tl"></div>
          <div class="panel-corner tr"></div>
          <div class="panel-corner bl"></div>
          <div class="panel-corner br"></div>
          <div class="panel-title">
            <span class="title-bar"></span>销售趋势分析
            <span class="panel-tag">实时</span>
          </div>
          <div ref="salesChartRef" class="chart-area"></div>
        </div>

        <div class="panel panel-flex">
          <div class="panel-corner tl"></div>
          <div class="panel-corner tr"></div>
          <div class="panel-corner bl"></div>
          <div class="panel-corner br"></div>
          <div class="panel-title"><span class="title-bar"></span>商品销售排行 TOP8</div>
          <div ref="rankBarRef" class="chart-area"></div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/admin'
const currentTime = ref('')
const salesPeriod = ref('week')
let timeTimer = null
let refreshTimer = null

const gauge1Ref = ref(null)
const gauge2Ref = ref(null)
const categoryChartRef = ref(null)
const monthBarRef = ref(null)
const salesChartRef = ref(null)
const rankBarRef = ref(null)

let gauge1Chart = null
let gauge2Chart = null
let categoryChart = null
let monthBarChart = null
let salesChart = null
let rankBarChart = null

const periodTabs = [
  { label: '近7天', value: 'week' },
  { label: '近30天', value: 'month' },
  { label: '本季度', value: 'quarter' }
]

const stats = reactive({
  orderTotal: 0,
  orderCompleted: 0,
  revenue: 0,
  productCount: 0,
  merchantCount: 0,
  userCount: 0,
  completionRate: 0,
  refundRate: 0
})

const recentOrders = ref([])

const centerKpis = computed(() => [
  { label: '订单总量', value: formatNum(stats.orderTotal), sub: `完成 ${stats.orderCompleted} 笔` },
  { label: '累计营收', value: '¥' + formatMoney(stats.revenue), sub: `完成率 ${stats.completionRate}%` },
  { label: '在售商品', value: formatNum(stats.productCount), sub: `平台用户 ${formatNum(stats.userCount)}` },
  { label: '合作供应商', value: formatNum(stats.merchantCount), sub: '实时统计' }
])

const scrollOrders = computed(() => {
  const list = recentOrders.value || []
  if (!list.length) return []
  return [...list, ...list]
})

const scrollDuration = computed(() => {
  const n = recentOrders.value?.length || 0
  return Math.max(18, Math.min(48, n * 2.8))
})

function formatOrderTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const CYAN = '#00e5ff'
const BLUE = '#1890ff'
const GOLD = '#ffc53d'
const ORANGE = '#ff7a45'

const chartColors = [CYAN, BLUE, GOLD, ORANGE, '#73d13d', '#9254de', '#36cfc9', '#ff85c0']

function formatNum(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n ?? 0)
}

function formatMoney(n) {
  const v = Number(n) || 0
  if (v >= 10000) return (v / 10000).toFixed(2) + '万'
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

function formatPayAmount(n) {
  const v = Number(n)
  if (Number.isNaN(v)) return '0.00'
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getStatusName(s) {
  const map = { 0: '待付款', 1: '待审核', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
  return map[s] ?? '未知'
}

function getStatusClass(s) {
  const map = {
    0: 'status-pending-pay',
    1: 'status-pending-audit',
    2: 'status-pending-ship',
    3: 'status-shipped',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return map[s] ?? 'status-unknown'
}

const particleStyle = (i) => {
  const left = (i * 17 + 7) % 100
  const top = (i * 23 + 11) % 100
  const delay = (i % 10) * 0.4
  const size = 2 + (i % 3)
  return {
    left: `${left}%`,
    top: `${top}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`
  }
}

const updateTime = () => {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const baseTextStyle = { color: 'rgba(0, 229, 255, 0.75)', fontSize: 11 }

function initGauge(el, value, name, color) {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      radius: '90%',
      pointer: { show: false },
      progress: {
        show: true,
        overlap: false,
        roundCap: true,
        clip: false,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: color },
            { offset: 1, color: CYAN }
          ]),
          shadowBlur: 12,
          shadowColor: color
        }
      },
      axisLine: {
        lineStyle: {
          width: 10,
          color: [[1, 'rgba(0, 229, 255, 0.12)']]
        }
      },
      splitLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false },
      title: {
        offsetCenter: [0, '72%'],
        fontSize: 11,
        color: 'rgba(0, 229, 255, 0.7)'
      },
      detail: {
        valueAnimation: true,
        offsetCenter: [0, '8%'],
        fontSize: 22,
        fontWeight: 700,
        color: '#fff',
        formatter: '{value}%'
      },
      data: [{ value, name }]
    }]
  })
  return chart
}

function initCategoryChart() {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(2, 20, 40, 0.92)',
      borderColor: CYAN,
      textStyle: { color: '#fff', fontSize: 12 }
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      textStyle: { ...baseTextStyle, fontSize: 10 },
      itemWidth: 8,
      itemHeight: 8,
      type: 'scroll',
      width: '92%'
    },
    series: [{
      type: 'pie',
      radius: ['36%', '58%'],
      center: ['50%', '42%'],
      padAngle: 4,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#020a14',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, color: '#fff', fontSize: 12 },
        itemStyle: { shadowBlur: 16, shadowColor: 'rgba(0,229,255,0.5)' }
      },
      data: []
    }]
  })
}

function initMonthBar() {
  if (!monthBarRef.value) return
  monthBarChart = echarts.init(monthBarRef.value)
  monthBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(2, 20, 40, 0.92)',
      borderColor: CYAN,
      textStyle: { color: '#fff' }
    },
    grid: { left: 8, right: 8, top: 16, bottom: 28, containLabel: true },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: { lineStyle: { color: 'rgba(0,229,255,0.2)' } },
      axisLabel: baseTextStyle
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(0,229,255,0.08)', type: 'dashed' } },
      axisLabel: baseTextStyle
    },
    series: [{
      type: 'bar',
      barWidth: '45%',
      data: [],
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: (p) => chartColors[p.dataIndex % chartColors.length],
        shadowBlur: 10,
        shadowColor: 'rgba(0, 229, 255, 0.45)',
        shadowOffsetY: 4
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 18,
          shadowColor: 'rgba(0, 229, 255, 0.65)'
        }
      }
    }]
  })
}

function initSalesChart() {
  if (!salesChartRef.value) return
  salesChart = echarts.init(salesChartRef.value)
  updateSalesChart()
}

function initRankBar() {
  if (!rankBarRef.value) return
  rankBarChart = echarts.init(rankBarRef.value)
  rankBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(2, 20, 40, 0.92)',
      borderColor: CYAN,
      textStyle: { color: '#fff' }
    },
    grid: { left: 6, right: 10, top: 8, bottom: 16, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(0,229,255,0.08)', type: 'dashed' } },
      axisLabel: { ...baseTextStyle, fontSize: 10 }
    },
    yAxis: {
      type: 'category',
      data: [],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: 'rgba(0,229,255,0.85)', fontSize: 10, width: 72, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      data: [],
      barWidth: 10,
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: 'rgba(0, 229, 255, 0.3)' },
          { offset: 1, color: CYAN }
        ])
      }
    }]
  })
}

const salesDataMap = reactive({
  week: { x: [], sales: [] },
  month: { x: [], sales: [] },
  quarter: { x: [], sales: [] }
})

function buildSalesChartOption() {
  const d = salesDataMap[salesPeriod.value]
  const xData = d.x?.length ? d.x : ['暂无数据']
  const salesData = d.sales?.length ? d.sales : [0]
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(2, 20, 40, 0.75)',
      borderColor: CYAN,
      textStyle: { color: '#fff' }
    },
    legend: {
      data: ['销售额'],
      right: 8,
      top: 0,
      textStyle: baseTextStyle,
      itemWidth: 12,
      itemHeight: 8
    },
    grid: { left: 48, right: 24, top: 36, bottom: 28 },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: 'rgba(0,229,255,0.25)' } },
      axisLabel: baseTextStyle
    },
    yAxis: {
      type: 'value',
      name: '销售额(元)',
      nameTextStyle: baseTextStyle,
      splitLine: { lineStyle: { color: 'rgba(0,229,255,0.08)', type: 'dashed' } },
      axisLabel: { ...baseTextStyle, formatter: (v) => (v >= 10000 ? v / 10000 + '万' : v) }
    },
    series: [
      {
        id: 'salesAmount',
        name: '销售额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: CYAN, shadowBlur: 8, shadowColor: CYAN },
        itemStyle: { color: CYAN },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 229, 255, 0.28)' },
            { offset: 1, color: 'rgba(0, 229, 255, 0.02)' }
          ])
        },
        data: salesData
      }
    ]
  }
}

function updateSalesChart() {
  if (!salesChart) return
  salesChart.setOption(buildSalesChartOption(), { notMerge: true })
}

function changePeriod(val) {
  salesPeriod.value = val
  updateSalesChart()
}

function applyTrend(points, key) {
  if (!points?.length) return
  salesDataMap[key] = {
    x: points.map((p) => p.label),
    sales: points.map((p) => Number(p.sales) || 0)
  }
}

function applyCategoryChartData(list) {
  if (!categoryChart || !list?.length) return
  const data = list.map((item, i) => ({
    name: item.name,
    value: Number(item.value) || 0,
    itemStyle: { color: chartColors[i % chartColors.length] }
  }))
  categoryChart.setOption({ series: [{ data }] })
}

function applyMonthBarData(list) {
  if (!monthBarChart || !list?.length) return
  monthBarChart.setOption({
    xAxis: { data: list.map((m) => m.month) },
    series: [{ data: list.map((m) => Number(m.orders) || 0) }]
  })
}

function applyRankBarData(list) {
  if (!rankBarChart || !list?.length) return
  const names = list.map((r) => r.name)
  const values = list.map((r) => Number(r.value) || 0)
  rankBarChart.setOption({
    yAxis: { data: [...names].reverse() },
    series: [{ data: [...values].reverse() }]
  })
}

async function loadDashboardData() {
  try {
    const dash = await getDashboardStats()

    stats.orderTotal = dash.orderTotal ?? 0
    stats.orderCompleted = dash.orderCompleted ?? 0
    stats.revenue = dash.revenue ?? 0
    stats.completionRate = dash.completionRate ?? 0
    stats.refundRate = dash.refundRate ?? 0
    stats.productCount = dash.productCount ?? 0
    stats.merchantCount = dash.merchantCount ?? 0
    stats.userCount = dash.userCount ?? 0

    applyTrend(dash.salesTrendWeek, 'week')
    applyTrend(dash.salesTrendMonth, 'month')
    applyTrend(dash.salesTrendQuarter, 'quarter')
    applyCategoryChartData(dash.categorySales)
    applyMonthBarData(dash.monthlyOrders)
    applyRankBarData(dash.productRanks)
    updateSalesChart()

    gauge1Chart?.setOption({
      series: [{ data: [{ value: stats.completionRate, name: '订单完成率' }] }]
    })
    gauge2Chart?.setOption({
      series: [{ data: [{ value: stats.refundRate, name: '退款率' }] }]
    })

    recentOrders.value = (dash.recentOrders || []).map((o) => ({
      orderNo: o.orderNo,
      receiverName: o.receiverName,
      productName: o.productName || '多商品订单',
      payAmount: o.payAmount,
      status: o.status,
      createTime: o.createTime
    }))
  } catch {
    recentOrders.value = []
  }
}

const handleResize = () => {
  gauge1Chart?.resize()
  gauge2Chart?.resize()
  categoryChart?.resize()
  monthBarChart?.resize()
  salesChart?.resize()
  rankBarChart?.resize()
}

onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  nextTick(() => {
    gauge1Chart = initGauge(gauge1Ref.value, 0, '订单完成率', CYAN)
    gauge2Chart = initGauge(gauge2Ref.value, 0, '退款率', ORANGE)
    initCategoryChart()
    initMonthBar()
    initSalesChart()
    initRankBar()
    loadDashboardData()
    refreshTimer = setInterval(loadDashboardData, 60000)
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  clearInterval(timeTimer)
  clearInterval(refreshTimer)
  window.removeEventListener('resize', handleResize)
  ;[gauge1Chart, gauge2Chart, categoryChart, monthBarChart, salesChart, rankBarChart].forEach((c) => {
    c?.dispose()
  })
  gauge1Chart = gauge2Chart = categoryChart = monthBarChart = salesChart = rankBarChart = null
})
</script>

<style scoped>
.data-screen {
  position: relative;
  width: 100%;
  max-width: 100%;
  height: calc(100vh - 60px);
  min-height: 640px;
  overflow: hidden;
  box-sizing: border-box;
  color: #e6f7ff;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background: radial-gradient(ellipse at 50% 0%, rgba(0, 60, 120, 0.35) 0%, rgba(2, 8, 20, 0.95) 55%, #01060f 100%);
}

/* 背景 */
.screen-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 229, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 229, 255, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.7;
}
.bg-glow {
  position: absolute;
  width: 50%;
  height: 100%;
  opacity: 0.15;
}
.bg-glow-left {
  left: 0;
  background: radial-gradient(ellipse at left center, rgba(0, 229, 255, 0.4), transparent 70%);
}
.bg-glow-right {
  right: 0;
  background: radial-gradient(ellipse at right center, rgba(24, 144, 255, 0.35), transparent 70%);
}
.bg-glow-center {
  left: 50%;
  top: 40%;
  transform: translate(-50%, -50%);
  width: 60%;
  height: 50%;
  background: radial-gradient(ellipse, rgba(0, 229, 255, 0.2), transparent 65%);
  opacity: 0.8;
}

.bg-hex {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='56' height='100' viewBox='0 0 56 100'%3E%3Cpath d='M28 0L56 16v32L28 64 0 48V16z' fill='none' stroke='%2300e5ff' stroke-width='0.6'/%3E%3C/svg%3E");
  background-size: 56px 100px;
  animation: hex-drift 40s linear infinite;
}

@keyframes hex-drift {
  from { background-position: 0 0; }
  to { background-position: 56px 100px; }
}

.light-pillar {
  position: absolute;
  top: 10%;
  bottom: 15%;
  width: 2px;
  background: linear-gradient(180deg, transparent, rgba(0, 229, 255, 0.85), transparent);
  box-shadow: 0 0 24px rgba(0, 229, 255, 0.6);
  animation: pillar-pulse 3s ease-in-out infinite;
}

.light-pillar-l {
  left: 26%;
  animation-delay: 0s;
}

.light-pillar-r {
  right: 26%;
  animation-delay: 1.5s;
}

@media (max-width: 1500px) {
  .light-pillar-l { left: 28%; }
  .light-pillar-r { right: 28%; }
}

@media (max-width: 1280px) {
  .light-pillar {
    opacity: 0.4;
  }
}

@keyframes pillar-pulse {
  0%, 100% { opacity: 0.35; transform: scaleY(0.92); }
  50% { opacity: 1; transform: scaleY(1); }
}

.orbit-dots {
  position: absolute;
  left: 50%;
  top: 42%;
  width: 0;
  height: 0;
  transform-style: preserve-3d;
}

.orbit-dot {
  position: absolute;
  width: 6px;
  height: 6px;
  margin: -3px 0 0 -3px;
  border-radius: 50%;
  background: #00e5ff;
  box-shadow: 0 0 10px #00e5ff;
  animation: orbit-spin 18s linear infinite;
  animation-delay: calc(var(--orbit-i) * -2.25s);
  transform: rotate(calc(var(--orbit-i) * 45deg)) translateX(180px) rotate(calc(var(--orbit-i) * -45deg));
}

@keyframes orbit-spin {
  to { transform: rotate(calc(var(--orbit-i) * 45deg + 360deg)) translateX(180px) rotate(calc(var(--orbit-i) * -45deg - 360deg)); }
}

/* 粒子与边框 */
.particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.particle {
  position: absolute;
  border-radius: 50%;
  background: #00e5ff;
  box-shadow: 0 0 6px #00e5ff, 0 0 12px rgba(0, 229, 255, 0.5);
  animation: particle-float 4s ease-in-out infinite;
  opacity: 0.5;
}
@keyframes particle-float {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.3; }
  50% { transform: translateY(-12px) scale(1.3); opacity: 0.9; }
}

.screen-frame {
  position: absolute;
  inset: 4px;
  border: 1px solid rgba(0, 229, 255, 0.18);
  box-shadow:
    inset 0 0 80px rgba(0, 229, 255, 0.04),
    0 0 40px rgba(0, 100, 200, 0.1);
  pointer-events: none;
  z-index: 1;
  background: rgba(0, 20, 40, 0.08);
}
.screen-frame::before,
.screen-frame::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 24px;
  border-color: #00e5ff;
  border-style: solid;
}
.screen-frame::before {
  top: -1px;
  left: -1px;
  border-width: 2px 0 0 2px;
}
.screen-frame::after {
  bottom: -1px;
  right: -1px;
  border-width: 0 2px 2px 0;
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.8), transparent);
  box-shadow: 0 0 20px #00e5ff;
  animation: scan-move 6s linear infinite;
  z-index: 3;
  pointer-events: none;
  opacity: 0.6;
}
@keyframes scan-move {
  0% { top: 8%; }
  100% { top: 92%; }
}

/* 顶部标题 */
.screen-header {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  margin: 0 16px;
  border-bottom: 1px solid rgba(0, 229, 255, 0.2);
  background: linear-gradient(180deg, rgba(0, 40, 80, 0.35) 0%, transparent 100%);
  backdrop-filter: blur(12px);
}
.header-center {
  text-align: center;
}
.screen-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 6px;
  background: linear-gradient(90deg, #e6f7ff 0%, #00e5ff 45%, #7dd3fc 55%, #e6f7ff 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: title-shine 6s linear infinite;
}

@keyframes title-shine {
  to {
    background-position: 200% center;
  }
}
.screen-subtitle {
  margin: 2px 0 0;
  font-size: 10px;
  letter-spacing: 6px;
  color: rgba(0, 229, 255, 0.45);
}
.header-deco {
  position: absolute;
  top: 50%;
  width: 120px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00e5ff, transparent);
}
.header-deco-left { left: 80px; }
.header-deco-right { right: 80px; }
.header-time {
  position: absolute;
  right: 20px;
  text-align: right;
}
.time-label {
  display: block;
  font-size: 10px;
  color: rgba(0, 229, 255, 0.5);
}
.time-value {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #00e5ff;
}

/* 三栏布局 */
.screen-body {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(240px, 23%) minmax(0, 1fr) minmax(240px, 23%);
  gap: 10px;
  height: calc(100% - 64px);
  padding: 8px 14px 12px;
  box-sizing: border-box;
  min-width: 0;
  width: 100%;
}

.col-left,
.col-right,
.col-center {
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  width: 100%;
}

/* 面板霓虹边框 */
.panel {
  position: relative;
  flex-shrink: 0;
  padding: 10px 12px 12px;
  background: linear-gradient(
    145deg,
    rgba(6, 28, 56, 0.42) 0%,
    rgba(4, 18, 38, 0.28) 50%,
    rgba(2, 12, 28, 0.35) 100%
  );
  border: 1px solid rgba(0, 229, 255, 0.28);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    inset 0 0 30px rgba(0, 100, 180, 0.04);
  backdrop-filter: blur(16px) saturate(1.2);
  -webkit-backdrop-filter: blur(16px) saturate(1.2);
  transform-style: preserve-3d;
  transition: transform 0.35s ease, box-shadow 0.35s ease, border-color 0.35s ease;
  animation: panel-glow 5s ease-in-out infinite;
}
.panel::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.5), transparent 40%, rgba(0, 229, 255, 0.2));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
  opacity: 0.7;
}
.panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 229, 255, 0.2), inset 0 0 50px rgba(0, 100, 180, 0.1);
  border-color: rgba(0, 229, 255, 0.45);
}

@keyframes panel-glow {
  0%, 100% { box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25), inset 0 1px 0 rgba(255, 255, 255, 0.06), 0 0 20px rgba(0, 229, 255, 0.05); }
  50% { box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25), inset 0 1px 0 rgba(255, 255, 255, 0.08), 0 0 28px rgba(0, 229, 255, 0.12); }
}
.panel-flex {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.panel-flex-lg {
  flex: 1.2;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.panel-corner {
  position: absolute;
  width: 12px;
  height: 12px;
  border-color: #00e5ff;
  border-style: solid;
}
.panel-corner.tl { top: -1px; left: -1px; border-width: 2px 0 0 2px; }
.panel-corner.tr { top: -1px; right: -1px; border-width: 2px 2px 0 0; }
.panel-corner.bl { bottom: -1px; left: -1px; border-width: 0 0 2px 2px; }
.panel-corner.br { bottom: -1px; right: -1px; border-width: 0 2px 2px 0; }

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: rgba(0, 229, 255, 0.95);
  letter-spacing: 1px;
}
.title-bar {
  width: 3px;
  height: 14px;
  background: linear-gradient(180deg, #00e5ff, #1890ff);
  box-shadow: 0 0 8px #00e5ff;
}
.panel-tag {
  margin-left: auto;
  padding: 1px 8px;
  font-size: 10px;
  border: 1px solid rgba(0, 229, 255, 0.5);
  color: #00e5ff;
  border-radius: 2px;
}

.gauge-row {
  display: flex;
  gap: 4px;
  height: 140px;
}
.gauge-chart {
  flex: 1;
  height: 100%;
  width: 100%;
  min-width: 0;
}
.chart-area {
  flex: 1;
  min-height: 120px;
  width: 100%;
  min-width: 0;
}

/* 中间区域 */
.col-center {
  gap: 10px;
}
.center-tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}
.tab-btn {
  padding: 4px 16px;
  font-size: 12px;
  color: rgba(0, 229, 255, 0.65);
  background: rgba(0, 40, 80, 0.28);
  border: 1px solid rgba(0, 229, 255, 0.2);
  cursor: pointer;
  transition: all 0.25s;
  backdrop-filter: blur(8px);
}
.tab-btn:hover,
.tab-btn.active {
  color: #fff;
  background: rgba(0, 229, 255, 0.12);
  border-color: rgba(0, 229, 255, 0.55);
  box-shadow: 0 0 16px rgba(0, 229, 255, 0.25);
}

.center-stage {
  position: relative;
  flex: 1;
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 8px;
  background: radial-gradient(ellipse at center, rgba(0, 80, 140, 0.15) 0%, transparent 70%);
  border: 1px solid rgba(0, 229, 255, 0.12);
  backdrop-filter: blur(4px);
}
.stage-grid {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) perspective(400px) rotateX(72deg);
  width: 120%;
  height: 55%;
  background:
    linear-gradient(rgba(0, 229, 255, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 229, 255, 0.15) 1px, transparent 1px);
  background-size: 30px 30px;
  mask-image: linear-gradient(to top, black 30%, transparent 100%);
}
.stage-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(0, 229, 255, 0.2);
  animation: ring-pulse 4s ease-in-out infinite;
}
.ring-1 {
  width: 200px;
  height: 200px;
  animation-delay: 0s;
}
.ring-2 {
  width: 280px;
  height: 280px;
  animation-delay: 1s;
  opacity: 0.5;
}
.ring-3 {
  width: 340px;
  height: 340px;
  animation-delay: 2s;
  opacity: 0.25;
  border-style: dashed;
}
.stage-platform {
  position: absolute;
  bottom: 16%;
  left: 50%;
  width: 220px;
  height: 220px;
  margin-left: -110px;
  border-radius: 50%;
  transform: perspective(500px) rotateX(72deg);
  background: radial-gradient(ellipse at center, rgba(0, 229, 255, 0.35) 0%, rgba(0, 80, 160, 0.15) 45%, transparent 70%);
  border: 1px solid rgba(0, 229, 255, 0.35);
  box-shadow: 0 0 50px rgba(0, 229, 255, 0.35), inset 0 0 30px rgba(0, 229, 255, 0.15);
  animation: platform-pulse 4s ease-in-out infinite;
  z-index: 0;
}

@keyframes platform-pulse {
  0%, 100% { opacity: 0.75; transform: perspective(500px) rotateX(72deg) scale(0.96); }
  50% { opacity: 1; transform: perspective(500px) rotateX(72deg) scale(1); }
}

.hub-orb {
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, rgba(0, 229, 255, 0.9), rgba(0, 80, 160, 0.2) 60%, transparent 70%);
  box-shadow: 0 0 40px rgba(0, 229, 255, 0.6), 0 0 80px rgba(0, 150, 255, 0.3);
  animation: orb-pulse 3s ease-in-out infinite;
  z-index: 1;
}
@keyframes orb-pulse {
  0%, 100% { transform: scale(1); opacity: 0.85; }
  50% { transform: scale(1.08); opacity: 1; }
}
@keyframes ring-pulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.05); opacity: 0.6; }
}

.hub-cube {
  position: relative;
  width: 72px;
  height: 72px;
  transform-style: preserve-3d;
  animation: cube-spin 12s linear infinite;
  z-index: 2;
}
@keyframes cube-spin {
  from { transform: rotateX(-18deg) rotateY(0deg); }
  to { transform: rotateX(-18deg) rotateY(360deg); }
}
.cube-face {
  position: absolute;
  width: 72px;
  height: 72px;
  border: 1px solid rgba(0, 229, 255, 0.7);
  background: rgba(0, 60, 120, 0.4);
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.3), inset 0 0 20px rgba(0, 229, 255, 0.1);
}
.cube-face.front  { transform: translateZ(36px); }
.cube-face.back   { transform: rotateY(180deg) translateZ(36px); }
.cube-face.left   { transform: rotateY(-90deg) translateZ(36px); }
.cube-face.right  { transform: rotateY(90deg) translateZ(36px); }
.cube-face.top    { transform: rotateX(90deg) translateZ(36px); }
.cube-face.bottom { transform: rotateX(-90deg) translateZ(36px); }

.flow-lines {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.flow-line {
  position: absolute;
  right: 10%;
  top: calc(15% + var(--i) * 12%);
  width: 120px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00e5ff, transparent);
  animation: flow-move 2.5s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.3s);
  opacity: 0.6;
}
@keyframes flow-move {
  0% { transform: translateX(80px); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateX(-60px); opacity: 0; }
}

.center-kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  flex-shrink: 0;
}
.kpi-item {
  text-align: center;
  padding: 10px 6px;
  background: linear-gradient(180deg, rgba(0, 80, 120, 0.32), rgba(0, 40, 70, 0.22));
  border: 1px solid rgba(0, 229, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  transform: perspective(400px) rotateX(2deg);
  transition: transform 0.3s, box-shadow 0.3s, background 0.3s;
}
.kpi-item:hover {
  transform: perspective(400px) rotateX(0deg) translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 229, 255, 0.2);
}
.kpi-value {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 0 12px rgba(0, 229, 255, 0.5);
}
.kpi-label {
  margin-top: 4px;
  font-size: 11px;
  color: rgba(0, 229, 255, 0.65);
}

.kpi-sub {
  margin-top: 4px;
  font-size: 10px;
  color: rgba(255, 197, 61, 0.85);
  letter-spacing: 0.5px;
}

.panel-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.panel-title-row .title-text {
  flex: 1;
  min-width: 0;
}

.order-live-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #ff6b6b;
  background: rgba(255, 77, 79, 0.12);
  border: 1px solid rgba(255, 77, 79, 0.35);
  border-radius: 999px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ff4d4f;
  box-shadow: 0 0 8px #ff4d4f;
  animation: live-blink 1.2s ease-in-out infinite;
}

@keyframes live-blink {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.35; transform: scale(0.75); }
}

.panel-orders {
  flex-shrink: 0;
  height: 196px;
}

.order-scroll {
  --order-cols: 28px minmax(0, 1.35fr) minmax(0, 0.8fr) minmax(0, 1.05fr) minmax(0, 0.88fr) minmax(0, 0.82fr);
  height: 152px;
}

.order-scroll-frame {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 6px;
  border: 1px solid rgba(0, 229, 255, 0.18);
  background: linear-gradient(180deg, rgba(0, 30, 60, 0.55), rgba(0, 12, 28, 0.35));
  box-shadow: inset 0 0 24px rgba(0, 229, 255, 0.06);
  overflow: hidden;
  position: relative;
}

.order-scroll-frame::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(0, 229, 255, 0.04) 50%,
    transparent
  );
  animation: order-shine 6s linear infinite;
}

@keyframes order-shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.order-scroll-header,
.order-scroll-item {
  display: grid;
  grid-template-columns: var(--order-cols);
  gap: 6px 10px;
  align-items: center;
  font-size: 11px;
  line-height: 1.35;
}

.order-scroll-header .col-amount,
.order-scroll-item .col-amount {
  text-align: right;
  justify-self: stretch;
  padding-right: 2px;
}

.order-scroll-header .col-status,
.order-scroll-item .col-status {
  display: flex;
  justify-content: center;
  justify-self: center;
}

.order-scroll-header {
  flex-shrink: 0;
  height: 30px;
  padding: 0 10px;
  color: rgba(0, 229, 255, 0.7);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  background: linear-gradient(180deg, rgba(0, 229, 255, 0.12), rgba(0, 229, 255, 0.03));
  border-bottom: 1px solid rgba(0, 229, 255, 0.22);
  box-shadow: 0 1px 0 rgba(0, 229, 255, 0.08);
}

.order-scroll-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  mask-image: linear-gradient(180deg, transparent 0%, #000 8%, #000 92%, transparent 100%);
  -webkit-mask-image: linear-gradient(180deg, transparent 0%, #000 8%, #000 92%, transparent 100%);
}

.order-scroll-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 100%;
  font-size: 12px;
  color: rgba(0, 229, 255, 0.45);
}

.order-scroll-empty .empty-icon {
  font-size: 22px;
  color: rgba(0, 229, 255, 0.25);
  animation: empty-pulse 2s ease-in-out infinite;
}

@keyframes empty-pulse {
  0%, 100% { opacity: 0.35; }
  50% { opacity: 0.9; }
}

.order-scroll-track {
  will-change: transform;
  animation: scroll-up var(--scroll-duration, 28s) linear infinite;
}

.order-scroll-track:hover {
  animation-play-state: paused;
}

@keyframes scroll-up {
  0% { transform: translate3d(0, 0, 0); }
  100% { transform: translate3d(0, -50%, 0); }
}

.order-scroll-item {
  position: relative;
  min-height: 36px;
  margin: 0 4px 4px;
  padding: 6px 8px 6px 10px !important;
  border-radius: 4px;
  border: 1px solid rgba(0, 229, 255, 0.08);
  background: rgba(0, 25, 50, 0.35);
  transition: background 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.order-scroll-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 20%;
  bottom: 20%;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--row-accent, rgba(0, 229, 255, 0.4));
  box-shadow: 0 0 10px var(--row-accent, rgba(0, 229, 255, 0.3));
}

.order-scroll-item:nth-child(even) {
  background: rgba(0, 40, 75, 0.45);
}

.order-scroll-track:hover .order-scroll-item:hover {
  background: rgba(0, 229, 255, 0.12);
  border-color: rgba(0, 229, 255, 0.28);
  box-shadow: 0 0 16px rgba(0, 229, 255, 0.12);
}

.order-scroll-item .col-idx {
  font-size: 10px;
  font-weight: 700;
  color: rgba(0, 229, 255, 0.45);
  font-variant-numeric: tabular-nums;
  text-align: center;
}

.order-scroll-item .col-no {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.order-no-text {
  color: #7ee8ff;
  font-family: ui-monospace, 'Cascadia Code', Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.3px;
}

.order-time {
  font-size: 9px;
  color: rgba(0, 229, 255, 0.45);
  font-variant-numeric: tabular-nums;
}

.order-scroll-item .col-customer {
  color: rgba(230, 247, 255, 0.9);
}

.order-scroll-item .col-product {
  color: rgba(255, 255, 255, 0.75);
}

.order-scroll-item .col-amount {
  color: #ffe58f;
  font-weight: 700;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 0 12px rgba(255, 197, 61, 0.35);
}

.order-scroll-item .col-amount em {
  font-style: normal;
  font-size: 10px;
  margin-right: 1px;
  color: rgba(255, 197, 61, 0.75);
}

.order-scroll-header .col-amount {
  color: rgba(0, 229, 255, 0.55);
}

.order-scroll .cell-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  min-width: 56px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.4px;
  white-space: nowrap;
  border: 1px solid transparent;
  backdrop-filter: blur(4px);
  box-shadow: 0 0 12px transparent;
}

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  flex-shrink: 0;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
  animation: status-pulse 2.4s ease-in-out infinite;
}

@keyframes status-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.55; transform: scale(0.85); }
}

.order-scroll-item.status-pending-pay { --row-accent: #ff9c6e; }
.order-scroll-item.status-pending-audit { --row-accent: #b37feb; }
.order-scroll-item.status-pending-ship { --row-accent: #ffc53d; }
.order-scroll-item.status-shipped { --row-accent: #40a9ff; }
.order-scroll-item.status-completed { --row-accent: #73d13d; }
.order-scroll-item.status-cancelled { --row-accent: #8c8c8c; }
.order-scroll-item.status-unknown { --row-accent: rgba(0, 229, 255, 0.4); }

.status-pending-pay {
  color: #ffb896;
  background: linear-gradient(135deg, rgba(255, 120, 80, 0.22), rgba(255, 80, 60, 0.08));
  border-color: rgba(255, 156, 110, 0.45);
  box-shadow: 0 0 14px rgba(255, 120, 80, 0.15);
}

.status-pending-audit {
  color: #d3adf7;
  background: linear-gradient(135deg, rgba(146, 84, 222, 0.22), rgba(114, 46, 209, 0.08));
  border-color: rgba(179, 127, 235, 0.45);
  box-shadow: 0 0 14px rgba(146, 84, 222, 0.12);
}

.status-pending-ship {
  color: #ffe58f;
  background: linear-gradient(135deg, rgba(255, 197, 61, 0.24), rgba(250, 173, 20, 0.08));
  border-color: rgba(255, 197, 61, 0.5);
  box-shadow: 0 0 14px rgba(255, 197, 61, 0.14);
}

.status-shipped {
  color: #91d5ff;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.24), rgba(64, 169, 255, 0.08));
  border-color: rgba(105, 192, 255, 0.5);
  box-shadow: 0 0 14px rgba(64, 169, 255, 0.14);
}

.status-completed {
  color: #b7eb8f;
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.24), rgba(115, 209, 61, 0.08));
  border-color: rgba(115, 209, 61, 0.5);
  box-shadow: 0 0 14px rgba(115, 209, 61, 0.14);
}

.status-completed .status-dot {
  animation: none;
  opacity: 1;
}

.status-cancelled {
  color: #bfbfbf;
  background: linear-gradient(135deg, rgba(140, 140, 140, 0.2), rgba(89, 89, 89, 0.1));
  border-color: rgba(140, 140, 140, 0.4);
}

.status-cancelled .status-dot {
  animation: none;
  box-shadow: none;
  opacity: 0.6;
}

.status-unknown {
  color: rgba(0, 229, 255, 0.75);
  background: rgba(0, 229, 255, 0.08);
  border-color: rgba(0, 229, 255, 0.25);
}

@media (max-width: 1500px) {
  .screen-body {
    grid-template-columns: minmax(220px, 25%) minmax(0, 1fr) minmax(220px, 25%);
    padding: 8px 10px 12px;
    gap: 8px;
  }

  .screen-title {
    font-size: 18px;
    letter-spacing: 3px;
  }

  .header-deco-left { left: 40px; width: 80px; }
  .header-deco-right { right: 40px; width: 80px; }
}

@media (max-width: 1280px) {
  .screen-body {
    grid-template-columns: minmax(200px, 28%) minmax(0, 1fr) minmax(200px, 28%);
  }

  .gauge-row {
    height: 120px;
  }

  .center-kpis {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1200px) {
  .data-screen {
    height: auto;
    min-height: calc(100vh - 60px);
    overflow-y: auto;
  }

  .screen-body {
    grid-template-columns: 1fr;
    height: auto;
    overflow: visible;
    padding: 8px 12px 16px;
  }

  .col-left,
  .col-right,
  .col-center {
    overflow: visible;
  }

  .col-center .center-stage {
    min-height: 160px;
  }

  .center-kpis {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
