import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { registerGlobalComponents } from './components'
// 自托管拉丁展示字体(品牌字标/数字),中文字形回落到系统字体栈
import '@fontsource-variable/outfit'
import '@/styles/global.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
registerGlobalComponents(app)

app.mount('#app')
