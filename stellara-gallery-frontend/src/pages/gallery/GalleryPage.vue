<script setup lang="ts">
import {
  CloudUploadOutlined,
  EyeOutlined,
  HeartFilled,
  HeartOutlined,
  PictureOutlined,
  SearchOutlined,
  StarFilled,
} from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listEnabledCategories, type CategoryVO } from '../../api/category'
import {
  listPublicPictures,
  toggleFavorite,
  toggleLike,
  uploadPicture,
  type PictureQueryRequest,
  type PictureVO,
} from '../../api/picture'
import { listEnabledTags, type TagVO } from '../../api/tag'

const router = useRouter()

const pictures = ref<PictureVO[]>([])
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const message = ref('')
const uploadVisible = ref(false)
const previewUrl = ref('')
const MAX_UPLOAD_SIZE = 20 * 1024 * 1024

const query = reactive<PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
  keyword: '',
  categoryId: undefined,
  tag: '',
})

const uploadForm = reactive({
  file: null as File | null,
  name: '',
  introduction: '',
  categoryId: undefined as number | undefined,
  tagsText: '',
  isPublic: 1,
})

const pages = computed(() => Math.max(1, Math.ceil(total.value / (query.pageSize || 12))))
const featuredPicture = computed(() => pictures.value[0])
const statCards = computed(() => [
  { label: '公开作品', value: total.value.toLocaleString(), icon: PictureOutlined },
  { label: '今日浏览', value: pictures.value.reduce((sum, item) => sum + (item.viewCount || 0), 0).toLocaleString(), icon: EyeOutlined },
  { label: '灵感收藏', value: pictures.value.reduce((sum, item) => sum + (item.favoriteCount || 0), 0).toLocaleString(), icon: HeartFilled },
])

const loadFilters = async () => {
  try {
    const [categoryData, tagData] = await Promise.all([listEnabledCategories(), listEnabledTags()])
    categories.value = categoryData
    tags.value = tagData
  } catch (error) {
    message.value = error instanceof Error ? error.message : '筛选数据加载失败'
  }
}

const loadPictures = async () => {
  loading.value = true
  message.value = ''
  try {
    const data = await listPublicPictures(query)
    pictures.value = data.records
    total.value = data.total
  } catch (error) {
    pictures.value = []
    total.value = 0
    message.value = error instanceof Error ? error.message : '图库加载失败'
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  query.current = 1
  loadPictures()
}

const setCategory = (categoryId?: number) => {
  query.categoryId = categoryId
  applyFilter()
}

const setTag = (tagName?: string) => {
  query.tag = tagName || ''
  applyFilter()
}

const changePage = (delta: number) => {
  const nextPage = Math.min(pages.value, Math.max(1, Number(query.current || 1) + delta))
  if (nextPage === query.current) {
    return
  }
  query.current = nextPage
  loadPictures()
}

const onFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  if (file.size > MAX_UPLOAD_SIZE) {
    message.value = '单张图片不能超过 20MB'
    input.value = ''
    uploadForm.file = null
    return
  }
  uploadForm.file = file
  uploadForm.name = uploadForm.name || file.name.replace(/\.[^.]+$/, '')
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = URL.createObjectURL(file)
}

const resetUploadForm = () => {
  uploadForm.file = null
  uploadForm.name = ''
  uploadForm.introduction = ''
  uploadForm.categoryId = undefined
  uploadForm.tagsText = ''
  uploadForm.isPublic = 1
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = ''
}

const submitUpload = async () => {
  if (!uploadForm.file) {
    message.value = '请先选择图片'
    return
  }
  if (!uploadForm.name.trim()) {
    message.value = '请填写图片名称'
    return
  }
  submitting.value = true
  message.value = ''
  try {
    await uploadPicture({
      file: uploadForm.file,
      name: uploadForm.name,
      introduction: uploadForm.introduction,
      categoryId: uploadForm.categoryId,
      tags: uploadForm.tagsText.split(/[,，\s]+/).map((item) => item.trim()).filter(Boolean),
      isPublic: uploadForm.isPublic,
    })
    uploadVisible.value = false
    resetUploadForm()
    await loadPictures()
    message.value = uploadForm.isPublic === 1 ? '上传成功，公开作品已进入审核队列' : '上传成功，已保存到我的空间'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '上传失败'
  } finally {
    submitting.value = false
  }
}

const handleLike = async (picture: PictureVO) => {
  try {
    const liked = await toggleLike(picture.id)
    picture.liked = liked
    picture.likeCount += liked ? 1 : -1
  } catch (error) {
    message.value = error instanceof Error ? error.message : '点赞失败'
  }
}

const handleFavorite = async (picture: PictureVO) => {
  try {
    const favorited = await toggleFavorite(picture.id)
    picture.favorited = favorited
    picture.favoriteCount += favorited ? 1 : -1
  } catch (error) {
    message.value = error instanceof Error ? error.message : '收藏失败'
  }
}

const openViewer = (pictureId: number) => {
  router.push(`/viewer/${pictureId}`)
}

onMounted(async () => {
  await loadFilters()
  await loadPictures()
})
</script>

<template>
  <section class="gallery-page">
    <div class="gallery-hero">
      <div class="hero-copy">
        <span class="page-kicker">Public Gallery</span>
        <h2>星璃公共图库 ✨</h2>
        <p>在星光、云海与创作者灵感之间，发现已审核通过的公开作品。</p>
        <div class="hero-actions">
          <button class="primary-action" type="button" @click="uploadVisible = true">
            <CloudUploadOutlined />
            上传图片
          </button>
          <button class="ghost-action" type="button" @click="setTag('星空')">
            <StarFilled />
            星空灵感
          </button>
        </div>
      </div>

      <div class="hero-preview" v-if="featuredPicture">
        <img :src="featuredPicture.thumbnailUrl || featuredPicture.url" :alt="featuredPicture.name" />
        <div>
          <strong>{{ featuredPicture.name }}</strong>
          <span>{{ featuredPicture.categoryName || '未分类' }}</span>
        </div>
      </div>
    </div>

    <div class="stat-grid">
      <article v-for="card in statCards" :key="card.label" class="stat-card">
        <component :is="card.icon" />
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </article>
    </div>

    <div class="gallery-toolbar glass-panel">
      <label class="toolbar-search">
        <SearchOutlined />
        <input v-model="query.keyword" placeholder="搜索作品名称 / 简介" @keyup.enter="applyFilter" />
      </label>
      <button
        :class="['filter-chip', { active: !query.categoryId }]"
        type="button"
        @click="setCategory(undefined)"
      >
        全部分类
      </button>
      <button
        v-for="category in categories"
        :key="category.id"
        :class="['filter-chip', { active: query.categoryId === category.id }]"
        type="button"
        @click="setCategory(category.id)"
      >
        {{ category.name }}
      </button>
      <button class="toolbar-button" type="button" @click="applyFilter">筛选</button>
    </div>

    <div class="tag-row">
      <button :class="['tag-pill', { active: !query.tag }]" type="button" @click="setTag('')">全部标签</button>
      <button
        v-for="tag in tags.slice(0, 10)"
        :key="tag.id"
        :class="['tag-pill', { active: query.tag === tag.name }]"
        :style="{ '--tag-color': tag.color || '#7b61ff' }"
        type="button"
        @click="setTag(tag.name)"
      >
        # {{ tag.name }}
      </button>
    </div>

    <p v-if="message" class="page-message">{{ message }}</p>

    <div v-if="loading" class="empty-panel glass-panel">星光加载中...</div>
    <div v-else-if="!pictures.length" class="empty-panel glass-panel">暂时没有公开作品，去上传第一张星璃图片吧。</div>
    <div v-else class="picture-grid">
      <article v-for="picture in pictures" :key="picture.id" class="picture-card" @click="openViewer(picture.id)">
        <div class="picture-cover">
          <img :src="picture.thumbnailUrl || picture.url" :alt="picture.name" />
          <span>{{ picture.categoryName || '未分类' }}</span>
        </div>
        <div class="picture-body">
          <h3>{{ picture.name }}</h3>
          <p>{{ picture.introduction || '这张作品还没有简介。' }}</p>
          <div class="picture-tags">
            <span v-for="tag in picture.tags?.slice(0, 3)" :key="tag"># {{ tag }}</span>
          </div>
          <div class="picture-meta">
            <div class="author">
              <img v-if="picture.userAvatar" :src="picture.userAvatar" alt="作者头像" />
              <span v-else>{{ picture.userName?.slice(0, 1) || '星' }}</span>
              {{ picture.userName || '星璃用户' }}
            </div>
            <div class="card-actions" @click.stop>
              <button :class="{ active: picture.liked }" type="button" @click="handleLike(picture)">
                <HeartOutlined />
                {{ Math.max(0, picture.likeCount) }}
              </button>
              <button :class="{ active: picture.favorited }" type="button" @click="handleFavorite(picture)">
                <StarFilled />
                {{ Math.max(0, picture.favoriteCount) }}
              </button>
              <span><EyeOutlined /> {{ picture.viewCount }}</span>
            </div>
          </div>
        </div>
      </article>
    </div>

    <div class="pager">
      <button type="button" :disabled="query.current === 1" @click="changePage(-1)">上一页</button>
      <span>{{ query.current }} / {{ pages }}</span>
      <button type="button" :disabled="query.current === pages" @click="changePage(1)">下一页</button>
    </div>

    <div v-if="uploadVisible" class="modal-mask" @click.self="uploadVisible = false">
      <form class="upload-modal" @submit.prevent="submitUpload">
        <header>
          <div>
            <span class="page-kicker">Upload</span>
            <h3>上传星璃作品</h3>
          </div>
          <button type="button" @click="uploadVisible = false">×</button>
        </header>
        <label class="upload-drop">
          <input type="file" accept="image/*" @change="onFileChange" />
          <img v-if="previewUrl" :src="previewUrl" alt="上传预览" />
          <span v-else><CloudUploadOutlined /> 选择图片</span>
        </label>
        <label>
          作品名称
          <input v-model="uploadForm.name" placeholder="例如：月光下的天空之城" />
        </label>
        <label>
          作品简介
          <textarea v-model="uploadForm.introduction" rows="3" placeholder="写一点关于这张作品的灵感..." />
        </label>
        <div class="form-grid">
          <label>
            分类
            <select v-model.number="uploadForm.categoryId">
              <option :value="undefined">选择分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
            </select>
          </label>
          <label>
            可见性
            <select v-model.number="uploadForm.isPublic">
              <option :value="1">公开并送审</option>
              <option :value="0">私有保存</option>
            </select>
          </label>
        </div>
        <label>
          标签
          <input v-model="uploadForm.tagsText" placeholder="星空, 少女, 壁纸" />
        </label>
        <footer>
          <button class="ghost-action" type="button" @click="uploadVisible = false">取消</button>
          <button class="primary-action" type="submit" :disabled="submitting">
            {{ submitting ? '上传中...' : '提交上传' }}
          </button>
        </footer>
      </form>
    </div>
  </section>
</template>

<style scoped>
.gallery-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.gallery-hero {
  position: relative;
  display: grid;
  min-height: 280px;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 28px;
  overflow: hidden;
  padding: 34px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 34px;
  background:
    radial-gradient(circle at 78% 18%, rgba(255, 177, 244, 0.44), transparent 24%),
    linear-gradient(115deg, rgba(55, 83, 190, 0.9), rgba(126, 105, 238, 0.66) 46%, rgba(255, 171, 232, 0.42)),
    url('../../assets/hero.png') center / cover no-repeat;
  box-shadow: var(--shadow);
  color: #ffffff;
}

.gallery-hero::after {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background-image:
    radial-gradient(circle, rgba(255, 255, 255, 0.9) 0 1px, transparent 2px),
    linear-gradient(135deg, transparent 0 58%, rgba(255, 255, 255, 0.22) 58.2%, transparent 58.6%);
  background-size: 88px 88px, 100% 100%;
  opacity: 0.6;
}

.hero-copy,
.hero-preview {
  position: relative;
  z-index: 1;
}

.hero-copy h2 {
  margin: 6px 0 10px;
  font-size: 42px;
  line-height: 1.1;
}

.hero-copy p {
  max-width: 560px;
  margin: 0;
  color: rgba(245, 247, 255, 0.86);
  line-height: 1.8;
}

.hero-actions,
.gallery-toolbar,
.tag-row,
.pager,
.card-actions,
.form-grid {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-actions {
  margin-top: 24px;
}

.primary-action,
.ghost-action,
.toolbar-button,
.filter-chip,
.tag-pill,
.pager button,
.card-actions button,
.upload-modal header button {
  border: 0;
  color: #ffffff;
}

.primary-action,
.ghost-action {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  gap: 8px;
  padding: 0 18px;
  border-radius: 16px;
  font-weight: 800;
}

.primary-action {
  background: linear-gradient(135deg, #6578ff, #e58bff);
  box-shadow: 0 16px 28px rgba(84, 83, 220, 0.28);
}

.primary-action:disabled {
  cursor: not-allowed;
  opacity: 0.66;
}

.ghost-action {
  border: 1px solid rgba(255, 255, 255, 0.56);
  background: rgba(255, 255, 255, 0.16);
}

.hero-preview {
  align-self: stretch;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.46);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(18px);
}

.hero-preview img {
  width: 100%;
  height: 198px;
  object-fit: cover;
}

.hero-preview div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
}

.hero-preview strong,
.hero-preview span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 3px 12px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.stat-card svg {
  grid-row: span 2;
  width: 44px;
  height: 44px;
  padding: 10px;
  border-radius: 16px;
  color: #ffffff;
  background: linear-gradient(135deg, #6e7bff, #e08dff);
}

.stat-card span {
  color: var(--muted);
  font-size: 13px;
}

.stat-card strong {
  font-size: 24px;
}

.gallery-toolbar {
  flex-wrap: wrap;
  padding: 16px;
  border-radius: 24px;
}

.toolbar-search {
  display: flex;
  min-width: 260px;
  height: 42px;
  flex: 1;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border: 1px solid rgba(122, 118, 220, 0.18);
  border-radius: 16px;
  color: #6970b8;
  background: rgba(255, 255, 255, 0.66);
}

.toolbar-search input,
.upload-modal input,
.upload-modal textarea,
.upload-modal select {
  width: 100%;
  border: 0;
  outline: none;
  color: var(--ink);
  background: transparent;
}

.filter-chip,
.tag-pill,
.pager button {
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid rgba(125, 118, 220, 0.2);
  border-radius: 999px;
  color: #5360ad;
  background: rgba(255, 255, 255, 0.6);
}

.filter-chip.active,
.tag-pill.active,
.toolbar-button {
  color: #ffffff;
  background: linear-gradient(135deg, #6f7cff, #d985ff);
  box-shadow: 0 10px 20px rgba(112, 104, 240, 0.22);
}

.tag-row {
  flex-wrap: wrap;
}

.tag-pill {
  border-color: color-mix(in srgb, var(--tag-color, #7b61ff), transparent 70%);
}

.page-message {
  margin: 0;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  color: #5961b3;
  background: rgba(255, 255, 255, 0.72);
}

.picture-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.picture-card {
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 42px rgba(77, 92, 190, 0.14);
  backdrop-filter: blur(18px);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.picture-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 48px rgba(77, 92, 190, 0.2);
}

.picture-cover {
  position: relative;
  height: 210px;
  overflow: hidden;
}

.picture-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.picture-cover span {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 800;
  background: rgba(55, 64, 170, 0.62);
  backdrop-filter: blur(12px);
}

.picture-body {
  padding: 16px;
}

.picture-body h3,
.picture-body p {
  overflow: hidden;
  text-overflow: ellipsis;
}

.picture-body h3 {
  margin: 0;
  font-size: 17px;
}

.picture-body p {
  display: -webkit-box;
  min-height: 42px;
  margin: 8px 0 12px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  color: var(--muted);
  line-height: 1.6;
}

.picture-tags {
  display: flex;
  min-height: 24px;
  flex-wrap: wrap;
  gap: 6px;
}

.picture-tags span {
  padding: 4px 8px;
  border-radius: 999px;
  color: #6870c4;
  font-size: 12px;
  background: rgba(112, 119, 255, 0.1);
}

.picture-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
}

.author {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  color: #4d58a4;
  font-size: 13px;
  font-weight: 700;
}

.author img,
.author span {
  display: grid;
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  place-items: center;
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(135deg, #89a0ff, #e08cff);
  object-fit: cover;
}

.card-actions {
  gap: 7px;
  color: #6c73b7;
  font-size: 12px;
}

.card-actions button,
.card-actions span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-actions button {
  padding: 0;
  color: inherit;
  background: transparent;
}

.card-actions button.active {
  color: #df70d9;
}

.empty-panel {
  display: grid;
  min-height: 220px;
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

.modal-mask {
  position: fixed;
  z-index: 20;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 32px;
  background: rgba(18, 28, 92, 0.38);
  backdrop-filter: blur(12px);
}

.upload-modal {
  width: min(560px, 100%);
  max-height: calc(100vh - 64px);
  overflow: auto;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 30px;
  background:
    radial-gradient(circle at 88% 12%, rgba(226, 140, 255, 0.24), transparent 28%),
    rgba(255, 255, 255, 0.9);
  box-shadow: 0 30px 80px rgba(45, 58, 150, 0.28);
}

.upload-modal header,
.upload-modal footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.upload-modal h3 {
  margin: 4px 0 0;
  font-size: 24px;
}

.upload-modal header button {
  width: 36px;
  height: 36px;
  border-radius: 14px;
  color: #5360ad;
  background: rgba(115, 122, 255, 0.1);
}

.upload-drop {
  display: grid;
  height: 210px;
  margin: 18px 0;
  place-items: center;
  overflow: hidden;
  border: 1px dashed rgba(122, 115, 220, 0.44);
  border-radius: 24px;
  color: #6a72c1;
  background: rgba(117, 125, 255, 0.08);
}

.upload-drop input {
  display: none;
}

.upload-drop img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-drop span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
}

.upload-modal label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
  color: #4b56a3;
  font-size: 13px;
  font-weight: 800;
}

.upload-modal label input,
.upload-modal label textarea,
.upload-modal label select {
  min-height: 42px;
  padding: 0 13px;
  border: 1px solid rgba(116, 112, 210, 0.18);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.7);
}

.upload-modal label textarea {
  padding-top: 12px;
  resize: vertical;
}

.form-grid {
  align-items: stretch;
}

.form-grid label {
  flex: 1;
}

.upload-modal footer {
  justify-content: flex-end;
  margin-top: 6px;
}

.upload-modal footer .ghost-action {
  color: #5a63b2;
  background: rgba(117, 125, 255, 0.08);
}
</style>
