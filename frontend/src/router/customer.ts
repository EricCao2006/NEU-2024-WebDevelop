import CustomerHome from '../views/customer/CustomerHome.vue'
import Cart from '../views/customer/Cart.vue'
import MyOrders from '../views/customer/MyOrders.vue'
import Profile from '../views/customer/Profile.vue'
import ProductDetail from '../views/customer/ProductDetail.vue'

export default [
    {
        path: '/',
        name: 'CustomerHome',
        component: CustomerHome,
        meta: { roles: ['customer', 'merchant', 'admin'] }
    },
    {
        path: '/cart',
        name: 'Cart',
        component: Cart,
        meta: { roles: ['customer', 'merchant', 'admin'] }
    },
    {
        path: '/orders',
        name: 'MyOrders',
        component: MyOrders,
        meta: { roles: ['customer'] }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: Profile,
        meta: { roles: ['customer', 'merchant', 'admin'] }
    },
    {
        path: '/product/:id',
        name: 'ProductDetail',
        component: ProductDetail,
        meta: { roles: ['customer', 'merchant', 'admin'] }
    }
]