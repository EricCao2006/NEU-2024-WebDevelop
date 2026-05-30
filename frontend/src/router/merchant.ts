import MerchantHome from '../views/merchant/MerchantHome.vue'
import MerchantProducts from '../views/merchant/MerchantProducts.vue'
import MerchantStock from '../views/merchant/MerchantStock.vue'
import MerchantOrders from '../views/merchant/MerchantOrders.vue'
import MerchantStatistics from '../views/merchant/MerchantStatistics.vue'

export default [
    {
        path: '/merchant',
        name: 'MerchantHome',
        component: MerchantHome,
        meta: { roles: ['merchant'] }
    },
    {
        path: '/merchant/products',
        name: 'MerchantProducts',
        component: MerchantProducts,
        meta: { roles: ['merchant'] }
    },
    {
        path: '/merchant/stock',
        name: 'MerchantStock',
        component: MerchantStock,
        meta: { roles: ['merchant'] }
    },
    {
        path: '/merchant/orders',
        name: 'MerchantOrders',
        component: MerchantOrders,
        meta: { roles: ['merchant'] }
    },
    {
        path: '/merchant/statistics',
        name: 'MerchantStatistics',
        component: MerchantStatistics,
        meta: { roles: ['merchant'] }
    }
]