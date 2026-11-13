<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="header-text">
          <h1>AI 超级智能体</h1>
          <span class="header-subtitle">全能型AI助手，自主调用多种工具解决复杂任务</span>
        </div>
        <button class="clear-btn" @click="clearMessages" title="清空对话">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
        </button>
      </div>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.isUser ? 'user-message' : 'ai-message', msg.isProgress ? 'progress-message' : '']"
      >
        <!-- AI 头像 -->
        <div v-if="!msg.isUser" class="avatar ai-avatar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2z"/>
            <circle cx="8.5" cy="14" r="1.5" fill="currentColor"/>
            <circle cx="15.5" cy="14" r="1.5" fill="currentColor"/>
          </svg>
        </div>
        <!-- 用户头像 -->
        <div v-else class="avatar user-avatar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <div class="message-content">
          <template v-if="msg.fileInfo">
            <div class="message-text" v-if="msg.textBeforeFile">{{ msg.textBeforeFile }}</div>
            <FilePreview :subDir="msg.fileInfo.subDir" :fileName="msg.fileInfo.fileName" />
          </template>
          <template v-else>
            <span v-if="msg.isProgress" class="progress-indicator">
              <span class="progress-dot"></span>
              {{ msg.content }}
            </span>
            <span v-else>{{ msg.content }}</span>
          </template>
        </div>
      </div>
      <!-- 加载动画 -->
      <div v-if="isLoading" class="message ai-message">
        <div class="avatar ai-avatar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2z"/>
            <circle cx="8.5" cy="14" r="1.5" fill="currentColor"/>
            <circle cx="15.5" cy="14" r="1.5" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-content">
          <div class="typing-indicator">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
          </div>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <div class="input-wrapper">
        <input
            v-model="inputMessage"
            type="text"
            placeholder="输入您的问题，按 Enter 发送..."
            @keyup.enter="sendMessage"
            :disabled="isLoading"
        />
        <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
          <svg v-if="!isLoading" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"/>
            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          <svg v-else class="spin" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 11-6.219-8.56"/>
          </svg>
        </button>
      </div>
    </div>
    <div class="footer">
      <p>&copy; 2026 ZJZ AI Agent | 站长：郑少 | <a href="mailto:2302181496@qq.com">2302181496@qq.com</a></p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { API_BASE_URL } from '../config'
import FilePreview from '../components/FilePreview.vue'

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
let eventSource = null

// 支持更多文件类型检测
const FILE_PATH_REGEX = /(?:tmp[\\/])(file|pdf|download|doc|docx)[\\/](.+?\.(?:pdf|txt|md|doc|docx|csv|json|jpg|jpeg|png|gif|webp|bmp|svg|html|xml|log|yml|yaml|zip|rar|xlsx|xls|pptx))(?:["\s,;]|$)/i
// 备用简单正则：匹配 tmp 下任何子目录的文件
const FILE_PATH_SIMPLE_REGEX = /(?:tmp[\\/])(\w+)[\\/](\S+\.\w{2,5})/

const parseFileInfo = (content) => {
  let match = content.match(FILE_PATH_REGEX)
  if (!match) {
    match = content.match(FILE_PATH_SIMPLE_REGEX)
  }
  if (match) {
    return {
      subDir: match[1],
      fileName: match[2].replace(/\\/g, '').replace(/[",;]+$/, '')
    }
  }
  return null
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const resetLoadingState = () => {
  isLoading.value = false
  if (eventSource) {
    try {
      eventSource.close()
    } catch (e) {
      // ignore
    }
    eventSource = null
  }
}

const clearMessages = () => {
  messages.value = [{
    isUser: false,
    content: '你好！我是AI超级智能体，有什么问题需要我帮您解决吗？我可以帮您搜索信息、读写文件、生成文档、发送邮件等。'
  }]
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  messages.value.push({ isUser: true, content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  const url = `${API_BASE_URL}/ai/zjz_manus/chat/SseEmitter?message=${encodeURIComponent(userMessage)}`

  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(url)
  let hasReceivedData = false
  let messageCount = 0

  const timeoutId = setTimeout(() => {
    if (isLoading.value) {
      resetLoadingState()
      messages.value.push({ isUser: false, content: '请求超时，请重试。' })
      scrollToBottom()
    }
  }, 120000)

  eventSource.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      clearTimeout(timeoutId)
      resetLoadingState()
      scrollToBottom()
    } else {
      hasReceivedData = true
      messageCount++

      const fileInfo = parseFileInfo(data)
      const isProgress = data.startsWith('正在执行第') || data.startsWith('好的，我收到了')
      const msg = {
        isUser: false,
        content: data,
        isProgress: isProgress
      }

      if (fileInfo) {
        msg.fileInfo = fileInfo
        // 提取文件路径前的文字
        const fileIdx = data.indexOf('tmp')
        if (fileIdx > 0) {
          msg.textBeforeFile = data.substring(0, fileIdx).replace(/[:\s]+$/, '')
        }
      }

      messages.value.push(msg)
      scrollToBottom()

      // 检测任务完成关键词
      if (data.includes('任务结束') || data.includes('圆满结束') ||
          data.includes('任务已完成') || data.includes('已成功生成') ||
          data.includes('已达到最大步骤限制')) {
        setTimeout(() => {
          clearTimeout(timeoutId)
          resetLoadingState()
          scrollToBottom()
        }, 500)
      }
    }
  }

  eventSource.onopen = () => {
    console.log('SSE 连接已建立')
  }

  eventSource.onerror = (error) => {
    clearTimeout(timeoutId)
    if (eventSource.readyState === EventSource.CLOSED) {
      console.log('SSE 连接已正常关闭')
      resetLoadingState()
    } else if (hasReceivedData) {
      // 已收到数据后的error通常是服务端正常关闭连接，不算错误
      console.log('SSE 连接正常结束（已收到数据）')
      resetLoadingState()
    } else {
      // 没有收到任何数据就出错，才是真的错误
      console.error('SSE Error:', error)
      messages.value.push({ isUser: false, content: '连接出错，请重试。' })
      resetLoadingState()
      scrollToBottom()
    }
  }
}

onMounted(() => {
  messages.value.push({
    isUser: false,
    content: '你好！我是AI超级智能体，有什么问题需要我帮您解决吗？我可以帮您搜索信息、读写文件、生成文档、发送邮件等。'
  })
})

onUnmounted(() => {
  resetLoadingState()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  background: #f7f8fc;
}

/* Header */
.chat-header {
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  gap: 14px;
}

.header-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
}

.header-text {
  flex: 1;
}

.header-text h1 {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.5px;
}

.header-subtitle {
  font-size: 0.75rem;
  opacity: 0.8;
  margin-top: 2px;
  display: block;
}

.clear-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: rgba(255,255,255,0.15);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.clear-btn:hover {
  background: rgba(255,255,255,0.3);
  transform: scale(1.05);
}

/* Messages */
.chat-messages {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: #f7f8fc;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 88%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

/* Avatar */
.avatar {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 0.75rem;
  font-weight: 700;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

/* Message Content */
.message-content {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  font-size: 0.92rem;
  word-wrap: break-word;
  white-space: pre-wrap;
  overflow-wrap: break-word;
}

.ai-message .message-content {
  background: white;
  color: #2d3748;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.user-message .message-content {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.message-text {
  margin-bottom: 8px;
  color: #555;
}

/* Progress message */
.progress-message .message-content {
  background: #f0f4ff;
  color: #667eea;
  font-size: 0.85rem;
  padding: 8px 14px;
}

.progress-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #667eea;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

/* Typing indicator */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 4px 0;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) { animation-delay: 0s; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* Input */
.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #e8ecf4;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #f7f8fc;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  padding: 6px 6px 6px 18px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.chat-input input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  outline: none;
  color: #2d3748;
  padding: 8px 0;
}

.chat-input input::placeholder {
  color: #a0aec0;
}

.chat-input button {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.chat-input button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.4);
}

.chat-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Footer */
.footer {
  padding: 12px;
  text-align: center;
  font-size: 0.75rem;
  color: #a0aec0;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.footer a {
  color: #667eea;
  text-decoration: none;
}

.footer a:hover {
  text-decoration: underline;
}

/* Responsive */
@media (max-width: 768px) {
  .chat-container {
    max-width: 100%;
  }

  .header-content {
    padding: 12px 16px;
  }

  .header-subtitle {
    display: none;
  }

  .message {
    max-width: 92%;
  }

  .chat-messages {
    padding: 12px 10px;
  }

  .chat-input {
    padding: 12px 14px;
  }
}
</style>
