<script setup lang="ts">
import {
  CheckCircleOutlined,
  DeleteOutlined,
  EyeOutlined,
  FileSearchOutlined,
  ReloadOutlined,
  SearchOutlined,
  StopOutlined,
} from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listCategories, type CategoryVO } from '../../api/category'
import {
  batchReviewPicture,
  deletePicture,
  listAdminPictures,
  reviewPicture,
  type PictureQueryRequest,
  type PictureVO,
} from '../../api/picture'

const router = useRouter()

const pictures = ref<PictureVO[]>([])
const categories = ref<CategoryVO[]>([])
const total = ref(0)
const loading = ref(false)
const message = ref('')
const selectedIds = ref<number[]>([])

const query = reactive<PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined,
  isPublic: undefined,
  reviewStatus: undefined,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const pages = computed(() => Math.max(1, Math.ceil(total.value / (query.pageSize || 10))))
const allSelected = computed(() => pictures.value.length > 0 && pictures.value.every((item) => selectedIds.value.includes(item.id)))
const stats = computed(() => [
  { label: '待审核', value: pictures.value.filter((item) => item.reviewStatus === 0).length, tone: 'warning' },
  { label: '已通过', value: pictures.value.filter((item) => item.reviewStatus === 1).length, tone: 'success' },
  { label: '已拒绝', value: pictures.value.filter((item) => item.reviewStatus === 2).length, tone: 'danger' },
  { label: '公开作品', value: pictures.value.filter((item) => item.isPublic === 1).length, tone: 'primary' },
])

const reviewLabel = (status: number) => {
  if (status === 0) return '待审核'
  if (status === 2) return '已拒绝'
  return '已通过'
}

const visibilityLabel = (isPublic: number) => (isPublic === 1 ? '公开' : '私有')

const loadCategories = async () => {
  categories.value = await listCategories()
}

const loadPictures = async () => {
  loading.value = true
  message.value = ''
  try {
    const data = await listAdminPictures(query)
    pictures.value = data.records
    total.value = data.total
    selectedIds.value = selectedIds.value.filter((id) => pictures.value.some((item) => item.id === id))
  } catch (error) {
    message.value = error instanceof Error ? error.message : '图片列表加载失败'
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  query.current = 1
  loadPictures()
}

const resetFilter = () => {
  query.keyword = ''
  query.categoryId = undefined
  query.isPublic = undefined
  query.reviewStatus = undefined
  applyFilter()
}

const changePage = (delta: number) => {
  const nextPage = Math.min(pages.value, Math.max(1, Number(query.current || 1) + delta))
  if (nextPage === query.current) return
  query.current = nextPage
  loadPictures()
}

const toggleSelect = (id: number) => {
  selectedIds.value = selectedIds.value.includes(id)
    ? selectedIds.value.filter((item) => item !== id)
    : [...selectedIds.value, id]
}

const toggleAll = () => {
  selectedIds.value = allSelected.value ? [] : pictures.value.map((item) => item.id)
}

const handleReview = async (picture: PictureVO, status: number) => {
  const reviewMessage = status === 2 ? window.prompt('请输入拒绝原因', picture.reviewMessage || '图片暂不符合公开图库规范') : '审核通过'
  if (status === 2 && reviewMessage === null) {
    return
  }
  try {
    await reviewPicture({ id: picture.id, reviewStatus: status, reviewMessage: reviewMessage || undefined })
    await loadPictures()
    message.value = status === 1 ? '已通过审核' : '已拒绝图片'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '审核失败'
  }
}

const handleBatchReview = async (status: number) => {
  if (!selectedIds.value.length) {
    message.value = '请先选择图片'
    return
  }
  const reviewMessage = status === 2 ? window.prompt('请输入批量拒绝原因', '图片暂不符合公开图库规范') : '批量审核通过'
  if (status === 2 && reviewMessage === null) {
    return
  }
  try {
    await batchReviewPicture(selectedIds.value, status, reviewMessage || undefined)
    selectedIds.value = []
    await loadPictures()
    message.value = status === 1 ? '已批量通过' : '已批量拒绝'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '批量审核失败'
  }
}

const removePicture = async (picture: PictureVO) => {
  if (!window.confirm(`确认删除《${picture.name}》吗？`)) {
    return
  }
  try {
    await deletePicture(picture.id)
    await loadPictures()
    message.value = '图片已删除'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '删除失败'
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadPictures()])
})
</script>

<template>
  <section class="picture-admin-page">
    <div class="review-hero">
      <div>
        <span class="page-kicker">Review Center</span>
        <h2>图片审核与管理 ✨</h2>
        <p>审核用户上传的公开作品，维护星璃云图内容质量。</p>
      </div>
    </div>

    <div class="review-stats">
      <article v-for="item in stats" :key="item.label" :class="['review-stat', item.tone]">
        <FileSearchOutlined />
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </div>

    <div class="admin-layout">
      <main class="review-table-shell glass-panel">
        <div class="review-toolbar">
          <label class="toolbar-search">
            <SearchOutlined />
            <input v-model="query.keyword" placeholder="搜索图片名称 / 简介" @keyup.enter="applyFilter" />
          </label>
          <select v-model.number="query.isPublic">
            <option :value="undefined">公开状态</option>
            <option :value="1">公开</option>
            <option :value="0">私有</option>
          </select>
          <select v-model.number="query.reviewStatus">
            <option :value="undefined">审核状态</option>
            <option :value="0">待审核</option>
            <option :value="1">已通过</option>
            <option :value="2">已拒绝</option>
          </select>
          <select v-model.number="query.categoryId">
            <option :value="undefined">全部分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
          </select>
          <button class="toolbar-button" type="button" @click="applyFilter">筛选</button>
          <button class="ghost-button" type="button" @click="resetFilter"><ReloadOutlined /> 重置</button>
        </div>

        <div class="batch-bar">
          <span>已选 {{ selectedIds.length }} 项</span>
          <button type="button" @click="handleBatchReview(1)"><CheckCircleOutlined /> 批量通过</button>
          <button type="button" class="danger" @click="handleBatchReview(2)"><StopOutlined /> 批量拒绝</button>
        </div>

        <p v-if="message" class="page-message">{{ message }}</p>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th><input type="checkbox" :checked="allSelected" @change="toggleAll" /></th>
                <th>图片</th>
                <th>名称 / 作者</th>
                <th>分类 / 标签</th>
                <th>公开状态</th>
                <th>审核状态</th>
                <th>浏览 / 点赞 / 收藏</th>
                <th>上传时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="9" class="empty-cell">审核星轨加载中...</td>
              </tr>
              <tr v-else-if="!pictures.length">
                <td colspan="9" class="empty-cell">暂无图片数据</td>
              </tr>
              <tr v-for="picture in pictures" v-else :key="picture.id">
                <td><input type="checkbox" :checked="selectedIds.includes(picture.id)" @change="toggleSelect(picture.id)" /></td>
                <td>
                  <img class="thumb" :src="picture.thumbnailUrl || picture.url" :alt="picture.name" />
                </td>
                <td>
                  <strong>{{ picture.name }}</strong>
                  <span>ID: {{ picture.id }} / {{ picture.userName || '星璃用户' }}</span>
                </td>
                <td>
                  <span class="soft-pill">{{ picture.categoryName || '未分类' }}</span>
                  <div class="tag-line">
                    <em v-for="tag in picture.tags?.slice(0, 2)" :key="tag">#{{ tag }}</em>
                  </div>
                </td>
                <td><span :class="['state-pill', picture.isPublic === 1 ? 'pass' : 'muted']">{{ visibilityLabel(picture.isPublic) }}</span></td>
                <td><span :class="['state-pill', `review-${picture.reviewStatus}`]">{{ reviewLabel(picture.reviewStatus) }}</span></td>
                <td>{{ picture.viewCount }} / {{ picture.likeCount }} / {{ picture.favoriteCount }}</td>
                <td>{{ picture.createTime?.slice(0, 16).replace('T', ' ') || '-' }}</td>
                <td>
                  <div class="row-actions">
                    <button type="button" title="查看" @click="router.push(`/viewer/${picture.id}`)"><EyeOutlined /></button>
                    <button type="button" title="通过" @click="handleReview(picture, 1)"><CheckCircleOutlined /></button>
                    <button type="button" title="拒绝" class="danger" @click="handleReview(picture, 2)"><StopOutlined /></button>
                    <button type="button" title="删除" class="danger" @click="removePicture(picture)"><DeleteOutlined /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pager">
          <button type="button" :disabled="query.current === 1" @click="changePage(-1)">上一页</button>
          <span>{{ query.current }} / {{ pages }}</span>
          <button type="button" :disabled="query.current === pages" @click="changePage(1)">下一页</button>
        </div>
      </main>

      <aside class="review-side">
        <section class="side-card">
          <h3>审核规则 ✨</h3>
          <p>禁止违规内容、侵权内容和明显低质图片。</p>
          <p>通过后图片会出现在公共图库，拒绝后仅上传者可见。</p>
        </section>
        <section class="side-card">
          <h3>快捷筛选</h3>
          <button type="button" @click="query.reviewStatus = 0; applyFilter()">只看待审核</button>
          <button type="button" @click="query.isPublic = 1; applyFilter()">公开作品</button>
          <button type="button" @click="query.reviewStatus = 2; applyFilter()">已拒绝</button>
        </section>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.picture-admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.review-hero {
  min-height: 220px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 34px;
  color: #ffffff;
  background:
    radial-gradient(circle at 76% 16%, rgba(255, 186, 244, 0.42), transparent 28%),
    linear-gradient(115deg, rgba(41, 70, 174, 0.92), rgba(123, 102, 236, 0.7)),
    url('../../assets/hero.png') center / cover no-repeat;
  box-shadow: var(--shadow);
}

.review-hero h2 {
  margin: 8px 0 10px;
  font-size: 40px;
}

.review-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.84);
}

.review-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.review-stat,
.review-table-shell,
.side-card {
  border: 1px solid rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.review-stat {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 3px 12px;
  padding: 18px;
  border-radius: 24px;
}

.review-stat svg {
  grid-row: span 2;
  width: 44px;
  height: 44px;
  padding: 10px;
  border-radius: 16px;
  color: #ffffff;
}

.review-stat.warning svg {
  background: linear-gradient(135deg, #f3aa45, #ffd17d);
}

.review-stat.success svg {
  background: linear-gradient(135deg, #49c59d, #8ce6d0);
}

.review-stat.danger svg {
  background: linear-gradient(135deg, #ef7192, #ffb0d3);
}

.review-stat.primary svg {
  background: linear-gradient(135deg, #6578ff, #d989ff);
}

.review-stat span {
  color: var(--muted);
  font-size: 13px;
}

.review-stat strong {
  font-size: 24px;
}

.admin-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
}

.review-table-shell {
  overflow: hidden;
  padding: 16px;
  border-radius: 28px;
}

.review-toolbar,
.batch-bar,
.pager,
.row-actions,
.tag-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-toolbar {
  flex-wrap: wrap;
}

.toolbar-search {
  display: flex;
  min-width: 250px;
  height: 40px;
  flex: 1;
  align-items: center;
  gap: 9px;
  padding: 0 13px;
  border: 1px solid rgba(122, 118, 220, 0.18);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.66);
}

.toolbar-search input,
.review-toolbar select {
  border: 0;
  outline: none;
  color: var(--ink);
  background: transparent;
}

.toolbar-search input {
  width: 100%;
}

.review-toolbar select {
  height: 40px;
  padding: 0 12px;
  border: 1px solid rgba(122, 118, 220, 0.18);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.66);
}

.toolbar-button,
.ghost-button,
.batch-bar button,
.pager button,
.row-actions button,
.side-card button {
  min-height: 36px;
  border: 0;
  border-radius: 14px;
}

.toolbar-button {
  padding: 0 16px;
  color: #ffffff;
  font-weight: 800;
  background: linear-gradient(135deg, #6678ff, #df8cff);
}

.ghost-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 14px;
  color: #5f68b5;
  background: rgba(113, 120, 255, 0.1);
}

.batch-bar {
  justify-content: flex-end;
  margin: 14px 0;
}

.batch-bar span {
  margin-right: auto;
  color: var(--muted);
  font-weight: 800;
}

.batch-bar button,
.side-card button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  color: #21966f;
  background: rgba(75, 197, 157, 0.12);
}

.batch-bar button.danger,
.row-actions button.danger {
  color: #d95477;
  background: rgba(238, 113, 146, 0.12);
}

.page-message {
  margin: 0 0 12px;
  padding: 10px 14px;
  border-radius: 16px;
  color: #5961b3;
  background: rgba(112, 119, 255, 0.1);
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 1050px;
  border-collapse: collapse;
}

th,
td {
  padding: 12px 10px;
  border-bottom: 1px solid rgba(119, 118, 213, 0.14);
  color: #5360ad;
  font-size: 13px;
  text-align: left;
}

th {
  color: #35449c;
  font-weight: 900;
  background: rgba(113, 120, 255, 0.08);
}

td strong,
td span {
  display: block;
}

td strong {
  max-width: 170px;
  overflow: hidden;
  color: #26317a;
  text-overflow: ellipsis;
  white-space: nowrap;
}

td span {
  margin-top: 4px;
  color: var(--muted);
}

.thumb {
  width: 84px;
  height: 54px;
  border-radius: 12px;
  object-fit: cover;
}

.soft-pill,
.state-pill {
  display: inline-flex;
  width: fit-content;
  padding: 5px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.soft-pill {
  color: #6b61c9;
  background: rgba(123, 97, 255, 0.12);
}

.tag-line {
  flex-wrap: wrap;
  margin-top: 6px;
}

.tag-line em {
  color: #7b83c8;
  font-style: normal;
}

.state-pill.pass,
.state-pill.review-1 {
  color: #24966f;
  background: rgba(75, 197, 157, 0.14);
}

.state-pill.review-0 {
  color: #cc7a18;
  background: rgba(255, 184, 85, 0.18);
}

.state-pill.review-2 {
  color: #d95477;
  background: rgba(238, 113, 146, 0.16);
}

.state-pill.muted {
  color: #6870bc;
  background: rgba(112, 119, 255, 0.12);
}

.row-actions button {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  color: #5360ad;
  background: rgba(113, 120, 255, 0.1);
}

.empty-cell {
  height: 180px;
  text-align: center;
}

.pager {
  justify-content: center;
  margin-top: 14px;
}

.pager button {
  padding: 0 14px;
  color: #5360ad;
  background: rgba(255, 255, 255, 0.64);
}

.pager button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.review-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-card {
  padding: 20px;
  border-radius: 24px;
}

.side-card h3 {
  margin: 0 0 12px;
}

.side-card p {
  color: var(--muted);
  line-height: 1.7;
}

.side-card button {
  width: 100%;
  justify-content: center;
  margin-top: 10px;
  color: #5961b3;
}
</style>
