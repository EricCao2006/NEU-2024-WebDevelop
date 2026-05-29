export default [
    {
        path: '/',
        name: 'ProductList',
        component: () => import('../views/customer/ProductList.vue')
    },
    {
        path: '/cart',
        name: 'Cart',
        component: () => import('../views/customer/Cart.vue')
    },
    {
        path: '/orders',
        name: 'MyOrders',
        component: () => import('../views/customer/MyOrders.vue')
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/customer/Profile.vue')
    }
]