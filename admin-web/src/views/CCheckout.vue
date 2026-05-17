<template>
  <div class="checkout page-wrap">
    <div class="container">
      <h2>确认订单</h2>

      <el-card class="section">
        <template #header>
          <span>收货地址</span>
          <el-button text type="primary" @click="showAddressDialog = true">选择地址</el-button>
        </template>
        <div v-if="selectedAddress" class="address-info">
          <p><strong>{{ selectedAddress.receiverName }}</strong> {{ selectedAddress.receiverPhone }}</p>
          <p>{{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}</p>
        </div>
        <div v-else class="no-address">
          <el-button @click="showAddressDialog = true">选择收货地址</el-button>
        </div>
      </el-card>

      <el-card class="section">
        <template #header>
          <span>{{ isPackage ? '套餐商品' : '商品信息' }}</span>
          <el-tag v-if="isPackage" type="warning" size="small" style="margin-left:8px">套餐价</el-tag>
        </template>
        <el-table :data="orderItems" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="specification" label="规格" width="120" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">¥{{ row.price ?? row.wholesalePrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ ((row.price ?? row.wholesalePrice ?? 0) * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="isPrescription" class="section">
        <template #header><span>处方信息</span></template>
        <div v-if="selectedPrescription">
          <p>处方编号：{{ selectedPrescription.prescriptionNo }}</p>
          <p>审核状态：
            <el-tag :type="rxStatusTag(selectedPrescription.auditStatus)">
              {{ rxStatusLabel(selectedPrescription.auditStatus) }}
            </el-tag>
          </p>
        </div>
        <el-button v-else type="primary" @click="showPrescriptionDialog = true">选择已审核处方</el-button>
      </el-card>

      <el-card v-if="!isPackage && !isPrescription" class="section">
        <template #header><span>优惠券</span></template>
        <el-select v-model="selectedCouponId" placeholder="不使用优惠券" clearable style="width: 100%">
          <el-option label="不使用优惠券" :value="null" />
          <el-option
            v-for="c in usableCoupons"
            :key="c.id"
            :label="`${c.name}（满${c.minOrderAmount || 0}减${c.discountValue}）`"
            :value="c.id"
          />
        </el-select>
      </el-card>

      <el-card class="section">
        <template #header><span>订单备注</span></template>
        <el-input v-model="remark" type="textarea" :rows="3" placeholder="如有特殊要求请在此备注" />
      </el-card>

      <div class="order-summary">
        <div class="summary-row total">
          <span>应付金额：</span>
          <span class="final-amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <div class="action-bar">
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">提交订单</el-button>
      </div>

      <el-dialog v-model="showAddressDialog" title="选择收货地址" width="520px">
        <el-radio-group v-model="selectedAddressId" class="address-list">
          <div v-for="addr in addressList" :key="addr.id" class="address-item">
            <el-radio :value="addr.id">
              <p><strong>{{ addr.receiverName }}</strong> {{ addr.receiverPhone }}</p>
              <p>{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</p>
            </el-radio>
          </div>
        </el-radio-group>
        <template #footer>
          <el-button @click="showAddressDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAddress">确定</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="showPrescriptionDialog" title="选择处方" width="640px">
        <el-table :data="prescriptionList" highlight-current-row @row-click="selectPrescription">
          <el-table-column prop="prescriptionNo" label="处方编号" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="rxStatusTag(row.auditStatus)">{{ rxStatusLabel(row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/consumer/product'
import { getCartList, addToCart } from '@/api/consumer/cart'
import { getPackageDetail } from '@/api/consumer/home'
import { getAddressList, getDefaultAddress } from '@/api/consumer/address'
import { getPrescriptionList } from '@/api/consumer/prescription'
import { createOtcOrder, createPrescriptionOrder, createPackageOrder } from '@/api/consumer/order'
import { getMyCoupons } from '@/api/consumer/coupon'
import { rxStatusLabel, rxStatusTag } from '@/utils/consumer'
import { cPath } from '@/utils/consumer-path'

const route = useRoute()
const router = useRouter()

const orderItems = ref([])
const cartItemIds = ref([])
const selectedAddress = ref(null)
const selectedAddressId = ref(null)
const addressList = ref([])
const showAddressDialog = ref(false)
const isPrescription = ref(false)
const isPackage = ref(false)
const packageId = ref(null)
const packagePayAmount = ref(null)
const selectedPrescription = ref(null)
const showPrescriptionDialog = ref(false)
const prescriptionList = ref([])
const remark = ref('')
const submitting = ref(false)
const usableCoupons = ref([])
const selectedCouponId = ref(null)

const totalAmount = computed(() => {
  if (isPackage.value && packagePayAmount.value != null) {
    return packagePayAmount.value
  }
  return orderItems.value.reduce(
    (sum, item) => sum + (item.price ?? item.wholesalePrice ?? 0) * (item.quantity || 1),
    0
  )
})

async function loadData() {
  const itemsQuery = route.query.items
  const productId = route.query.productId
  const pkgId = route.query.packageId

  if (route.query.type === 'package' && pkgId) {
    isPackage.value = true
    packageId.value = Number(pkgId)
    const pkg = await getPackageDetail(packageId.value)
    orderItems.value = (pkg?.items || []).map((it) => ({
      productId: it.productId,
      productName: it.productName,
      specification: it.specification,
      quantity: it.quantity || 1,
      price: null,
      wholesalePrice: null
    }))
    packagePayAmount.value = pkg?.packagePrice != null ? Number(pkg.packagePrice) : null
    cartItemIds.value = []
    isPrescription.value = false
  } else if (itemsQuery) {
    const ids = String(itemsQuery).split(',').map(Number).filter(Boolean)
    const list = await getCartList()
    const selected = (list || []).filter(i => ids.includes(i.id))
    orderItems.value = selected
    cartItemIds.value = selected.map(i => i.id)
    isPrescription.value = false
  } else if (productId) {
    const product = await getProductDetail(productId)
    isPrescription.value =
      product.prescriptionType === 'PRESCRIPTION' || route.query.type === 'prescription'
    if (isPrescription.value) {
      orderItems.value = [
        {
          ...product,
          productName: product.productName,
          quantity: parseInt(route.query.quantity || '1', 10)
        }
      ]
    } else {
      const qty = parseInt(route.query.quantity || '1', 10)
      const sidRaw = route.query.seckillId
      const sid = sidRaw != null && sidRaw !== '' ? Number(sidRaw) : null
      const seckillId = Number.isFinite(sid) && sid > 0 ? sid : null
      await addToCart(productId, qty, seckillId)
      const list = await getCartList()
      const pid = Number(productId)
      const matched =
        list?.find(
          (i) =>
            Number(i.productId) === pid &&
            Number(i.seckillId || 0) === (seckillId || 0)
        ) || list?.[0]
      orderItems.value = matched ? [matched] : []
      cartItemIds.value = matched ? [matched.id] : []
    }
  } else {
    const list = await getCartList()
    orderItems.value = list || []
    cartItemIds.value = (list || []).map(i => i.id)
  }

  addressList.value = await getAddressList()
  let defaultAddr = null
  try {
    defaultAddr = await getDefaultAddress()
  } catch {
    defaultAddr = null
  }
  if (!defaultAddr) {
    defaultAddr = addressList.value.find((a) => a.isDefault === 1) || addressList.value[0]
  }
  if (defaultAddr) {
    selectedAddress.value = defaultAddr
    selectedAddressId.value = defaultAddr.id
  }

  if (isPrescription.value) {
    const rxPage = await getPrescriptionList({ page: 1, pageSize: 50 })
    prescriptionList.value = (rxPage?.list ?? []).filter(p => p.auditStatus === 1)
  }

  if (!isPackage.value && !isPrescription.value) {
    try {
      usableCoupons.value = await getMyCoupons({ orderAmount: totalAmount.value })
    } catch {
      usableCoupons.value = []
    }
  }
}

function confirmAddress() {
  const addr = addressList.value.find(a => a.id === selectedAddressId.value)
  if (addr) selectedAddress.value = addr
  showAddressDialog.value = false
}

function selectPrescription(row) {
  if (row.auditStatus !== 1) {
    ElMessage.warning('请选择审核通过的处方')
    return
  }
  selectedPrescription.value = row
  showPrescriptionDialog.value = false
}

async function submitOrder() {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (isPrescription.value && !selectedPrescription.value) {
    ElMessage.warning('请选择处方')
    return
  }

  submitting.value = true
  try {
    let order
    if (isPackage.value) {
      if (!packageId.value) {
        ElMessage.warning('套餐信息无效')
        return
      }
      order = await createPackageOrder({
        packageId: packageId.value,
        addressId: selectedAddress.value.id,
        remark: remark.value
      })
    } else if (isPrescription.value) {
      order = await createPrescriptionOrder({
        prescriptionId: selectedPrescription.value.id,
        addressId: selectedAddress.value.id,
        items: orderItems.value.map(item => ({
          productId: item.productId || item.id,
          quantity: item.quantity
        })),
        remark: remark.value
      })
    } else {
      if (!cartItemIds.value.length) {
        ElMessage.warning('请选择商品')
        return
      }
      order = await createOtcOrder({
        cartItemIds: cartItemIds.value,
        addressId: selectedAddress.value.id,
        remark: remark.value,
        userCouponId: selectedCouponId.value || undefined
      })
    }
    ElMessage.success('订单提交成功')
    router.push(cPath(`pay/${order.id}`))
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-wrap { padding: 20px 0; }
.container { max-width: 1000px; margin: 0 auto; padding: 0 20px; }
.section { margin-bottom: 16px; }
.address-info p { margin: 4px 0; }
.no-address { text-align: center; padding: 16px; }
.order-summary { background: #f5f7fa; padding: 16px; border-radius: 8px; margin: 16px 0; }
.summary-row.total { display: flex; justify-content: space-between; font-size: 16px; }
.final-amount { color: #e4393c; font-size: 22px; font-weight: bold; }
.action-bar { display: flex; justify-content: flex-end; gap: 12px; }
.address-item { padding: 8px 0; border-bottom: 1px solid #eee; }
.address-item p { margin: 2px 0; font-size: 13px; }
</style>
