import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 扩展路由元信息类型
declare module 'vue-router' {
    interface RouteMeta {
        roles?: string[]
    }
}

// 普通用户页面
import CustomerHome from './views/customer/CustomerHome.vue'
import Login from './views/auth/Login.vue'
import Register from './views/auth/Register.vue'
import Cart from './views/customer/Cart.vue'
import MyOrders from './views/customer/MyOrders.vue'
import Profile from './views/customer/Profile.vue'

// 商家页面
import MerchantHome from './views/merchant/MerchantHome.vue'
import MerchantProducts from './views/merchant/MerchantProducts.vue'
import MerchantStock from './views/merchant/MerchantStock.vue'
import MerchantStatistics from './views/merchant/MerchantStatistics.vue'
import MerchantOrders from './views/merchant/MerchantOrders.vue'

// 管理员页面
import AdminHome from './views/admin/AdminHome.vue'
import AdminUsers from './views/admin/AdminUsers.vue'

const routes: RouteRecordRaw[] = [
    // 公共页面（无需登录）
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },

    // 顾客页面
    { path: '/', name: 'CustomerHome', component: CustomerHome, meta: { roles: ['customer', 'merchant', 'admin'] } },
    { path: '/cart', name: 'Cart', component: Cart, meta: { roles: ['customer', 'merchant', 'admin'] } },
    { path: '/orders', name: 'MyOrders', component: MyOrders, meta: { roles: ['customer'] } },
    { path: '/profile', name: 'Profile', component: Profile, meta: { roles: ['customer', 'merchant', 'admin'] } },
    { path: '/product/:id', name: 'ProductDetail', component: () => import('./views/customer/ProductDetail.vue'), meta: { roles: ['customer', 'merchant', 'admin'] } },

    // 商家页面
    { path: '/merchant', name: 'MerchantHome', component: MerchantHome, meta: { roles: ['merchant'] } },
    { path: '/merchant/products', name: 'MerchantProducts', component: MerchantProducts, meta: { roles: ['merchant'] } },
    { path: '/merchant/stock', name: 'MerchantStock', component: MerchantStock, meta: { roles: ['merchant'] } },
    { path: '/merchant/statistics', name: 'MerchantStatistics', component: MerchantStatistics, meta: { roles: ['merchant'] } },
    { path: '/merchant/orders', name: 'MerchantOrders', component: MerchantOrders, meta: { roles: ['merchant'] } },

    // 管理员页面
    { path: '/admin', name: 'AdminHome', component: AdminHome, meta: { roles: ['admin'] } },
    { path: '/admin/users', name: 'AdminUsers', component: AdminUsers, meta: { roles: ['admin'] } },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局路由守卫
router.beforeEach((to, from) => {
    const userStr = localStorage.getItem('user')
    const publicPages = ['/login', '/register']
    const isPublicPage = publicPages.includes(to.path)

    if (!userStr && !isPublicPage) {
        return '/login'
    }

    if (userStr && to.meta.roles) {
        const user = JSON.parse(userStr)
        if (!to.meta.roles.includes(user.role)) {
            if (user.role === 'merchant') return '/merchant'
            if (user.role === 'admin') return '/admin'
            return '/'
        }
    }

    return true
})

export default router