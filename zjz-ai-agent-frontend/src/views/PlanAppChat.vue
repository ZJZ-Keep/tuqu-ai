<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <button class="back-btn" @click="goHome" title="返回首页">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          <span class="back-text">返回</span>
        </button>
        <div class="header-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
        <div class="header-text">
          <h1>途趣AI行程规划大师</h1>
          <span class="header-subtitle">专业旅行规划AI，为您定制详细、合理、可落地的行程方案</span>
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
          :class="['message', msg.isUser ? 'user-message' : 'ai-message']"
      >
        <div v-if="!msg.isUser" class="avatar ai-avatar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
        <div v-else class="avatar user-avatar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <div class="message-content">
          <template v-if="msg.fileInfo">
            <FilePreview :subDir="msg.fileInfo.subDir" :fileName="msg.fileInfo.fileName" />
          </template>
          <template v-else>
            <span v-html="formatContent(msg.content)"></span>
          </template>
        </div>
      </div>
      <!-- 加载动画 -->
      <div v-if="isLoading" class="message ai-message">
        <div class="avatar ai-avatar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
            <circle cx="12" cy="10" r="3"/>
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
            placeholder="输入您的旅行需求，按 Enter 发送..."
            @keyup.enter="sendMessage"
            :disabled="isLoading"
        />
        <button
            class="send-btn"
            @click="sendMessage"
            :disabled="isLoading || !inputMessage.trim()"
            :title="isLoading ? '正在处理…' : '发送'"
        >
          <template v-if="!isLoading">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
              <path d="M3.4 20.4l17.4-8.4a.6.6 0 0 0 0-1.1L3.4 2.5a.6.6 0 0 0-.8.8L5.6 11H13a1 1 0 0 1 0 2H5.6l-3 7.6a.6.6 0 0 0 .8.8z"/>
            </svg>
            <span class="send-text">发送</span>
          </template>
          <svg v-else class="spin" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
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
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '../config'
import FilePreview from '../components/FilePreview.vue'

const router = useRouter()
const goHome = () => router.push({ name: 'home' })

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const chatId = ref('')
const messagesContainer = ref(null)
let eventSource = null

const FILE_PATH_REGEX = /(?:tmp[\\/])(file|pdf|download|doc|docx)[\\/](.+?\.(?:pdf|txt|md|doc|docx|csv|json|jpg|jpeg|png|gif|webp|bmp|svg|html|xml|log|yml|yaml|zip|rar|xlsx|xls|pptx))(?:["\s,;]|$)/i
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

const formatContent = (content) => {
  if (content.startsWith('{') || content.startsWith('[')) {
    try {
      const parsed = JSON.parse(content)
      return `<pre>${JSON.stringify(parsed, null, 2)}</pre>`
    } catch (e) {
      return content
    }
  }
  return content.replace(/\n/g, '<br>')
}

const generateChatId = () => {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const clearMessages = () => {
  messages.value = [{
    isUser: false,
    content: '你好！我是途趣AI行程规划大师，有什么可以帮您规划的旅行吗？'
  }]
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  messages.value.push({ isUser: true, content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  const aiMessageIndex = messages.value.length
  messages.value.push({ isUser: false, content: '' })

  const url = `${API_BASE_URL}/ai/plan_app/chat/Sse?message=${encodeURIComponent(userMessage)}&chatId=${chatId.value}`

  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(url)
  let hasReceivedData = false

  const timeoutId = setTimeout(() => {
    if (isLoading.value) {
      try {
        eventSource.close()
      } catch (e) {
        // ignore
      }
      isLoading.value = false
      if (!hasReceivedData) {
        messages.value[aiMessageIndex].content = '请求超时，请重试。'
      }
      scrollToBottom()
    }
  }, 60000)

  eventSource.onmessage = (event) => {
    const data = event.data
    const content = data.replace(/^data: /, '').trim()
    if (content === '[DONE]') {
      clearTimeout(timeoutId)
      eventSource.close()
      isLoading.value = false
      scrollToBottom()
    } else {
      hasReceivedData = true
      messages.value[aiMessageIndex].content += content
      scrollToBottom()

      if (content.includes('任务结束')) {
        const fileInfo = parseFileInfo(messages.value[aiMessageIndex].content)
        if (fileInfo) {
          messages.value[aiMessageIndex].fileInfo = fileInfo
        }
        setTimeout(() => {
          clearTimeout(timeoutId)
          try {
            eventSource.close()
          } catch (e) {
            // ignore
          }
          isLoading.value = false
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
    if (eventSource.readyState === 2) {
      console.log('SSE 连接正常关闭')
      isLoading.value = false
    } else if (hasReceivedData) {
      console.log('SSE 连接正常结束（已收到数据）')
      isLoading.value = false
    } else {
      try {
        eventSource.close()
      } catch (e) {
        // ignore
      }
      messages.value[aiMessageIndex].content += '连接出错，请重试'
      isLoading.value = false
      scrollToBottom()
    }
  }
}

onMounted(() => {
  chatId.value = generateChatId()
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

.chat-header {
  padding: 0;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #1a3a2a;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 12px rgba(67, 233, 123, 0.3);
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
  background: rgba(255,255,255,0.3);
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

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 36px;
  padding: 0 10px;
  border-radius: 10px;
  border: none;
  background: rgba(255,255,255,0.25);
  color: #1a3a2a;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
  flex-shrink: 0;
}

.back-btn:hover {
  background: rgba(255,255,255,0.4);
  transform: translateX(-2px);
}

.back-text {
  white-space: nowrap;
}

.clear-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: rgba(255,255,255,0.25);
  color: #1a3a2a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.clear-btn:hover {
  background: rgba(255,255,255,0.4);
  transform: scale(1.05);
}

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
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: #1a3a2a;
  box-shadow: 0 2px 8px rgba(67, 233, 123, 0.3);
}

.user-avatar {
  background: linear-gradient(135deg, #f6d365, #fda085);
  color: #5a3a1a;
  box-shadow: 0 2px 8px rgba(253, 160, 133, 0.3);
}

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
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: #1a3a2a;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(67, 233, 123, 0.25);
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 4px 0;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #43e97b;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) { animation-delay: 0s; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

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
  border-color: #43e97b;
  box-shadow: 0 0 0 3px rgba(67, 233, 123, 0.15);
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
  height: 40px;
  min-width: 64px;
  padding: 0 14px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: #14352a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.2s;
  flex-shrink: 0;
}

.chat-input button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 3px 12px rgba(67, 233, 123, 0.4);
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

.footer {
  padding: 12px;
  text-align: center;
  font-size: 0.75rem;
  color: #a0aec0;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.footer a {
  color: #43e97b;
  text-decoration: none;
}

.footer a:hover {
  text-decoration: underline;
}

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

  .back-text {
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
