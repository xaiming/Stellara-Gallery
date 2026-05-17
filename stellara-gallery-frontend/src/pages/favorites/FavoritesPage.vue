<script setup lang="ts">
import { EyeOutlined, HeartFilled, SearchOutlined, StarFilled } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  listFavoritePictures,
  toggleFavorite,
  type PictureQueryRequest,
  type PictureVO,
} from '../../api/picture'

const router = useRouter()
const pictures = ref<PictureVO[]>([])
const total = ref(0)
const loading = ref(false)
const message = ref('')

const query = reactive<PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  keyword: '',
})

const pages = computed(() => Math.max(1, Math.ceil(total.value / (query.pageSize || 12))))
const totalViews = computed(() => pictures.value.reduce((sum, item) => sum + (item.viewCount || 0), 0))

const loadFavorites = async () => {
  loading.value = true
  message.value = ''
  try {
    const data = await listFavoritePictures(query)
    pictures.value = data.records
    total.value = data.total
  } catch (error) {
    message.value = error instanceof Error ? error.message : '收藏夹加载失败'
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  query.current = 1
  loadFavorites()
}

const changePage = (delta: number) => {
  const nextPage = Math.min(pages.value, Math.max(1, Number(query.current || 1) + delta))
  if (nextPage === query.current) return
  query.current = nextPage
  loadFavorites()
}

const removeFavorite = async (picture: PictureVO) => {
  try {
    await toggleFavorite(picture.id)
    await loadFavorites()
    message.value = '已取消收藏'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '取消收藏失败'
  }
}

onMounted(loadFavorites)
</script>

<template>
  <section class="favorites-page">
    <div class="favorites-hero">
      <span class="page-kicker">Favorites</span>
      <h2>我的收藏夹 ✨</h2>
      <p>收藏的灵感、壁纸和天空之城，都在这里安静发光。</p>
    </div>

    <div class="favorite-stats">
      <article>
        <HeartFilled />
        <span>收藏总数</span>
        <strong>{{ total }}</strong>
      </article>
      <article>
        <EyeOutlined />
        <span>累计浏览UV</span>
        <strong>{{ totalViews }}</strong>
      </article>
      <article>
        <StarFilled />
        <span>当前页作品</span>
        <strong>{{ pictures.length }}</strong>
      </article>
    </div>

    <div class="favorite-toolbar glass-panel">
      <label>
        <SearchOutlined />
        <input v-model="query.keyword" placeholder="搜索收藏作品..." @keyup.enter="applyFilter" />
      </label>
      <button type="button" @click="applyFilter">搜索</button>
    </div>

    <p v-if="message" class="page-message">{{ message }}</p>

    <div v-if="loading" class="empty-panel glass-panel">正在打开收藏星匣...</div>
    <div v-else-if="!pictures.length" class="empty-panel glass-panel">还没有收藏图片，去公共图库点亮星星吧。</div>
    <div v-else class="favorite-grid">
      <article v-for="picture in pictures" :key="picture.id" class="favorite-card">
        <div class="cover" @click="router.push(`/viewer/${picture.id}`)">
          <img :src="picture.thumbnailUrl || picture.url" :alt="picture.name" />
          <button type="button" @click.stop="removeFavorite(picture)"><HeartFilled /></button>
        </div>
        <div class="body">
          <h3>{{ picture.name }}</h3>
          <p>{{ picture.introduction || '暂无简介' }}</p>
          <div class="meta">
            <span>{{ picture.categoryName || '未分类' }}</span>
            <span><EyeOutlined /> {{ picture.viewCount }}</span>
          </div>
        </div>
      </article>
    </div>

    <div class="pager">
      <button type="button" :disabled="query.current === 1" @click="changePage(-1)">上一页</button>
      <span>{{ query.current }} / {{ pages }}</span>
      <button type="button" :disabled="query.current === pages" @click="changePage(1)">下一页</button>
    </div>
  </section>
</template>

<style scoped>
.favorites-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.favorites-hero,
.favorite-stats article,
.favorite-card {
  border: 1px solid rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.favorites-hero {
  min-height: 220px;
  padding: 32px;
  border-radius: 34px;
  color: #ffffff;
  background:
    radial-gradient(circle at 76% 18%, rgba(255, 179, 244, 0.44), transparent 28%),
    linear-gradient(115deg, rgba(41, 70, 174, 0.92), rgba(128, 99, 235, 0.66)),
    url('../../assets/hero.png') center / cover no-repeat;
}

.favorites-hero h2 {
  margin: 8px 0 10px;
  font-size: 40px;
}

.favorites-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.84);
}

.favorite-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.favorite-stats article {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 3px 12px;
  padding: 18px;
  border-radius: 24px;
}

.favorite-stats svg {
  grid-row: span 2;
  width: 44px;
  height: 44px;
  padding: 10px;
  border-radius: 16px;
  color: #ffffff;
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.favorite-stats span {
  color: var(--muted);
  font-size: 13px;
}

.favorite-stats strong {
  font-size: 24px;
}

.favorite-toolbar {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 24px;
}

.favorite-toolbar label {
  display: flex;
  height: 42px;
  flex: 1;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border: 1px solid rgba(122, 118, 220, 0.18);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.66);
}

.favorite-toolbar input {
  width: 100%;
  border: 0;
  outline: none;
  color: var(--ink);
  background: transparent;
}

.favorite-toolbar button,
.pager button {
  min-height: 38px;
  padding: 0 16px;
  border: 0;
  border-radius: 14px;
  color: #ffffff;
  font-weight: 800;
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.page-message {
  margin: 0;
  padding: 12px 16px;
  border-radius: 18px;
  color: #5961b3;
  background: rgba(255, 255, 255, 0.72);
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.favorite-card {
  overflow: hidden;
  border-radius: 26px;
}

.cover {
  position: relative;
  height: 210px;
  cursor: pointer;
  overflow: hidden;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover button {
  position: absolute;
  top: 12px;
  right: 12px;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 0;
  border-radius: 14px;
  color: #ffffff;
  background: rgba(229, 92, 168, 0.84);
}

.body {
  padding: 16px;
}

.body h3 {
  margin: 0;
  font-size: 17px;
}

.body p {
  display: -webkit-box;
  min-height: 42px;
  margin: 8px 0 12px;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  color: var(--muted);
  line-height: 1.6;
}

.meta,
.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.meta {
  color: #6d73b6;
  font-size: 13px;
  font-weight: 800;
}

.empty-panel {
  display: grid;
  min-height: 260px;
  place-items: center;
  border-radius: 28px;
  color: #6670bd;
  font-weight: 800;
}

.pager {
  justify-content: center;
}

.pager button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pager span {
  color: #5962b3;
  font-weight: 800;
}
</style>
