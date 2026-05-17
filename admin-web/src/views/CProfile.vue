<template>
  <div class="profile">
    <h2>个人中心</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="info">
        <el-form :model="userInfo" label-width="100px">
          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="userInfo.nickname" />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="userInfo.realName" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="userInfo.email" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="passwordForm" label-width="100px">
          <el-form-item label="原密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePwd">确认修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="企业资质" name="qualification">
        <el-button type="primary" @click="$router.push('/c/qualification')">管理企业资质</el-button>
      </el-tab-pane>
      <el-tab-pane label="收货地址" name="address">
        <el-button type="primary" @click="$router.push(cPath('addresses'))">管理收货地址</el-button>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserProfile, changePassword } from '@/api/consumer/user'
import { cPath } from '@/utils/consumer-path'

const activeTab = ref('info')
const userInfo = ref({})
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(async () => {
  try {
    userInfo.value = await getUserInfo()
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
})

const saveProfile = async () => {
  try {
    await updateUserProfile(userInfo.value)
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('保存失败', e)
  }
}

const changePwd = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    console.error('修改密码失败', e)
  }
}
</script>

<style scoped>
.profile { max-width: 800px; margin: 0 auto; padding: 20px; }
h2 { margin-bottom: 20px; }
</style>
