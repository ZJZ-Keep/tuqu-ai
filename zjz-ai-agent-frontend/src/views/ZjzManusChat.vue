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
import { API_BASE_URL } from '../config'

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
let eventSource = null

// 重置加载状态的函数
const resetLoadingState = () => {
  isLoading.value = false
  if (eventSource) {
    try {
      eventSource.close()
    } catch (e) {
      console.log('EventSource 已经关闭')
    }
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage = inputMessage.value.trim()
  messages.value.push({ isUser: true, content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  
  // 调用 SSE 接口
  const url = `${API_BASE_URL}/ai/zjz_manus/chat/SseEmitter?message=${encodeURIComponent(userMessage)}`
  
  if (eventSource) {
    eventSource.close()
  }
  
  eventSource = new EventSource(url)
  
  // 设置超时机制，确保isLoading能够被重置
  const timeoutId = setTimeout(() => {
    if (isLoading.value) {
      console.log('SSE 连接超时，重置加载状态')
      resetLoadingState()
    }
  }, 60000) // 60秒超时
  
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      clearTimeout(timeoutId)
      resetLoadingState()
    } else {
      // 步骤式气泡，每个步骤一个气泡，且添加多余的换行
      messages.value.push({ isUser: false, content: data + '\n' })
      
      // 检查是否包含任务结束的消息
      if (data.includes('任务结束') || data.includes('圆满结束') || 
          data.includes('任务已完成') || data.includes('已成功生成')) {
        // 延迟一点时间关闭连接，确保所有数据都已接收
        setTimeout(() => {
          clearTimeout(timeoutId)
          resetLoadingState()
        }, 500)
      }
    }
  }
  
  eventSource.onopen = () => {
    console.log('SSE 连接已建立')
  }
  
  eventSource.onerror = (error) => {
    clearTimeout(timeoutId)
    console.error('SSE Error:', error)
    resetLoadingState()
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
  resetLoadingState()
})
</script>