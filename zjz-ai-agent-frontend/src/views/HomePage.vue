<template>
  <div class="home-container">
    <div class="bg-grid"></div>
    <div class="bg-glow"></div>
    <div class="content-wrapper">
      <div class="header-section">
        <h1 class="home-title">
          <span class="title-glow">ZJZ AI</span>
          <span class="title-accent">AGENT</span>
        </h1>
        <div class="subtitle">
          <span class="cursor-blink">_</span>
        </div>
      </div>
      
      <div class="app-cards">
        <div class="app-card" @click="goToPlanApp">
          <div class="card-icon">✈️</div>
          <h2>途趣AI行程规划大师</h2>
          <p>智能行程规划，为您定制完美旅行方案</p>
          <div class="card-border"></div>
          <button>
            <span>进入应用</span>
            <span class="arrow">→</span>
          </button>
        </div>
        <div class="app-card" @click="goToZjzManus">
          <div class="card-icon">🤖</div>
          <h2>AI超级智能体</h2>
          <p>多步骤智能处理，解决复杂问题</p>
          <div class="card-border"></div>
          <button>
            <span>进入应用</span>
            <span class="arrow">→</span>
          </button>
        </div>
      </div>
      
      <div class="footer">
        <div class="footer-line"></div>
        <p>
          <span class="footer-text">© 2026 ZJZ AI Agent</span>
          <span class="separator">|</span>
          <span class="footer-text">站长：郑少</span>
          <span class="separator">|</span>
          <a href="mailto:2302181496@qq.com" class="footer-link">2302181496@qq.com</a>
        </p>
      </div>
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

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0f 0%, #1a1a2e 50%, #0f0f1a 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(0, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.bg-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.15) 0%, transparent 70%);
  top: -200px;
  left: 50%;
  transform: translateX(-50%);
  filter: blur(80px);
}

.content-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  width: 100%;
}

.header-section {
  text-align: center;
  margin-bottom: 80px;
}

.home-title {
  font-size: 4.5rem;
  font-weight: 900;
  letter-spacing: 8px;
  margin: 0 0 20px 0;
  font-family: 'Courier New', monospace;
}

.title-glow {
  color: #00ffff;
  text-shadow: 
    0 0 10px #00ffff,
    0 0 20px #00ffff,
    0 0 40px #00ffff,
    0 0 80px #00ffff;
  animation: glowPulse 2s ease-in-out infinite;
}

.title-accent {
  color: #ff00ff;
  text-shadow: 
    0 0 10px #ff00ff,
    0 0 20px #ff00ff,
    0 0 40px #ff00ff,
    0 0 80px #ff00ff;
  animation: glowPulse 2s ease-in-out infinite;
  animation-delay: 1s;
}

@keyframes glowPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.subtitle {
  font-size: 1.2rem;
  color: #666;
  font-family: 'Courier New', monospace;
}

.cursor-blink {
  color: #00ffff;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.app-cards {
  display: flex;
  gap: 50px;
  justify-content: center;
  align-items: stretch;
  flex-wrap: wrap;
  margin-bottom: 80px;
  padding: 0 20px;
}

.app-card {
  background: rgba(20, 20, 35);
  border: 2px solid rgba(0, 255, 255, 0.3);
  border-radius: 20px;
  padding: 50px 40px;
  width: 100%;
  max-width: 450px;
  flex: 1;
  min-width: 320px;
  text-align: center;
  cursor: pointer;
  position: relative;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.1) 0%, transparent 50%, rgba(255, 0, 255, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.4s;
}

.app-card:hover {
  transform: translateY(-10px) scale(1.02);
  border-color: #00ffff;
  box-shadow: 
    0 0 20px rgba(0, 255, 255, 0.4),
    0 0 40px rgba(0, 255, 255, 0.2),
    0 20px 40px rgba(0, 0, 0, 0.5);
}

.app-card:hover::before {
  opacity: 1;
}

.app-card:nth-child(2):hover {
  border-color: #ff00ff;
  box-shadow: 
    0 0 20px rgba(255, 0, 255, 0.4),
    0 0 40px rgba(255, 0, 255, 0.2),
    0 20px 40px rgba(0, 0, 0, 0.5);
}

.card-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  filter: drop-shadow(0 0 20px rgba(0, 255, 255, 0.5));
}

.app-card:nth-child(2) .card-icon {
  filter: drop-shadow(0 0 20px rgba(255, 0, 255, 0.5));
}

.app-card h2 {
  font-size: 1.6rem;
  color: #fff;
  margin: 0 0 15px 0;
  font-weight: 700;
  letter-spacing: 2px;
}

.app-card p {
  color: #aaa;
  font-size: 1rem;
  line-height: 1.6;
  margin: 0 0 25px 0;
}

.card-border {
  height: 2px;
  background: linear-gradient(90deg, transparent, #00ffff, transparent);
  margin-bottom: 25px;
}

.app-card:nth-child(2) .card-border {
  background: linear-gradient(90deg, transparent, #ff00ff, transparent);
}

.app-card button {
  background: transparent;
  border: 2px solid #00ffff;
  color: #00ffff;
  padding: 14px 40px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 0 auto;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.app-card:nth-child(2) button {
  border-color: #ff00ff;
  color: #ff00ff;
}

.app-card button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.app-card:nth-child(2) button::before {
  background: linear-gradient(90deg, transparent, rgba(255, 0, 255, 0.3), transparent);
}

.app-card button:hover::before {
  left: 100%;
}

.app-card button:hover {
  background: #00ffff;
  color: #0a0a0f;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
}

.app-card:nth-child(2) button:hover {
  background: #ff00ff;
  color: #0a0a0f;
  box-shadow: 0 0 20px rgba(255, 0, 255, 0.5);
}

.app-card button .arrow {
  transition: transform 0.3s;
}

.app-card button:hover .arrow {
  transform: translateX(5px);
}

.footer {
  text-align: center;
}

.footer-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 255, 255, 0.5), transparent);
  margin-bottom: 30px;
}

.footer p {
  color: #666;
  font-size: 0.95rem;
  margin: 0;
  font-family: 'Courier New', monospace;
}

.footer-text {
  color: #888;
}

.separator {
  color: #00ffff;
  margin: 0 15px;
}

.footer-link {
  color: #00ffff;
  text-decoration: none;
  transition: all 0.3s;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.footer-link:hover {
  color: #ff00ff;
  text-shadow: 0 0 10px rgba(255, 0, 255, 0.5);
}

@media (max-width: 768px) {
  .home-title {
    font-size: 2.5rem;
    letter-spacing: 4px;
  }
  
  .app-cards {
    gap: 20px;
  }
  
  .app-card {
    padding: 30px 20px;
  }
}
</style>