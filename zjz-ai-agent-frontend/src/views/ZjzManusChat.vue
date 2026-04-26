<template>
  <div class="chat-container">
    <div class="chat-header">
      AI超级智能体
    </div>
    <div class="chat-messages">
      <div 
        v-for="(msg, index) in messages" 
        :key="index"
        :class="['message', msg.isUser ? 'user-message' : 'ai-message']"
      >
        <div v-if="!msg.isUser" class="avatar ai-avatar">AI</div>
        <div v-else class="avatar">你</div>
        <div class="message-content">{{ msg.content }}</div>
      </div>
    </div>
    <div class="chat-input">
      <input 
        v-model="inputMessage" 
        type="text" 
        placeholder="请输入您的问题..."
        @keyup.enter="sendMessage"
        :disabled="isLoading"
      />
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>
    <div class="footer">
      <p>© 2026 ZJZ AI Agent. All rights reserved. | 站长：郑少 | 邮箱：<a href="mailto:2302181496@qq.com">2302181496@qq.com</a></p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
let eventSource = null

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage = inputMessage.value.trim()
  messages.value.push({ isUser: true, content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  
  // 调用SSE接口
  const url = `http://localhost:8081/api/ai/zjz_manus/chat/SseEmitter?message=${encodeURIComponent(userMessage)}`
  
  if (eventSource) {
    eventSource.close()
  }
  
  eventSource = new EventSource(url)
  
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      eventSource.close()
      isLoading.value = false
    } else {
      // 步骤式气泡，每个步骤一个气泡，且添加多余的换行
      messages.value.push({ isUser: false, content: data + '\n' })
    }
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    eventSource.close()
    isLoading.value = false
    messages.value.push({ isUser: false, content: '连接出错，请重试\n' })
  }
}

onMounted(() => {
  // 欢迎消息
  messages.value.push({ 
    isUser: false, 
    content: '你好！我是AI超级智能体，有什么问题需要我帮您解决吗？\n'
  })
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>