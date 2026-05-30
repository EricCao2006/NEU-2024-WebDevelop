import AdminHome from '../views/admin/AdminHome.vue'

export default [
    {
        path: '/admin',
        name: 'AdminHome',
        component: AdminHome,
        meta: { roles: ['admin'] }
    },
    {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue'),
        meta: { roles: ['admin'] }
    },
    {
        path: '/admin/orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/AdminOrders.vue'),
        meta: { roles: ['admin'] }
    }
]