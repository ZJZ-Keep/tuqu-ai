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
          :class="['message', msg.isUser ? 'user-message' : 'ai-message',
                  'kind-' + msg.kind, msg.kind === 'progress' ? 'progress-message' : '']"
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
          <!-- 文件卡片 -->
          <template v-if="msg.kind === 'file'">
            <FilePreview :subDir="msg.fileInfo.subDir" :fileName="msg.fileInfo.fileName" />
          </template>

          <!-- 工具执行卡片：一句话结论 + 可折叠的原始输出 -->
          <template v-else-if="msg.kind === 'tool'">
            <div class="tool-card" :class="{ failed: msg.success === false }">
              <div class="tool-head" @click="toggleDetail(msg)">
                <span class="tool-icon">
                  <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="3"/>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.6 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.6a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9v.09a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
                  </svg>
                </span>
                <span class="tool-label">{{ msg.nameLabel }}</span>
                <span class="tool-summary">{{ msg.content }}</span>
                <span class="tool-status" :class="msg.success === false ? 'bad' : 'good'">
                  {{ msg.success === false ? '失败' : '完成' }}
                </span>
                <span v-if="msg.detail" class="tool-toggle">{{ msg.expanded ? '收起' : '详情' }}</span>
              </div>
              <pre v-if="msg.expanded && msg.detail" class="tool-detail">{{ msg.detail }}</pre>
            </div>
          </template>

          <!-- 错误 -->
          <template v-else-if="msg.kind === 'error'">
            <div class="error-bubble">
              <span class="error-icon">!</span>
              <span>{{ msg.content }}</span>
            </div>
          </template>

          <!-- 最终答复 -->
          <template v-else-if="msg.kind === 'final'">
            <div class="final-text">{{ msg.content }}</div>
            <div class="final-badge">已完成</div>
          </template>

          <!-- 进度 / 思考 / 普通文本 -->
          <template v-else>
            <span v-if="msg.kind === 'progress'" class="progress-indicator">
              <span class="progress-dot"></span>
              {{ msg.content }}
            </span>
            <span v-else class="plain-text">{{ msg.content }}</span>
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
let inactivityTimer = null

const WELCOME = '你好！我是AI超级智能体，有什么问题需要我帮您解决吗？我可以帮您搜索信息、读写文件、生成文档、发送邮件等。'

// 无任何事件的最长等待时间（服务端每 15 秒有心跳，正常不会触发）
const INACTIVITY_TIMEOUT = 10 * 60 * 1000

const TOOL_LABELS = {
  writeFile: '写入文件',
  readFile: '读取文件',
  generatePDF: '生成 PDF',
  sendTextEmail: '发送邮件',
  sendEmailWithAttachment: '发送带附件邮件',
  executeTerminalCommand: '执行命令',
  downloadResource: '下载资源',
  scrapeWeb: '网页抓取',
  searchWeb: '网络搜索',
  doTerminate: '结束任务'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const clearTimers = () => {
  if (inactivityTimer) {
    clearTimeout(inactivityTimer)
    inactivityTimer = null
  }
}

const stopStream = () => {
  isLoading.value = false
  clearTimers()
  if (eventSource) {
    try {
      eventSource.close()
    } catch (e) {
      // ignore
    }
    eventSource = null
  }
}

const resetInactivityTimer = () => {
  clearTimers()
  inactivityTimer = setTimeout(() => {
    if (isLoading.value) {
      isLoading.value = false
      messages.value.push({
        kind: 'error',
        content: '长时间没有收到新的进度，任务可能仍在后台继续执行。可以稍后刷新页面查看，或重新发起请求。'
      })
      scrollToBottom()
    }
  }, INACTIVITY_TIMEOUT)
}

const clearMessages = () => {
  stopStream()
  messages.value = [{ isUser: false, kind: 'text', content: WELCOME }]
}

const toggleDetail = (msg) => {
  if (!msg.detail) return
  msg.expanded = !msg.expanded
}

const pushProgress = (text) => {
  const last = messages.value[messages.value.length - 1]
  if (last && last.kind === 'progress') {
    last.content = text
  } else {
    messages.value.push({ isUser: false, kind: 'progress', content: text })
  }
}

const pushFile = (subDir, fileName) => {
  if (!subDir || !fileName) return
  const exists = messages.value.some(
    (m) => m.kind === 'file' && m.fileInfo && m.fileInfo.subDir === subDir && m.fileInfo.fileName === fileName
  )
  if (exists) return
  messages.value.push({
    isUser: false,
    kind: 'file',
    fileName,
    fileInfo: { subDir, fileName }
  })
}

const handleEvent = (evt) => {
  switch (evt.type) {
    case 'progress':
      pushProgress(evt.message || '正在处理…')
      break
    case 'thinking':
      if (evt.message) {
        messages.value.push({ isUser: false, kind: 'thinking', content: evt.message })
      }
      break
    case 'tool':
      messages.value.push({
        isUser: false,
        kind: 'tool',
        name: evt.name,
        nameLabel: TOOL_LABELS[evt.name] || evt.name || '工具',
        content: evt.message || '执行完成',
        detail: evt.detail || '',
        success: evt.success !== false,
        expanded: false
      })
      break
    case 'file':
      pushFile(evt.subDir, evt.fileName)
      break
    case 'final':
      if (evt.message) {
        messages.value.push({ isUser: false, kind: 'final', content: evt.message })
      }
      break
    case 'error':
      messages.value.push({ isUser: false, kind: 'error', content: evt.message || '执行出错' })
      break
    case 'done':
      finishStream()
      break
    case 'ping':
      // 心跳只用于重置超时计时器，不刷新界面
      return
    default:
      if (evt.message) {
        messages.value.push({ isUser: false, kind: 'thinking', content: evt.message })
      }
  }
  scrollToBottom()
}

const finishStream = () => {
  // 任务结束后清掉临时进度提示
  messages.value = messages.value.filter((m) => m.kind !== 'progress')
  stopStream()
  scrollToBottom()
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  messages.value.push({ isUser: true, kind: 'user', content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  const url = `${API_BASE_URL}/ai/zjz_manus/chat/SseEmitter?message=${encodeURIComponent(userMessage)}`

  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(url)
  let hasReceivedData = false
  resetInactivityTimer()

  eventSource.onmessage = (event) => {
    const raw = event.data
    if (!raw) return
    resetInactivityTimer()

    if (raw === '[DONE]') {
      finishStream()
      return
    }

    hasReceivedData = true
    let evt
    try {
      evt = JSON.parse(raw)
    } catch (e) {
      // 兼容旧的纯文本消息：过滤掉原始 “stepN结果：工具：...” 内容
      if (/^step\d+结果[：:]/.test(raw)) {
        return
      }
      const isProgress = raw.startsWith('正在执行第') || raw.startsWith('好的，我收到了')
      evt = {
        type: isProgress ? 'progress' : 'thinking',
        message: raw
      }
    }
    handleEvent(evt)
  }

  eventSource.onopen = () => {
    console.log('SSE 连接已建立')
  }

  eventSource.onerror = () => {
    if (eventSource && eventSource.readyState === EventSource.CLOSED) {
      console.log('SSE 连接已关闭')
      finishStream()
    } else if (hasReceivedData) {
      // 服务端已结束推送时常见，按正常结束处理
      finishStream()
    } else {
      isLoading.value = false
      clearTimers()
      messages.value.push({ isUser: false, kind: 'error', content: '连接后端失败，请确认后端服务已启动后重试。' })
      scrollToBottom()
    }
  }
}

onMounted(() => {
  messages.value.push({ isUser: false, kind: 'text', content: WELCOME })
})

onUnmounted(() => {
  stopStream()
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
  min-width: 0;
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

/* 进度 */
.progress-message .message-content {
  background: #f0f4ff;
  color: #667eea;
  font-size: 0.85rem;
  padding: 8px 14px;
  box-shadow: none;
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
  flex-shrink: 0;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

/* 工具卡片 */
.kind-tool .message-content {
  padding: 6px 8px;
  background: #f8fafc;
  border: 1px solid #e8edf5;
  box-shadow: none;
  width: 100%;
}

.tool-card {
  border-radius: 10px;
  overflow: hidden;
  background: white;
}

.tool-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  cursor: pointer;
  font-size: 0.82rem;
  user-select: none;
}

.tool-head:hover {
  background: #f7f9ff;
}

.tool-icon {
  color: #8b9bb4;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.tool-label {
  color: #4a5568;
  font-weight: 600;
  flex-shrink: 0;
}

.tool-summary {
  flex: 1;
  min-width: 0;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-status {
  font-size: 0.72rem;
  padding: 2px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}

.tool-status.good {
  color: #047857;
  background: #d1fae5;
}

.tool-status.bad {
  color: #b91c1c;
  background: #fee2e2;
}

.tool-card.failed .tool-icon {
  color: #ef4444;
}

.tool-toggle {
  font-size: 0.72rem;
  color: #667eea;
  flex-shrink: 0;
}

.tool-detail {
  margin: 0;
  padding: 10px 12px;
  border-top: 1px solid #eef2f7;
  background: #fbfcfe;
  color: #475569;
  font-size: 0.74rem;
  line-height: 1.55;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 240px;
  overflow-y: auto;
}

/* 错误 */
.kind-error .message-content {
  background: #fff5f5;
  border: 1px solid #fed7d7;
  color: #c53030;
  box-shadow: none;
}

.error-bubble {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.error-icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fc8181;
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

/* 最终答复 */
.kind-final .message-content {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  box-shadow: none;
}

.final-badge {
  display: inline-block;
  margin-top: 8px;
  font-size: 0.7rem;
  color: #047857;
  background: #d1fae5;
  padding: 2px 8px;
  border-radius: 999px;
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
