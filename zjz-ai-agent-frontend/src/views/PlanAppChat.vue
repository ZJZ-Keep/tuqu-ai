<template>
  <div class="chat-container">
    <div class="chat-header">
      途趣AI行程规划大师
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
const chatId = ref('')
let eventSource = null

// 生成聊天室ID
const generateChatId = () => {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage = inputMessage.value.trim()
  messages.value.push({ isUser: true, content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  
  // 清空之前的AI消息，准备接收新的流式响应
  const aiMessageIndex = messages.value.length
  messages.value.push({ isUser: false, content: '' })
  
  // 调用SSE接口
  const url = `http://localhost:8081/api/ai/plan_app/chat/Sse?message=${encodeURIComponent(userMessage)}&chatId=${chatId.value}`
  
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
      // 打字机效果，在同一个气泡中拼接消息
      messages.value[aiMessageIndex].content += data
    }
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    eventSource.close()
    isLoading.value = false
    messages.value[aiMessageIndex].content += '\n\n连接出错，请重试'
  }
}

onMounted(() => {
  chatId.value = generateChatId()
  // 欢迎消息
  messages.value.push({ 
    isUser: false, 
    content: '你好！我是途趣AI行程规划大师，有什么可以帮您规划的旅行吗？'
  })
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>