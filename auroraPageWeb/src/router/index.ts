import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/home/index.vue')
    },
    {
      path: '/user/:userUid',
      name: 'userHome',
      component: () => import('../views/user-home/index.vue')
    },
    {
      path: '/friendLink/:userUid',
      name: 'friendLink',
      component: () => import('../views/friend-link/index.vue')
    },
    {
      path: '/archive/:userUid',
      name: 'archive',
      component: () => import('../views/archive/index.vue')
    },
    {
      path: '/tag-category/:userUid',
      name: 'tagCategory',
      component: () => import('../views/tagAndCategory/index.vue')
    },
    {
      path: '/about/:userUid',
      name: 'about',
      component: () => import('../views/about/index.vue')
    },
    {
      path: '/shareSpace/:userUid',
      name: '说说1',
      component: () => import('../views/share-space/swiperShareSpace.vue')
    },
    {
      path: '/shareSpace-page/:userUid',
      name: '说说2',
      component: () => import('../views/share-space/commonShareSpace.vue')
    },
    {
      path: '/article/:userUid/:pageUid',
      name: 'article',
      component: () => import('../views/article/index.vue')
    }
  ]
})

export default router