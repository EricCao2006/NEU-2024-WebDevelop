export default [
    {
        path: '/admin',
        name: 'AdminProducts',
        component: () => import('../views/admin/AdminProducts.vue')
    },
    {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('../views/admin/Statistics.vue')
    }
]