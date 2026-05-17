import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router/consumer'
import { setAppRouter } from './utils/app-context'
import { setConsumerStandalone } from './utils/consumer-path'
import { formatDateTime, formatDate, formatDateTimeShort } from './utils/format'
import './styles/consumer.css'

setConsumerStandalone(true)
setAppRouter(router)

const app = createApp(App)
const pinia = createPinia()

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
