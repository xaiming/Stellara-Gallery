<script setup lang="ts">
import {
  EyeInvisibleOutlined,
  GithubOutlined,
  GlobalOutlined,
  LockOutlined,
  MailOutlined,
  QqOutlined,
  UserOutlined,
  WechatOutlined,
} from '@ant-design/icons-vue'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cacheLoginUser, loginUser, registerUser, type UserVO } from '../../api/user'

const router = useRouter()
const route = useRoute()
const mode = ref<'login' | 'register'>('login')
const loading = ref(false)
const message = ref('')
const form = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userName: '',
})

const isLoginMode = computed(() => mode.value === 'login')

const switchMode = () => {
  message.value = ''
  mode.value = isLoginMode.value ? 'register' : 'login'
}

const handleSubmit = async () => {
  message.value = ''
  loading.value = true
  try {
    let loginUserInfo: UserVO
    if (isLoginMode.value) {
      loginUserInfo = await loginUser({
        userAccount: form.userAccount,
        userPassword: form.userPassword,
      })
    } else {
      await registerUser({
        userAccount: form.userAccount,
        userPassword: form.userPassword,
        checkPassword: form.checkPassword,
        userName: form.userName,
      })
      loginUserInfo = await loginUser({
        userAccount: form.userAccount,
        userPassword: form.userPassword,
      })
    }
    cacheLoginUser(loginUserInfo)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/gallery'
    await router.push(redirect)
  } catch (error) {
    message.value = error instanceof Error ? error.message : '操作失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <button class="language-select" type="button">
      <GlobalOutlined />
      <span>简体中文</span>
      <span>⌄</span>
    </button>

    <section class="auth-brand">
      <div class="auth-logo">
        <img src="../../assets/stellara-moon-logo.png" alt="星璃云图月亮图标" />
      </div>
      <div>
        <h1>星璃云图</h1>
        <p>STELLARA GALLERY</p>
      </div>
    </section>

    <section class="auth-stage">
      <div class="auth-panel">
        <div class="auth-copy">
          <h2>{{ isLoginMode ? '欢迎来到星璃云图' : '加入星璃云图' }} <span>✦</span></h2>
          <p>{{ isLoginMode ? '登录后继续探索你的梦幻图库' : '创建账号，开启你的梦幻图库之旅' }}</p>
        </div>

        <form class="auth-form" @submit.prevent="handleSubmit">
          <label>
            <span>账号</span>
            <div class="auth-input">
              <UserOutlined />
              <input
                v-model.trim="form.userAccount"
                required
                minlength="4"
                :placeholder="isLoginMode ? '请输入账号 / 邮箱 / 手机号' : '请输入账号（6-20位字母、数字或下划线）'"
              />
            </div>
          </label>

          <label v-if="!isLoginMode">
            <span>昵称</span>
            <div class="auth-input">
              <UserOutlined />
              <input v-model.trim="form.userName" placeholder="请输入昵称（2-16位）" />
            </div>
          </label>

          <label v-if="!isLoginMode">
            <span>邮箱</span>
            <div class="auth-input">
              <MailOutlined />
              <input placeholder="请输入邮箱地址" />
            </div>
          </label>

          <label>
            <span>密码</span>
            <div class="auth-input">
              <LockOutlined />
              <input
                v-model="form.userPassword"
                required
                minlength="8"
                type="password"
                :placeholder="isLoginMode ? '请输入密码' : '请输入密码（8-20位，含字母和数字）'"
              />
              <EyeInvisibleOutlined />
            </div>
          </label>

          <label v-if="!isLoginMode">
            <span>确认密码</span>
            <div class="auth-input">
              <LockOutlined />
              <input
                v-model="form.checkPassword"
                required
                minlength="8"
                type="password"
                placeholder="请再次输入密码"
              />
              <EyeInvisibleOutlined />
            </div>
          </label>

          <div class="auth-row">
            <label class="remember-check">
              <input type="checkbox" />
              <span>{{ isLoginMode ? '记住我' : '我已阅读并同意 用户协议 与 隐私政策' }}</span>
            </label>
            <a v-if="isLoginMode" href="#">忘记密码？</a>
          </div>

          <p v-if="message" class="auth-message">{{ message }}</p>

          <button class="auth-submit" :disabled="loading" type="submit">
            {{ loading ? '处理中...' : isLoginMode ? '登录' : '立即注册' }}
          </button>
          <button class="auth-switch" type="button" @click="switchMode">
            {{ isLoginMode ? '还没有账号？ 立即注册' : '已经有账号？ 立即登录' }}
          </button>

          <div class="auth-divider">
            <span>{{ isLoginMode ? '其他登录方式' : '其他注册方式' }}</span>
          </div>
          <div class="social-actions">
            <button type="button">
              <QqOutlined />
              <span>QQ{{ isLoginMode ? '登录' : '注册' }}</span>
            </button>
            <button type="button">
              <WechatOutlined />
              <span>微信{{ isLoginMode ? '登录' : '注册' }}</span>
            </button>
            <button type="button">
              <GithubOutlined />
              <span>GitHub</span>
            </button>
          </div>
        </form>
      </div>

      <aside class="auth-visual">
        <img class="auth-girl" src="../../assets/stellara-auth-girl.png" alt="星璃云图二次元少女视觉图" />
        <p>“{{ isLoginMode ? '每一张图片，都是一段故事的星光。' : '收藏每一份灵感，点亮属于你的星图。' }}”</p>
        <span>—— 星璃酱</span>
      </aside>
    </section>

    <footer class="auth-footer">
      <nav>
        <a href="#">用户协议</a>
        <a href="#">隐私政策</a>
        <a href="#">社区规范</a>
        <a href="#">帮助中心</a>
        <a href="#">联系我们</a>
      </nav>
      <p>© 2024 星璃云图 Stellara Gallery. 保留所有权利。</p>
    </footer>
  </main>
</template>
