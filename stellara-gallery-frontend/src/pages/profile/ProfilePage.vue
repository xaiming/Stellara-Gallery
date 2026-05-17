<script setup lang="ts">
import {
  CameraOutlined,
  CloudOutlined,
  EditOutlined,
  EnvironmentOutlined,
  EyeOutlined,
  HeartFilled,
  HeartOutlined,
  HistoryOutlined,
  PictureOutlined,
  SaveOutlined,
  ShareAltOutlined,
  StarFilled,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { cacheLoginUser, getCachedLoginUser, getLoginUser, updateUser, type UserVO } from '../../api/user'

interface ArtworkCard {
  title: string
  tag: string
  views: string
  likes: string
  gradient: string
}

interface ActivityItem {
  title: string
  time: string
  tone: string
}

const currentUser = ref<UserVO | null>(null)
const loading = ref(false)
const saving = ref(false)
const editModalOpen = ref(false)
const errorMessage = ref('')

const editForm = reactive({
  userName: '',
  userAvatar: '',
  userCover: '',
  userProfile: '',
})

const artworks: ArtworkCard[] = [
  {
    title: '星桥上的祈愿',
    tag: '幻想插画',
    views: '2.4k',
    likes: '386',
    gradient: 'linear-gradient(135deg, #667bff 0%, #c78cff 52%, #ffd2f4 100%)',
  },
  {
    title: '云端图书馆',
    tag: '天空之城',
    views: '1.8k',
    likes: '245',
    gradient: 'linear-gradient(135deg, #83c4ff 0%, #8c84ff 48%, #f5b8ff 100%)',
  },
  {
    title: '月璃花园',
    tag: '治愈系',
    views: '3.1k',
    likes: '512',
    gradient: 'linear-gradient(135deg, #ffc2e2 0%, #aebcff 48%, #fff1bf 100%)',
  },
  {
    title: '流星邮差',
    tag: '角色设定',
    views: '986',
    likes: '132',
    gradient: 'linear-gradient(135deg, #1f2b75 0%, #696bff 52%, #f79bff 100%)',
  },
  {
    title: '夜航鲸歌',
    tag: '概念场景',
    views: '4.6k',
    likes: '729',
    gradient: 'linear-gradient(135deg, #304ab5 0%, #69c9ff 48%, #ded2ff 100%)',
  },
  {
    title: '粉雪星港',
    tag: '空间封面',
    views: '1.2k',
    likes: '208',
    gradient: 'linear-gradient(135deg, #ff9ed1 0%, #938dff 48%, #bceaff 100%)',
  },
]

const activities: ActivityItem[] = [
  { title: '更新了个人封面与简介', time: '今天 20:18', tone: '#8a75ff' },
  { title: '收藏作品「云端图书馆」', time: '昨天 18:42', tone: '#ff92c8' },
  { title: '加入团队空间「星港企划」', time: '05-12 14:06', tone: '#5dc8d5' },
  { title: '发布作品「月璃花园」', time: '05-10 21:33', tone: '#f0a35d' },
]

const displayUser = computed(() => currentUser.value)
const displayName = computed(() => displayUser.value?.userName || displayUser.value?.userAccount || '星璃旅人')
const displayAccount = computed(() => displayUser.value?.userAccount || '未登录账号')
const displayRole = computed(() => (displayUser.value?.userRole === 'admin' ? '管理员' : '普通用户'))
const displayStatus = computed(() => (displayUser.value?.userStatus === 1 ? '禁用' : '正常'))
const profileText = computed(
  () => displayUser.value?.userProfile || '在星璃云图收藏灵感、整理作品，也把每一次闪光的创作都放进自己的星空档案。',
)
const coverStyle = computed(() => ({
  backgroundImage: displayUser.value?.userCover
    ? `linear-gradient(110deg, rgba(31, 45, 135, 0.62), rgba(143, 104, 220, 0.34)), url(${displayUser.value.userCover})`
    : undefined,
}))

const stats = computed(() => [
  { label: '公开作品', value: '128', icon: PictureOutlined, desc: '静态作品展示' },
  { label: '收藏灵感', value: '1,406', icon: HeartFilled, desc: '常用素材合集' },
  { label: '团队空间', value: '6', icon: TeamOutlined, desc: '协作空间数量' },
  { label: '最后登录', value: formatTime(displayUser.value?.lastLoginTime), icon: HistoryOutlined, desc: '来自用户接口' },
])

const loadProfile = async () => {
  loading.value = true
  errorMessage.value = ''
  currentUser.value = currentUser.value ?? getCachedLoginUser()
  try {
    const loginUser = await getLoginUser()
    currentUser.value = loginUser
    cacheLoginUser(loginUser)
  } catch (error) {
    currentUser.value = getCachedLoginUser()
    errorMessage.value = error instanceof Error ? error.message : '个人资料加载失败'
  } finally {
    loading.value = false
  }
}

const openEditModal = () => {
  const user = currentUser.value
  editForm.userName = user?.userName ?? ''
  editForm.userAvatar = user?.userAvatar ?? ''
  editForm.userCover = user?.userCover ?? ''
  editForm.userProfile = user?.userProfile ?? ''
  editModalOpen.value = true
  errorMessage.value = ''
}

const submitProfile = async () => {
  if (!currentUser.value) {
    errorMessage.value = '请先登录后再编辑资料'
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await updateUser({
      id: currentUser.value.id,
      userName: editForm.userName.trim() || undefined,
      userAvatar: editForm.userAvatar.trim() || undefined,
      userCover: editForm.userCover.trim() || undefined,
      userProfile: editForm.userProfile.trim() || undefined,
    })
    editModalOpen.value = false
    await loadProfile()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '资料保存失败'
  } finally {
    saving.value = false
  }
}

const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '暂无记录')

onMounted(loadProfile)
</script>

<template>
  <section class="profile-page">
    <header class="profile-hero" :style="coverStyle">
      <div class="hero-skyline" aria-hidden="true">
        <span class="tower tower-a" />
        <span class="tower tower-b" />
        <span class="tower tower-c" />
      </div>

      <div class="hero-profile">
        <div class="profile-avatar">
          <img v-if="displayUser?.userAvatar" :src="displayUser.userAvatar" :alt="displayName" />
          <StarFilled v-else />
        </div>
        <div class="profile-copy">
          <div class="profile-kicker">
            <span>{{ displayRole }}</span>
            <span :class="{ disabled: displayUser?.userStatus === 1 }">{{ displayStatus }}</span>
          </div>
          <h2>{{ displayName }} ✨</h2>
          <p>{{ profileText }}</p>
          <div class="profile-meta">
            <span><UserOutlined /> {{ displayAccount }}</span>
            <span><EnvironmentOutlined /> 星璃云图创作者</span>
          </div>
        </div>
      </div>

      <div class="hero-actions">
        <button class="ghost-action">
          <ShareAltOutlined />
          分享主页
        </button>
        <button class="primary-action" @click="openEditModal">
          <EditOutlined />
          编辑资料
        </button>
      </div>
    </header>

    <div v-if="loading" class="profile-state">正在加载个人星图...</div>
    <div v-else-if="errorMessage && !displayUser" class="profile-state error">{{ errorMessage }}</div>

    <template v-else>
      <section class="profile-stats">
        <article v-for="item in stats" :key="item.label" class="profile-stat-card">
          <div class="stat-orb">
            <component :is="item.icon" />
          </div>
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.desc }}</small>
          </div>
        </article>
      </section>

      <div class="profile-grid">
        <main class="works-panel">
          <div class="section-head">
            <div>
              <span>Gallery</span>
              <h3>公开作品</h3>
            </div>
            <button>
              <CameraOutlined />
              上传作品
            </button>
          </div>

          <div class="artwork-grid">
            <article v-for="work in artworks" :key="work.title" class="artwork-card">
              <div class="artwork-thumb" :style="{ background: work.gradient }">
                <span class="star-dot" />
                <span class="moon-arc" />
              </div>
              <div class="artwork-info">
                <div>
                  <strong>{{ work.title }}</strong>
                  <span>{{ work.tag }}</span>
                </div>
                <div class="artwork-counts">
                  <span><EyeOutlined /> {{ work.views }}</span>
                  <span><HeartOutlined /> {{ work.likes }}</span>
                </div>
              </div>
            </article>
          </div>
        </main>

        <aside class="profile-side">
          <section class="side-card account-card">
            <div class="section-head compact">
              <div>
                <span>Account</span>
                <h3>账号信息</h3>
              </div>
            </div>
            <dl>
              <dt>用户 ID</dt>
              <dd>{{ displayUser?.id ?? '-' }}</dd>
              <dt>账号</dt>
              <dd>{{ displayAccount }}</dd>
              <dt>角色</dt>
              <dd>{{ displayRole }}</dd>
              <dt>创建时间</dt>
              <dd>{{ formatTime(displayUser?.createTime) }}</dd>
            </dl>
          </section>

          <section class="side-card storage-panel">
            <div class="storage-ring">
              <CloudOutlined />
              <strong>62%</strong>
              <span>空间使用</span>
            </div>
            <div class="storage-copy">
              <h3>云端容量</h3>
              <p>1.24 TB / 2 TB</p>
              <div class="profile-progress">
                <i />
              </div>
            </div>
          </section>

          <section class="side-card">
            <div class="section-head compact">
              <div>
                <span>Timeline</span>
                <h3>最近动态</h3>
              </div>
            </div>
            <ol class="activity-list">
              <li v-for="item in activities" :key="item.title">
                <i :style="{ background: item.tone }" />
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.time }}</span>
                </div>
              </li>
            </ol>
          </section>

          <section class="side-card quick-panel">
            <button><PictureOutlined /> 管理作品</button>
            <button><HeartOutlined /> 查看收藏</button>
            <button><TeamOutlined /> 团队空间</button>
          </section>
        </aside>
      </div>
    </template>

    <div v-if="editModalOpen" class="modal-mask">
      <form class="profile-modal" @submit.prevent="submitProfile">
        <div class="modal-title">
          <div>
            <h3>编辑个人资料 ✨</h3>
            <p>同步保存到后端用户信息</p>
          </div>
          <button type="button" @click="editModalOpen = false">×</button>
        </div>

        <label>
          <span>昵称</span>
          <input v-model="editForm.userName" placeholder="输入昵称" />
        </label>
        <label>
          <span>头像地址</span>
          <input v-model="editForm.userAvatar" placeholder="https://..." />
        </label>
        <label>
          <span>封面地址</span>
          <input v-model="editForm.userCover" placeholder="https://..." />
        </label>
        <label>
          <span>个人简介</span>
          <textarea v-model="editForm.userProfile" rows="4" placeholder="介绍你的星璃创作档案" />
        </label>
        <p v-if="errorMessage" class="modal-error">{{ errorMessage }}</p>
        <div class="modal-actions">
          <button type="button" class="ghost-action" @click="editModalOpen = false">取消</button>
          <button type="submit" class="primary-action" :disabled="saving">
            <SaveOutlined />
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </div>
  </section>
</template>

<style scoped>
.profile-page {
  position: relative;
  min-height: calc(100vh - 120px);
  overflow: hidden;
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 32px;
  color: #222c86;
  background:
    radial-gradient(circle at 90% 12%, rgba(255, 157, 229, 0.42), transparent 24%),
    radial-gradient(circle at 12% 14%, rgba(111, 140, 255, 0.45), transparent 28%),
    linear-gradient(135deg, rgba(244, 241, 255, 0.88), rgba(231, 240, 255, 0.76));
  box-shadow: 0 30px 80px rgba(72, 66, 168, 0.18);
}

.profile-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background-image:
    radial-gradient(circle, rgba(255, 255, 255, 0.95) 0 1px, transparent 2px),
    radial-gradient(circle, rgba(117, 108, 255, 0.36) 0 1px, transparent 2px);
  background-position: 18px 24px, 82px 68px;
  background-size: 88px 88px, 130px 130px;
  opacity: 0.48;
}

.profile-hero,
.profile-stats,
.profile-grid,
.profile-state {
  position: relative;
  z-index: 1;
}

.profile-hero {
  position: relative;
  display: flex;
  min-height: 286px;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  overflow: hidden;
  padding: 34px;
  border: 1px solid rgba(255, 255, 255, 0.54);
  border-radius: 30px;
  background:
    radial-gradient(circle at 72% 20%, rgba(255, 255, 255, 0.9) 0 1px, transparent 2px),
    radial-gradient(circle at 42% 10%, rgba(255, 208, 249, 0.48), transparent 20%),
    linear-gradient(110deg, rgba(38, 62, 170, 0.92), rgba(105, 102, 232, 0.68), rgba(255, 178, 229, 0.42)),
    url('../../assets/stellara-login-bg.png') center / cover no-repeat;
  background-size: auto, auto, auto, cover;
  box-shadow: 0 24px 62px rgba(71, 65, 178, 0.26);
}

.profile-hero::after {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    linear-gradient(115deg, transparent 0 68%, rgba(255, 255, 255, 0.36) 68.2%, transparent 68.7%),
    linear-gradient(180deg, transparent 0%, rgba(29, 41, 132, 0.38) 100%);
}

.hero-skyline {
  position: absolute;
  top: 42px;
  right: 18%;
  width: 230px;
  height: 124px;
  opacity: 0.82;
  filter: drop-shadow(0 0 28px rgba(255, 255, 255, 0.68));
}

.hero-skyline::before {
  position: absolute;
  right: 24px;
  bottom: 10px;
  width: 168px;
  height: 42px;
  border-radius: 24px 24px 8px 8px;
  content: '';
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(195, 207, 255, 0.52));
}

.tower {
  position: absolute;
  bottom: 38px;
  width: 28px;
  border-radius: 12px 12px 4px 4px;
  background: linear-gradient(180deg, #ffffff, rgba(187, 199, 255, 0.65));
}

.tower::before {
  position: absolute;
  left: 50%;
  bottom: 100%;
  width: 0;
  height: 0;
  border-right: 14px solid transparent;
  border-bottom: 30px solid rgba(255, 255, 255, 0.95);
  border-left: 14px solid transparent;
  content: '';
  transform: translateX(-50%);
}

.tower-a { right: 126px; height: 52px; }
.tower-b { right: 84px; height: 84px; }
.tower-c { right: 46px; height: 62px; }

.hero-profile {
  position: relative;
  z-index: 1;
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  display: grid;
  width: 112px;
  height: 112px;
  flex: 0 0 112px;
  place-items: center;
  overflow: hidden;
  border: 3px solid rgba(255, 255, 255, 0.82);
  border-radius: 36px;
  color: #ffffff;
  font-size: 42px;
  background:
    radial-gradient(circle at 35% 26%, #ffffff 0 7%, transparent 8%),
    linear-gradient(135deg, #8aa2ff, #df8cff);
  box-shadow: 0 18px 42px rgba(37, 41, 132, 0.28), 0 0 28px rgba(255, 224, 255, 0.72);
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-copy {
  max-width: 650px;
}

.profile-kicker,
.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-kicker span,
.profile-meta span {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  gap: 6px;
  padding: 0 11px;
  border: 1px solid rgba(255, 255, 255, 0.48);
  border-radius: 999px;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  font-weight: 800;
  backdrop-filter: blur(12px);
}

.profile-kicker .disabled {
  background: rgba(255, 128, 168, 0.26);
}

.profile-copy h2 {
  margin: 12px 0 10px;
  color: #ffffff;
  font-size: 38px;
  line-height: 1.12;
  text-shadow: 0 8px 26px rgba(28, 36, 126, 0.45);
}

.profile-copy p {
  max-width: 620px;
  margin: 0 0 14px;
  color: rgba(247, 249, 255, 0.92);
  font-size: 15px;
  line-height: 1.8;
  font-weight: 600;
}

.hero-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 0 0 auto;
  gap: 10px;
}

.primary-action,
.ghost-action,
.section-head button,
.quick-panel button {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 16px;
  padding: 0 16px;
  font-weight: 900;
}

.primary-action {
  border: 1px solid rgba(255, 255, 255, 0.74);
  color: #ffffff;
  background: linear-gradient(135deg, #5575ff, #df7eff);
  box-shadow: 0 12px 28px rgba(97, 83, 219, 0.34);
}

.primary-action:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.ghost-action,
.section-head button {
  border: 1px solid rgba(255, 255, 255, 0.48);
  color: #5260b0;
  background: rgba(255, 255, 255, 0.68);
}

.hero-actions .ghost-action {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.16);
}

.profile-state {
  display: grid;
  min-height: 260px;
  place-items: center;
  margin-top: 16px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  color: #5862ad;
  background: rgba(255, 255, 255, 0.58);
  font-weight: 900;
  backdrop-filter: blur(18px);
}

.profile-state.error {
  color: #d34d78;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.profile-stat-card,
.works-panel,
.side-card {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.64);
  box-shadow: 0 18px 42px rgba(78, 77, 171, 0.16);
  backdrop-filter: blur(22px);
}

.profile-stat-card {
  display: flex;
  min-height: 104px;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
}

.stat-orb {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 20px;
  color: #ffffff;
  font-size: 24px;
  background: linear-gradient(135deg, #6b7cff, #df8cff);
  box-shadow: inset 0 0 0 8px rgba(255, 255, 255, 0.2), 0 10px 22px rgba(92, 81, 200, 0.2);
}

.profile-stat-card span,
.profile-stat-card strong,
.profile-stat-card small {
  display: block;
}

.profile-stat-card span {
  color: #5c66af;
  font-size: 13px;
  font-weight: 800;
}

.profile-stat-card strong {
  margin: 4px 0;
  color: #1d2792;
  font-size: 23px;
}

.profile-stat-card small {
  color: #7880bd;
  font-size: 12px;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 316px;
  gap: 16px;
  margin-top: 16px;
}

.works-panel,
.side-card {
  border-radius: 24px;
}

.works-panel {
  min-width: 0;
  padding: 20px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-head span {
  color: #7b70d7;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.section-head h3 {
  margin: 4px 0 0;
  color: #24309b;
  font-size: 22px;
}

.section-head.compact {
  margin-bottom: 12px;
}

.section-head.compact h3 {
  font-size: 18px;
}

.artwork-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.artwork-card {
  overflow: hidden;
  border: 1px solid rgba(126, 124, 218, 0.14);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.56);
}

.artwork-thumb {
  position: relative;
  height: 164px;
  overflow: hidden;
}

.artwork-thumb::before,
.artwork-thumb::after {
  position: absolute;
  content: '';
}

.artwork-thumb::before {
  right: 22px;
  bottom: 24px;
  width: 106px;
  height: 40px;
  border-radius: 20px 20px 8px 8px;
  background: rgba(255, 255, 255, 0.58);
  filter: blur(0.2px);
}

.artwork-thumb::after {
  left: 22px;
  top: 26px;
  width: 72px;
  height: 72px;
  border: 2px solid rgba(255, 255, 255, 0.78);
  border-radius: 50%;
}

.star-dot {
  position: absolute;
  right: 32px;
  top: 28px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 22px 32px 0 rgba(255, 255, 255, 0.8), -54px 42px 0 rgba(255, 255, 255, 0.72);
}

.moon-arc {
  position: absolute;
  left: 50%;
  bottom: 42px;
  width: 120px;
  height: 42px;
  border-top: 2px solid rgba(255, 255, 255, 0.74);
  border-radius: 50%;
  transform: translateX(-50%) rotate(-10deg);
}

.artwork-info {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
}

.artwork-info strong,
.artwork-info span {
  display: block;
}

.artwork-info strong {
  color: #25339b;
  font-size: 14px;
}

.artwork-info span {
  margin-top: 4px;
  color: #727bbb;
  font-size: 12px;
}

.artwork-counts {
  display: flex;
  gap: 8px;
}

.artwork-counts span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.profile-side {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
}

.side-card {
  padding: 18px;
}

.account-card dl {
  display: grid;
  grid-template-columns: 76px 1fr;
  gap: 10px 12px;
  margin: 0;
}

.account-card dt,
.account-card dd {
  min-width: 0;
  margin: 0;
  font-size: 13px;
}

.account-card dt {
  color: #6670b8;
  font-weight: 900;
}

.account-card dd {
  overflow-wrap: anywhere;
  color: #26339a;
}

.storage-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  background:
    linear-gradient(140deg, rgba(96, 116, 255, 0.36), rgba(255, 255, 255, 0.58) 48%, rgba(255, 255, 255, 0.72));
}

.storage-ring {
  display: grid;
  width: 104px;
  height: 104px;
  place-items: center;
  border-radius: 50%;
  background: conic-gradient(#6979ff 0 62%, rgba(255, 255, 255, 0.44) 62% 100%);
  color: #ffffff;
}

.storage-ring svg {
  font-size: 22px;
}

.storage-ring strong,
.storage-ring span {
  line-height: 1;
}

.storage-ring strong {
  color: #ffffff;
  font-size: 21px;
}

.storage-ring span {
  color: rgba(255, 255, 255, 0.88);
  font-size: 11px;
  font-weight: 800;
}

.storage-copy {
  min-width: 0;
  flex: 1;
}

.storage-copy h3 {
  margin: 0;
  color: #27349b;
}

.storage-copy p {
  margin: 6px 0 12px;
  color: #6370b7;
  font-size: 13px;
  font-weight: 800;
}

.profile-progress {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(118, 125, 211, 0.16);
}

.profile-progress i {
  display: block;
  width: 62%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #6379ff, #e48cff);
}

.activity-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.activity-list li {
  display: grid;
  grid-template-columns: 12px 1fr;
  gap: 10px;
  align-items: start;
}

.activity-list i {
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 50%;
  box-shadow: 0 0 0 5px rgba(126, 118, 255, 0.12);
}

.activity-list strong,
.activity-list span {
  display: block;
}

.activity-list strong {
  color: #303c9e;
  font-size: 13px;
}

.activity-list span {
  margin-top: 3px;
  color: #7880be;
  font-size: 12px;
}

.quick-panel {
  display: grid;
  gap: 9px;
}

.quick-panel button {
  width: 100%;
  justify-content: flex-start;
  border: 1px solid rgba(124, 122, 218, 0.16);
  color: #5260b0;
  background: rgba(255, 255, 255, 0.58);
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  background: rgba(24, 30, 94, 0.28);
  backdrop-filter: blur(10px);
}

.profile-modal {
  width: min(460px, calc(100vw - 48px));
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 28px;
  background:
    radial-gradient(circle at 82% 10%, rgba(255, 176, 237, 0.42), transparent 30%),
    linear-gradient(140deg, rgba(255, 255, 255, 0.92), rgba(235, 235, 255, 0.8));
  box-shadow: 0 30px 80px rgba(47, 50, 132, 0.34);
}

.modal-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.modal-title h3 {
  margin: 0;
  color: #29369c;
  font-size: 22px;
}

.modal-title p {
  margin: 6px 0 0;
  color: #6c75b8;
  font-size: 13px;
}

.modal-title button {
  width: 34px;
  height: 34px;
  border: 1px solid rgba(122, 119, 219, 0.18);
  border-radius: 12px;
  color: #6972bb;
  background: rgba(255, 255, 255, 0.68);
  font-size: 22px;
  line-height: 1;
}

.profile-modal label {
  display: grid;
  gap: 7px;
  margin-top: 12px;
  color: #4855a8;
  font-size: 13px;
  font-weight: 800;
}

.profile-modal input,
.profile-modal textarea {
  width: 100%;
  border: 1px solid rgba(124, 122, 218, 0.2);
  border-radius: 16px;
  outline: 0;
  padding: 0 13px;
  color: #25308c;
  background: rgba(255, 255, 255, 0.64);
  font: inherit;
}

.profile-modal input {
  height: 42px;
}

.profile-modal textarea {
  min-height: 92px;
  resize: vertical;
  padding-top: 11px;
}

.modal-error {
  margin: 12px 0 0;
  color: #d34d78;
  font-size: 13px;
  font-weight: 800;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 1440px) {
  .profile-grid {
    grid-template-columns: minmax(0, 1fr) 292px;
  }

  .artwork-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
