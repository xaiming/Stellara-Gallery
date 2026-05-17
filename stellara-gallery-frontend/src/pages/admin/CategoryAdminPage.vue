<script setup lang="ts">
import { AppstoreOutlined, DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  addCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  type CategoryVO,
} from '../../api/category'

const categories = ref<CategoryVO[]>([])
const loading = ref(false)
const message = ref('')
const editingId = ref<number | null>(null)

const form = reactive({
  name: '',
  icon: 'Sparkles',
  sort: 0,
  status: 0,
})

const enabledCount = computed(() => categories.value.filter((item) => item.status === 0).length)

const resetForm = () => {
  editingId.value = null
  form.name = ''
  form.icon = 'Sparkles'
  form.sort = 0
  form.status = 0
}

const loadCategories = async () => {
  loading.value = true
  message.value = ''
  try {
    categories.value = await listCategories()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '分类加载失败'
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!form.name.trim()) {
    message.value = '分类名称不能为空'
    return
  }
  try {
    if (editingId.value) {
      await updateCategory({ id: editingId.value, ...form })
      message.value = '分类已更新'
    } else {
      await addCategory(form)
      message.value = '分类已新增'
    }
    resetForm()
    await loadCategories()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '保存失败'
  }
}

const editCategory = (category: CategoryVO) => {
  editingId.value = category.id
  form.name = category.name
  form.icon = category.icon || 'Sparkles'
  form.sort = category.sort || 0
  form.status = category.status || 0
}

const toggleStatus = async (category: CategoryVO) => {
  try {
    await updateCategory({
      id: category.id,
      name: category.name,
      icon: category.icon,
      sort: category.sort,
      status: category.status === 0 ? 1 : 0,
    })
    await loadCategories()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '状态更新失败'
  }
}

const removeCategory = async (category: CategoryVO) => {
  if (!window.confirm(`确认删除分类「${category.name}」吗？`)) return
  try {
    await deleteCategory(category.id)
    await loadCategories()
    message.value = '分类已删除'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '删除失败'
  }
}

onMounted(loadCategories)
</script>

<template>
  <section class="taxonomy-page">
    <div class="taxonomy-hero category">
      <span class="page-kicker">Category</span>
      <h2>分类管理 ✨</h2>
      <p>维护公共图库和上传表单使用的分类体系。</p>
    </div>

    <div class="taxonomy-stats">
      <article>
        <AppstoreOutlined />
        <span>分类总数</span>
        <strong>{{ categories.length }}</strong>
      </article>
      <article>
        <PlusOutlined />
        <span>启用分类</span>
        <strong>{{ enabledCount }}</strong>
      </article>
      <article>
        <ReloadOutlined />
        <span>排序最高</span>
        <strong>{{ categories[0]?.sort ?? 0 }}</strong>
      </article>
    </div>

    <div class="taxonomy-layout">
      <main class="taxonomy-panel">
        <header>
          <h3>分类星图</h3>
          <button type="button" @click="loadCategories"><ReloadOutlined />刷新</button>
        </header>
        <p v-if="message" class="page-message">{{ message }}</p>
        <div v-if="loading" class="empty-panel">分类加载中...</div>
        <div v-else class="category-grid">
          <article v-for="category in categories" :key="category.id" class="category-card">
            <button class="more-button" type="button" @click="editCategory(category)"><EditOutlined /></button>
            <div class="category-icon">{{ category.icon?.slice(0, 1) || '星' }}</div>
            <h3>{{ category.name }}</h3>
            <p>排序 {{ category.sort ?? 0 }}</p>
            <div class="card-footer">
              <button :class="['state-pill', { disabled: category.status === 1 }]" type="button" @click="toggleStatus(category)">
                {{ category.status === 1 ? '已禁用' : '启用中' }}
              </button>
              <button class="danger" type="button" @click="removeCategory(category)"><DeleteOutlined /></button>
            </div>
          </article>
        </div>
      </main>

      <aside class="form-panel">
        <h3>{{ editingId ? '编辑分类' : '新增分类' }}</h3>
        <label>
          分类名称
          <input v-model="form.name" placeholder="例如：天空之城" />
        </label>
        <label>
          图标标识
          <input v-model="form.icon" placeholder="Sparkles / Cloud / Crown" />
        </label>
        <label>
          排序
          <input v-model.number="form.sort" type="number" min="0" />
        </label>
        <label>
          状态
          <select v-model.number="form.status">
            <option :value="0">启用</option>
            <option :value="1">禁用</option>
          </select>
        </label>
        <footer>
          <button class="ghost-button" type="button" @click="resetForm">清空</button>
          <button class="primary-button" type="button" @click="submitForm">{{ editingId ? '保存分类' : '新增分类' }}</button>
        </footer>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.taxonomy-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.taxonomy-hero,
.taxonomy-panel,
.form-panel,
.taxonomy-stats article,
.category-card {
  border: 1px solid rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.taxonomy-hero {
  min-height: 220px;
  padding: 32px;
  border-radius: 34px;
  color: #ffffff;
  background:
    radial-gradient(circle at 78% 18%, rgba(255, 178, 244, 0.42), transparent 28%),
    linear-gradient(115deg, rgba(40, 70, 176, 0.92), rgba(125, 103, 236, 0.68)),
    url('../../assets/hero.png') center / cover no-repeat;
}

.taxonomy-hero h2 {
  margin: 8px 0 10px;
  font-size: 40px;
}

.taxonomy-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.84);
}

.taxonomy-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.taxonomy-stats article {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 3px 12px;
  padding: 18px;
  border-radius: 24px;
}

.taxonomy-stats svg {
  grid-row: span 2;
  width: 44px;
  height: 44px;
  padding: 10px;
  border-radius: 16px;
  color: #ffffff;
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.taxonomy-stats span {
  color: var(--muted);
  font-size: 13px;
}

.taxonomy-stats strong {
  font-size: 24px;
}

.taxonomy-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.taxonomy-panel,
.form-panel {
  padding: 20px;
  border-radius: 28px;
}

.taxonomy-panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.taxonomy-panel h3,
.form-panel h3 {
  margin: 0 0 14px;
}

.taxonomy-panel header button,
.primary-button,
.ghost-button,
.state-pill,
.more-button,
.danger {
  border: 0;
  border-radius: 14px;
}

.taxonomy-panel header button,
.ghost-button {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  gap: 6px;
  padding: 0 14px;
  color: #5d66b4;
  background: rgba(113, 120, 255, 0.1);
}

.page-message {
  margin: 0 0 12px;
  padding: 10px 14px;
  border-radius: 16px;
  color: #5961b3;
  background: rgba(112, 119, 255, 0.1);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.category-card {
  position: relative;
  min-height: 210px;
  padding: 24px;
  border-radius: 24px;
  text-align: center;
}

.more-button {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 34px;
  height: 34px;
  color: #5360ad;
  background: rgba(113, 120, 255, 0.1);
}

.category-icon {
  display: grid;
  width: 68px;
  height: 68px;
  place-items: center;
  margin: 4px auto 14px;
  border-radius: 24px;
  color: #ffffff;
  font-size: 28px;
  font-weight: 900;
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.category-card h3 {
  margin: 0;
}

.category-card p {
  color: var(--muted);
}

.card-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.state-pill,
.danger {
  min-height: 34px;
  padding: 0 12px;
}

.state-pill {
  color: #22946e;
  background: rgba(75, 197, 157, 0.14);
}

.state-pill.disabled,
.danger {
  color: #d95477;
  background: rgba(238, 113, 146, 0.14);
}

.form-panel label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
  color: #4d58a4;
  font-size: 13px;
  font-weight: 800;
}

.form-panel input,
.form-panel select {
  height: 42px;
  padding: 0 13px;
  border: 1px solid rgba(116, 112, 210, 0.18);
  border-radius: 15px;
  outline: none;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.7);
}

.form-panel footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.primary-button {
  min-height: 40px;
  padding: 0 16px;
  color: #ffffff;
  font-weight: 800;
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.empty-panel {
  display: grid;
  min-height: 220px;
  place-items: center;
  color: #6670bd;
  font-weight: 800;
}
</style>
