import { createApp } from 'vue'
import App from './App.vue'
import router from './index.ts'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './theme.css'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')