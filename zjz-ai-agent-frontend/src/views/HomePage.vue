<template>
  <div class="home-container">
    <h1 class="home-title">ZJZ AI Agent</h1>
    <div class="app-cards">
      <div class="app-card" @click="goToPlanApp">
        <h2>途趣AI行程规划大师</h2>
        <p>智能行程规划，为您定制完美旅行方案</p>
        <button>进入应用</button>
      </div>
      <div class="app-card" @click="goToZjzManus">
        <h2>AI超级智能体</h2>
        <p>多步骤智能处理，解决复杂问题</p>
        <button>进入应用</button>
      </div>
    </div>
    <div class="footer">
      <p>© 2026 ZJZ AI Agent. All rights reserved. | 站长：郑少 | 邮箱：<a href="mailto:2302181496@qq.com">2302181496@qq.com</a></p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToPlanApp = () => {
  router.push('/plan-app')
}

const goToZjzManus = () => {
  router.push('/zjz-manus')
}
</script>