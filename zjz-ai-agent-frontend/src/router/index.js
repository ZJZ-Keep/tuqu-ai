import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../views/HomePage.vue'
import PlanAppChat from '../views/PlanAppChat.vue'
import ZjzManusChat from '../views/ZjzManusChat.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomePage
  },
  {
    path: '/plan-app',
    name: 'planApp',
    component: PlanAppChat
  },
  {
    path: '/zjz-manus',
    name: 'zjzManus',
    component: ZjzManusChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router