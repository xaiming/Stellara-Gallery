<script setup lang="ts">
import {
  CloudUploadOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  LockOutlined,
  PictureOutlined,
  SearchOutlined,
  StarFilled,
} from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listEnabledCategories, type CategoryVO } from '../../api/category'
import {
  deletePicture,
  listMyPictures,
  uploadPicture,
  type PictureQueryRequest,
  type PictureVO,
} from '../../api/picture'
import { getMySpace, type SpaceVO } from '../../api/space'
import { listEnabledTags, type TagVO } from '../../api/tag'

const router = useRouter()

const space = ref<SpaceVO | null>(null)
const pictures = ref<PictureVO[]>([])
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const total = ref(0)
const loading = ref(false)
const message = ref('')
const uploadVisible = ref(false)
const submitting = ref(false)
const previewUrl = ref('')

const query = reactive<PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  keyword: '',
  isPublic: undefined,
  reviewStatus: undefined,
})

const uploadForm = reactive({
  file: null as File | null,
  name: '',
  introduction: '',
  categoryId: undefined as number | undefined,
  tagsText: '',
  isPublic: 0,
})

const pages = computed(() => Math.max(1, Math.ceil(total.value / (query.pageSize || 12))))
const usagePercent = computed(() => {
  if (!space.value?.maxSize) {
    return 0
  }
  return Math.min(100, Math.round((space.value.totalSize / space.value.maxSize) * 100))
})
const publicCount = computed(() => pictures.value.filter((item) => item.isPublic === 1).length)
const privateCount = computed(() => pictures.value.filter((item) => item.isPublic === 0).length)
const reviewingCount = computed(() => pictures.value.filter((item) => item.reviewStatus === 0).length)

const formatSize = (size?: number) => {
  if (!size) {
    return '0 MB'
  }
  if (size >= 1024 * 1024 * 1024) {
    return `${(size / 1024 / 1024 / 1024).toFixed(2)} GB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const reviewText = (picture: PictureVO) => {
  if (picture.isPublic === 0) {
    return '私有'
  }
  if (picture.reviewStatus === 0) {
    return '审核中'
  }
  if (picture.reviewStatus === 2) {
    return '已拒绝'
  }
  return '公开'
}

const loadFilters = async () => {
  const [categoryData, tagData] = await Promise.all([listEnabledCategories(), listEnabledTags()])
  categories.value = categoryData
  tags.value = tagData
}

const loadSpace = async () => {
  space.value = await getMySpace()
}

const loadPictures = async () => {
  loading.value = true
  message.value = ''
  try {
    const data = await listMyPictures(query)
    pictures.value = data.records
    total.value = data.total
  } catch (error) {
    message.value = error instanceof Error ? error.message : '我的空间加载失败'
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  query.current = 1
  loadPictures()
}

const setScope = (scope: 'all' | 'public' | 'private' | 'reviewing') => {
  query.isPublic = undefined
  query.reviewStatus = undefined
  if (scope === 'public') {
    query.isPublic = 1
    query.reviewStatus = 1
  }
  if (scope === 'private') {
    query.isPublic = 0
  }
  if (scope === 'reviewing') {
    query.isPublic = 1
    query.reviewStatus = 0
  }
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
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) {
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
  uploadForm.isPublic = 0
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = ''
}

const submitUpload = async () => {
  if (!uploadForm.file || !uploadForm.name.trim()) {
    message.value = '请选择图片并填写名称'
    return
  }
  submitting.value = true
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
    await Promise.all([loadSpace(), loadPictures()])
    message.value = '图片已保存到我的空间'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '上传失败'
  } finally {
    submitting.value = false
  }
}

const removePicture = async (picture: PictureVO) => {
  if (!window.confirm(`确认删除《${picture.name}》吗？`)) {
    return
  }
  try {
    await deletePicture(picture.id)
    await Promise.all([loadSpace(), loadPictures()])
    message.value = '图片已删除'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '删除失败'
  }
}

const openViewer = (pictureId: number) => {
  router.push(`/viewer/${pictureId}`)
}

onMounted(async () => {
  try {
    await Promise.all([loadFilters(), loadSpace(), loadPictures()])
  } catch (error) {
    message.value = error instanceof Error ? error.message : '我的空间初始化失败'
  }
})
</script>

<template>
  <section class="my-space-page">
    <div class="space-hero">
      <div>
        <span class="page-kicker">My Space</span>
        <h2>我的空间 ✨</h2>
        <p>管理你的私有灵感、公开作品与审核中的星璃图片。</p>
      </div>
      <button class="primary-action" type="button" @click="uploadVisible = true">
        <CloudUploadOutlined />
        上传图片
      </button>
    </div>

    <div class="space-overview">
      <article class="usage-card">
        <div class="ring" :style="{ '--usage': `${usagePercent * 3.6}deg` }">
          <strong>{{ usagePercent }}%</strong>
          <span>已使用</span>
        </div>
        <div>
          <span>存储空间使用</span>
          <strong>{{ formatSize(space?.totalSize) }} / {{ formatSize(space?.maxSize) }}</strong>
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: `${usagePercent}%` }" />
          </div>
        </div>
      </article>
      <article class="mini-stat">
        <PictureOutlined />
        <span>图片总数</span>
        <strong>{{ space?.totalCount || total }}</strong>
      </article>
      <article class="mini-stat">
        <EyeOutlined />
        <span>公开作品</span>
        <strong>{{ publicCount }}</strong>
      </article>
      <article class="mini-stat">
        <LockOutlined />
        <span>私有图片</span>
        <strong>{{ privateCount }}</strong>
      </article>
      <article class="mini-stat">
        <StarFilled />
        <span>审核中</span>
        <strong>{{ reviewingCount }}</strong>
      </article>
    </div>

    <div class="space-toolbar glass-panel">
      <button class="primary-action" type="button" @click="uploadVisible = true">
        <CloudUploadOutlined />
        上传图片
      </button>
      <label class="toolbar-search">
        <SearchOutlined />
        <input v-model="query.keyword" placeholder="搜索我的图片..." @keyup.enter="applyFilter" />
      </label>
      <button class="filter-chip" type="button" @click="setScope('all')">全部</button>
      <button class="filter-chip" type="button" @click="setScope('public')">公开</button>
      <button class="filter-chip" type="button" @click="setScope('private')">私有</button>
      <button class="filter-chip" type="button" @click="setScope('reviewing')">审核中</button>
    </div>

    <p v-if="message" class="page-message">{{ message }}</p>

    <div class="space-layout">
      <main>
        <div v-if="loading" class="empty-panel glass-panel">正在整理你的星璃空间...</div>
        <div v-else-if="!pictures.length" class="empty-panel glass-panel">还没有图片，先上传一张吧。</div>
        <div v-else class="my-picture-grid">
          <article v-for="picture in pictures" :key="picture.id" class="my-picture-card">
            <div class="cover" @click="openViewer(picture.id)">
              <img :src="picture.thumbnailUrl || picture.url" :alt="picture.name" />
              <span :class="['status', `status-${picture.reviewStatus}`]">{{ reviewText(picture) }}</span>
              <div class="quick-actions">
                <button type="button" title="查看" @click.stop="openViewer(picture.id)"><EyeOutlined /></button>
                <button type="button" title="编辑"><EditOutlined /></button>
                <button type="button" title="删除" @click.stop="removePicture(picture)"><DeleteOutlined /></button>
              </div>
            </div>
            <div class="body">
              <h3>{{ picture.name }}</h3>
              <p>{{ picture.introduction || '暂无简介' }}</p>
              <div class="tags">
                <span v-for="tag in picture.tags?.slice(0, 3)" :key="tag"># {{ tag }}</span>
              </div>
              <footer>
                <span>{{ picture.categoryName || '未分类' }}</span>
                <span>{{ picture.viewCount }} UV</span>
              </footer>
            </div>
          </article>
        </div>

        <div class="pager">
          <button type="button" :disabled="query.current === 1" @click="changePage(-1)">上一页</button>
          <span>{{ query.current }} / {{ pages }}</span>
          <button type="button" :disabled="query.current === pages" @click="changePage(1)">下一页</button>
        </div>
      </main>

      <aside class="space-side">
        <section class="side-panel">
          <h3>空间详情</h3>
          <p>等级：Lv.{{ (space?.spaceLevel || 0) + 1 }} 星璃空间</p>
          <p>图片额度：{{ space?.totalCount || 0 }} / {{ space?.maxCount || 0 }}</p>
          <p>状态：{{ space?.spaceStatus === 1 ? '禁用' : '正常' }}</p>
        </section>
        <section class="side-panel">
          <h3>常用标签</h3>
          <div class="side-tags">
            <span v-for="tag in tags.slice(0, 8)" :key="tag.id" :style="{ background: tag.color || '#7b61ff' }">
              {{ tag.name }}
            </span>
          </div>
        </section>
      </aside>
    </div>

    <div v-if="uploadVisible" class="modal-mask" @click.self="uploadVisible = false">
      <form class="upload-modal" @submit.prevent="submitUpload">
        <header>
          <div>
            <span class="page-kicker">Upload</span>
            <h3>保存到我的空间</h3>
          </div>
          <button type="button" @click="uploadVisible = false">×</button>
        </header>
        <label class="upload-drop">
          <input type="file" accept="image/*" @change="onFileChange" />
          <img v-if="previewUrl" :src="previewUrl" alt="上传预览" />
          <span v-else><CloudUploadOutlined /> 选择图片</span>
        </label>
        <label>
          图片名称
          <input v-model="uploadForm.name" placeholder="例如：星轨列车" />
        </label>
        <label>
          图片简介
          <textarea v-model="uploadForm.introduction" rows="3" placeholder="记录一点灵感..." />
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
              <option :value="0">私有保存</option>
              <option :value="1">公开并送审</option>
            </select>
          </label>
        </div>
        <label>
          标签
          <input v-model="uploadForm.tagsText" placeholder="星空, 梦幻, 壁纸" />
        </label>
        <footer>
          <button class="ghost-action" type="button" @click="uploadVisible = false">取消</button>
          <button class="primary-action" type="submit" :disabled="submitting">
            {{ submitting ? '上传中...' : '保存图片' }}
          </button>
        </footer>
      </form>
    </div>
  </section>
</template>

<style scoped>
.my-space-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.space-hero {
  display: flex;
  min-height: 220px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  overflow: hidden;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 34px;
  color: #ffffff;
  background:
    radial-gradient(circle at 70% 22%, rgba(255, 176, 244, 0.48), transparent 26%),
    linear-gradient(115deg, rgba(38, 65, 169, 0.92), rgba(126, 103, 235, 0.68)),
    url('../../assets/hero.png') center / cover no-repeat;
  box-shadow: var(--shadow);
}

.space-hero h2 {
  margin: 8px 0 10px;
  font-size: 40px;
}

.space-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
}

.primary-action,
.ghost-action,
.filter-chip,
.pager button,
.quick-actions button,
.upload-modal header button {
  border: 0;
}

.primary-action,
.ghost-action {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  gap: 8px;
  padding: 0 18px;
  border-radius: 16px;
  color: #ffffff;
  font-weight: 800;
}

.primary-action {
  background: linear-gradient(135deg, #6578ff, #e58bff);
  box-shadow: 0 16px 28px rgba(84, 83, 220, 0.26);
}

.primary-action:disabled {
  cursor: not-allowed;
  opacity: 0.66;
}

.ghost-action {
  color: #5961ad;
  background: rgba(118, 125, 255, 0.1);
}

.space-overview {
  display: grid;
  grid-template-columns: 1.6fr repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.usage-card,
.mini-stat,
.side-panel,
.my-picture-card,
.space-toolbar,
.upload-modal {
  border: 1px solid rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.usage-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px;
  border-radius: 24px;
}

.ring {
  display: grid;
  width: 94px;
  height: 94px;
  flex: 0 0 94px;
  place-items: center;
  border-radius: 50%;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.94) 0 56%, transparent 57%),
    conic-gradient(#6b78ff var(--usage), rgba(124, 132, 240, 0.16) 0);
}

.ring strong,
.ring span {
  grid-area: 1 / 1;
}

.ring strong {
  margin-top: -10px;
  font-size: 22px;
}

.ring span {
  margin-top: 32px;
  color: var(--muted);
  font-size: 12px;
}

.usage-card > div:last-child {
  flex: 1;
}

.usage-card > div:last-child span,
.mini-stat span {
  color: var(--muted);
  font-size: 13px;
}

.usage-card > div:last-child strong,
.mini-stat strong {
  display: block;
  margin-top: 6px;
  font-size: 24px;
}

.progress-track {
  height: 8px;
  margin-top: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(113, 119, 220, 0.16);
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #6877ff, #df8cff);
}

.mini-stat {
  display: flex;
  min-height: 118px;
  flex-direction: column;
  justify-content: center;
  padding: 18px;
  border-radius: 24px;
}

.mini-stat svg {
  width: 24px;
  height: 24px;
  margin-bottom: 10px;
  color: #7581ff;
}

.space-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
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
.pager button {
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid rgba(125, 118, 220, 0.2);
  border-radius: 999px;
  color: #5360ad;
  background: rgba(255, 255, 255, 0.62);
}

.page-message {
  margin: 0;
  padding: 12px 16px;
  border-radius: 18px;
  color: #5961b3;
  background: rgba(255, 255, 255, 0.72);
}

.space-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
}

.my-picture-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.my-picture-card {
  overflow: hidden;
  border-radius: 24px;
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

.status {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 800;
  background: rgba(54, 185, 132, 0.82);
}

.status-0 {
  background: rgba(244, 169, 62, 0.88);
}

.status-2 {
  background: rgba(225, 90, 126, 0.86);
}

.quick-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 6px;
}

.quick-actions button {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 12px;
  color: #ffffff;
  background: rgba(35, 48, 144, 0.44);
  backdrop-filter: blur(12px);
}

.body {
  padding: 15px;
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

.tags,
.side-tags,
.pager,
.form-grid {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tags {
  min-height: 24px;
  flex-wrap: wrap;
}

.tags span {
  padding: 4px 8px;
  border-radius: 999px;
  color: #6870c4;
  font-size: 12px;
  background: rgba(112, 119, 255, 0.1);
}

.body footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14px;
  color: #6d73b6;
  font-size: 12px;
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
  margin-top: 18px;
}

.pager button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.space-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-panel {
  padding: 20px;
  border-radius: 24px;
}

.side-panel h3 {
  margin: 0 0 12px;
}

.side-panel p {
  margin: 10px 0;
  color: var(--muted);
}

.side-tags {
  flex-wrap: wrap;
}

.side-tags span {
  padding: 6px 9px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 12px;
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
  border-radius: 30px;
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

.form-grid label {
  flex: 1;
}

.upload-modal footer {
  justify-content: flex-end;
}
</style>
