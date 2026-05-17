<script setup lang="ts">
import {
  BellOutlined,
  HeartOutlined,
  LogoutOutlined,
  MailOutlined,
  SearchOutlined,
  StarFilled,
} from '@ant-design/icons-vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getLoginUser, logoutUser, type UserVO } from '../api/user'
import SidebarMenu from '../components/layout/SidebarMenu.vue'

const router = useRouter()
const currentUser = ref<UserVO | null>(null)

const loadCurrentUser = async () => {
  try {
    currentUser.value = await getLoginUser()
  } catch {
    currentUser.value = null
  }
}

const handleLogout = async () => {
  try {
    await logoutUser()
  } finally {
    currentUser.value = null
    await router.push('/login')
  }
}

const goProfile = () => {
  router.push('/profile')
}

onMounted(loadCurrentUser)
</script>

<template>
  <div class="basic-layout">
    <SidebarMenu />

    <section class="layout-shell">
      <header class="top-header glass-panel">
        <label class="search-box">
          <SearchOutlined />
          <input placeholder="搜索图片、标签、分类..." />
        </label>

        <div class="header-actions">
          <button class="icon-button" aria-label="通知">
            <BellOutlined />
          </button>
          <button class="icon-button" aria-label="消息">
            <MailOutlined />
          </button>
          <button class="icon-button" aria-label="收藏">
            <HeartOutlined />
          </button>
          <button class="user-card profile-entry" type="button" @click="goProfile">
            <div class="avatar">
              <img v-if="currentUser?.userAvatar" :src="currentUser.userAvatar" alt="用户头像" />
              <StarFilled v-else />
            </div>
            <div>
              <strong>{{ currentUser?.userName || currentUser?.userAccount || '未登录' }}</strong>
              <span>{{ currentUser?.userRole === 'admin' ? '管理员' : currentUser ? '普通用户' : '游客' }}</span>
            </div>
          </button>
          <button class="icon-button" aria-label="退出登录" title="退出登录" @click="handleLogout">
            <LogoutOutlined />
          </button>
        </div>
      </header>

      <main class="main-content">
        <RouterView />
      </main>
    </section>
  </div>
</template>
