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
        <div class="message-content" v-html="formatContent(msg.content)"></div>
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

// 格式化消息内容
const formatContent = (content) => {
  // 检查是否是 JSON 格式
  if (content.startsWith('{') || content.startsWith('[')) {
    try {
      const parsed = JSON.parse(content)
      // 格式化 JSON
      return `<pre>${JSON.stringify(parsed, null, 2)}</pre>`
    } catch (e) {
      // 不是有效的 JSON，直接返回
      return content
    }
  }
  // 处理普通文本，添加换行和空格
  return content.replace(/\n/g, '<br>')
}

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
    // 处理 SSE 格式，移除 data: 前缀
    const content = data.replace(/^data: /, '').trim()
    if (content === '[DONE]') {
      eventSource.close()
      isLoading.value = false
    } else {
      // 处理可能的编码问题
      // 直接使用原始数据，现代浏览器会自动处理编码
      messages.value[aiMessageIndex].content += content
    }
  }
  
  eventSource.onopen = () => {
    console.log('SSE 连接已建立')
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    // 检查连接状态，如果是正常关闭则不显示错误
    // readyState === 2 表示连接已关闭
    if (eventSource.readyState === 2) {
      console.log('SSE 连接正常关闭')
      isLoading.value = false
    } else {
      // 检查是否已经收到了有效的数据
      const hasReceivedData = messages.value[aiMessageIndex].content !== ''
      
      // 无论如何都关闭连接
      try {
        eventSource.close()
      } catch (e) {
        console.log('EventSource 已经关闭')
      }
      
      // 只有在未收到任何数据的情况下才显示错误消息
      if (!hasReceivedData) {
        messages.value[aiMessageIndex].content += '连接出错，请重试'
      }
      
      // 重置加载状态
      isLoading.value = false
    }
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