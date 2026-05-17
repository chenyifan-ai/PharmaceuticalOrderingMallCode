import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import { setAppRouter } from './utils/app-context'
import './styles/admin-theme.css'
import './styles/admin-page.css'
import './styles/consumer.css'

setAppRouter(router)
import { formatDateTime, formatDate, formatDateTimeShort } from './utils/format'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.config.globalProperties.$formatDateTime = formatDateTime
app.config.globalProperties.$formatDate = formatDate
app.config.globalProperties.$formatDateTimeShort = formatDateTimeShort
app.mount('#app')
