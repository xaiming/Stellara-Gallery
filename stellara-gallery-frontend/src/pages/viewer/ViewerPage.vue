<script setup lang="ts">
import {
  DownloadOutlined,
  EyeOutlined,
  HeartOutlined,
  LeftOutlined,
  ShareAltOutlined,
  StarFilled,
  StarOutlined,
} from '@ant-design/icons-vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  downloadPicture as requestDownloadPicture,
  getPictureById,
  listPublicPictures,
  toggleFavorite,
  toggleLike,
  type PictureVO,
} from '../../api/picture'

const route = useRoute()
const router = useRouter()

const activePicture = ref<PictureVO | null>(null)
const stripPictures = ref<PictureVO[]>([])
const loading = ref(false)
const message = ref('')

const activeId = computed(() => Number(route.params.pictureId || activePicture.value?.id || 0))
const currentIndex = computed(() => stripPictures.value.findIndex((item) => item.id === activePicture.value?.id))

const loadStrip = async () => {
  const data = await listPublicPictures({ current: 1, pageSize: 12, sortField: 'createTime', sortOrder: 'descend' })
  stripPictures.value = data.records
}

const loadPicture = async (id?: number) => {
  loading.value = true
  message.value = ''
  try {
    if (!stripPictures.value.length) {
      await loadStrip()
    }
    const targetId = id || stripPictures.value[0]?.id
    if (!targetId) {
      activePicture.value = null
      return
    }
    activePicture.value = await getPictureById(targetId)
  } catch (error) {
    message.value = error instanceof Error ? error.message : '图片加载失败'
  } finally {
    loading.value = false
  }
}

const switchPicture = (delta: number) => {
  if (!stripPictures.value.length) return
  const index = currentIndex.value < 0 ? 0 : currentIndex.value
  const next = stripPictures.value[(index + delta + stripPictures.value.length) % stripPictures.value.length]
  router.push(`/viewer/${next.id}`)
}

const handleLike = async () => {
  if (!activePicture.value) return
  try {
    const liked = await toggleLike(activePicture.value.id)
    activePicture.value.liked = liked
    activePicture.value.likeCount += liked ? 1 : -1
  } catch (error) {
    message.value = error instanceof Error ? error.message : '点赞失败'
  }
}

const handleFavorite = async () => {
  if (!activePicture.value) return
  try {
    const favorited = await toggleFavorite(activePicture.value.id)
    activePicture.value.favorited = favorited
    activePicture.value.favoriteCount += favorited ? 1 : -1
  } catch (error) {
    message.value = error instanceof Error ? error.message : '收藏失败'
  }
}

const handleDownload = async () => {
  if (!activePicture.value) return
  try {
    const url = await requestDownloadPicture(activePicture.value.id)
    window.open(url, '_blank')
  } catch (error) {
    message.value = error instanceof Error ? error.message : '下载失败'
  }
}

const sharePicture = async () => {
  if (!activePicture.value) return
  try {
    await navigator.clipboard.writeText(window.location.href)
    message.value = '链接已复制'
  } catch {
    message.value = '当前浏览器不支持复制链接'
  }
}

watch(
  () => route.params.pictureId,
  (value) => loadPicture(Number(value || 0)),
)

onMounted(() => loadPicture(activeId.value))
</script>

<template>
  <section class="viewer-page">
    <div class="viewer-stage">
      <button class="nav-button left" type="button" @click="switchPicture(-1)"><LeftOutlined /></button>
      <div v-if="loading" class="viewer-empty">星光加载中...</div>
      <div v-else-if="!activePicture" class="viewer-empty">暂无可观赏图片</div>
      <img v-else :src="activePicture.url" :alt="activePicture.name" />
      <button class="nav-button right" type="button" @click="switchPicture(1)"><LeftOutlined /></button>
    </div>

    <aside class="viewer-info">
      <span class="page-kicker">Viewer</span>
      <h2>{{ activePicture?.name || '观赏模式' }}</h2>
      <p>{{ activePicture?.introduction || '沉浸式查看星璃云图中的公开作品。' }}</p>
      <div class="author">
        <img v-if="activePicture?.userAvatar" :src="activePicture.userAvatar" alt="作者头像" />
        <span v-else>{{ activePicture?.userName?.slice(0, 1) || '星' }}</span>
        {{ activePicture?.userName || '星璃用户' }}
      </div>
      <div class="viewer-tags">
        <span>{{ activePicture?.categoryName || '未分类' }}</span>
        <span v-for="tag in activePicture?.tags" :key="tag"># {{ tag }}</span>
      </div>
      <div class="metric-grid">
        <article><EyeOutlined /><span>浏览UV</span><strong>{{ activePicture?.viewCount || 0 }}</strong></article>
        <article><HeartOutlined /><span>点赞</span><strong>{{ Math.max(0, activePicture?.likeCount || 0) }}</strong></article>
        <article><StarFilled /><span>收藏</span><strong>{{ Math.max(0, activePicture?.favoriteCount || 0) }}</strong></article>
      </div>
      <div class="viewer-actions">
        <button :class="{ active: activePicture?.liked }" type="button" @click="handleLike">
          <HeartOutlined />
          {{ activePicture?.liked ? '已点赞' : '点赞' }}
        </button>
        <button :class="{ active: activePicture?.favorited }" type="button" @click="handleFavorite">
          <StarOutlined />
          {{ activePicture?.favorited ? '已收藏' : '收藏' }}
        </button>
        <button type="button" @click="handleDownload"><DownloadOutlined />下载</button>
        <button type="button" @click="sharePicture"><ShareAltOutlined />分享</button>
      </div>
      <p v-if="message" class="page-message">{{ message }}</p>
    </aside>

    <div class="film-strip">
      <button
        v-for="picture in stripPictures"
        :key="picture.id"
        :class="{ active: picture.id === activePicture?.id }"
        type="button"
        @click="router.push(`/viewer/${picture.id}`)"
      >
        <img :src="picture.thumbnailUrl || picture.url" :alt="picture.name" />
      </button>
    </div>
  </section>
</template>

<style scoped>
.viewer-page {
  position: relative;
  display: grid;
  min-height: calc(100vh - 120px);
  grid-template-columns: minmax(0, 1fr) 340px;
  grid-template-rows: minmax(0, 1fr) 98px;
  gap: 18px;
  overflow: hidden;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 34px;
  background:
    radial-gradient(circle at 72% 12%, rgba(255, 178, 244, 0.28), transparent 28%),
    radial-gradient(circle at 20% 84%, rgba(103, 130, 255, 0.32), transparent 28%),
    linear-gradient(135deg, #111f67, #293990 48%, #6858c8);
  box-shadow: var(--shadow);
}

.viewer-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background-image: radial-gradient(circle, rgba(255, 255, 255, 0.9) 0 1px, transparent 2px);
  background-size: 86px 86px;
  opacity: 0.45;
}

.viewer-stage,
.viewer-info,
.film-strip {
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(18px);
}

.viewer-stage {
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 28px;
}

.viewer-stage img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.viewer-empty {
  color: rgba(255, 255, 255, 0.86);
  font-weight: 900;
}

.nav-button {
  position: absolute;
  z-index: 2;
  top: 50%;
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border: 0;
  border-radius: 18px;
  color: #ffffff;
  background: rgba(20, 32, 100, 0.42);
  backdrop-filter: blur(12px);
}

.nav-button.left {
  left: 18px;
}

.nav-button.right {
  right: 18px;
  transform: rotate(180deg);
}

.viewer-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 24px;
  border-radius: 28px;
  color: #ffffff;
}

.viewer-info h2 {
  margin: 8px 0 10px;
  font-size: 28px;
}

.viewer-info p {
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.7;
}

.author {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 10px 0;
  font-weight: 800;
}

.author img,
.author span {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 12px;
  color: #ffffff;
  background: linear-gradient(135deg, #89a0ff, #e08cff);
  object-fit: cover;
}

.viewer-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.viewer-tags span {
  padding: 6px 10px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.16);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 20px 0;
}

.metric-grid article {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
}

.metric-grid svg {
  color: #ffd7ff;
}

.metric-grid span,
.metric-grid strong {
  display: block;
}

.metric-grid span {
  margin: 8px 0 4px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
}

.metric-grid strong {
  font-size: 20px;
}

.viewer-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: auto;
}

.viewer-actions button {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  gap: 7px;
  border: 0;
  border-radius: 16px;
  color: #ffffff;
  font-weight: 900;
  background: rgba(255, 255, 255, 0.14);
}

.viewer-actions button.active,
.viewer-actions button:hover {
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.page-message {
  margin: 12px 0 0;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
}

.film-strip {
  grid-column: 1 / -1;
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 12px;
  border-radius: 24px;
}

.film-strip button {
  flex: 0 0 94px;
  overflow: hidden;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 16px;
  background: transparent;
}

.film-strip button.active {
  border-color: #ffccff;
  box-shadow: 0 0 18px rgba(255, 210, 255, 0.62);
}

.film-strip img {
  width: 100%;
  height: 68px;
  object-fit: cover;
}
</style>
