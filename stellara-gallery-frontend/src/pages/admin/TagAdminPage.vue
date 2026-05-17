<script setup lang="ts">
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined, TagsOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { addTag, deleteTag, listTags, updateTag, type TagVO } from '../../api/tag'

const tags = ref<TagVO[]>([])
const loading = ref(false)
const message = ref('')
const editingId = ref<number | null>(null)
const colors = ['#7b61ff', '#e65ca8', '#5a9fff', '#53c9df', '#55c89b', '#ffad4d', '#8a7cff', '#ff91c7']

const form = reactive({
  name: '',
  color: colors[0],
})

const totalUseCount = computed(() => tags.value.reduce((sum, item) => sum + (item.useCount || 0), 0))

const resetForm = () => {
  editingId.value = null
  form.name = ''
  form.color = colors[0]
}

const loadTags = async () => {
  loading.value = true
  message.value = ''
  try {
    tags.value = await listTags()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '标签加载失败'
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!form.name.trim()) {
    message.value = '标签名称不能为空'
    return
  }
  try {
    if (editingId.value) {
      await updateTag({ id: editingId.value, ...form })
      message.value = '标签已更新'
    } else {
      await addTag(form)
      message.value = '标签已新增'
    }
    resetForm()
    await loadTags()
  } catch (error) {
    message.value = error instanceof Error ? error.message : '保存失败'
  }
}

const editTag = (tag: TagVO) => {
  editingId.value = tag.id
  form.name = tag.name
  form.color = tag.color || colors[0]
}

const removeTag = async (tag: TagVO) => {
  if (!window.confirm(`确认删除标签「${tag.name}」吗？`)) return
  try {
    await deleteTag(tag.id)
    await loadTags()
    message.value = '标签已删除'
  } catch (error) {
    message.value = error instanceof Error ? error.message : '删除失败'
  }
}

onMounted(loadTags)
</script>

<template>
  <section class="tag-admin-page">
    <div class="tag-hero">
      <span class="page-kicker">Tags</span>
      <h2>标签管理 ✨</h2>
      <p>维护图片检索关键词，让灵感更容易被发现。</p>
    </div>

    <div class="tag-stats">
      <article>
        <TagsOutlined />
        <span>标签总数</span>
        <strong>{{ tags.length }}</strong>
      </article>
      <article>
        <PlusOutlined />
        <span>累计使用</span>
        <strong>{{ totalUseCount }}</strong>
      </article>
    </div>

    <div class="tag-layout">
      <main class="tag-panel">
        <header>
          <h3>标签星云</h3>
          <button type="button" @click="loadTags"><ReloadOutlined />刷新</button>
        </header>
        <p v-if="message" class="page-message">{{ message }}</p>
        <div v-if="loading" class="empty-panel">标签加载中...</div>
        <div v-else class="tag-cloud">
          <button
            v-for="tag in tags"
            :key="tag.id"
            :style="{ '--tag-color': tag.color || '#7b61ff' }"
            type="button"
            @click="editTag(tag)"
          >
            # {{ tag.name }}
            <span>{{ tag.useCount || 0 }}</span>
          </button>
        </div>

        <div class="tag-table">
          <table>
            <thead>
              <tr>
                <th>标签名称</th>
                <th>颜色</th>
                <th>使用次数</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!tags.length">
                <td colspan="5" class="empty-cell">暂无标签</td>
              </tr>
              <tr v-for="tag in tags" :key="tag.id">
                <td>{{ tag.name }}</td>
                <td><span class="color-dot" :style="{ background: tag.color || '#7b61ff' }" />{{ tag.color || '#7b61ff' }}</td>
                <td>{{ tag.useCount || 0 }}</td>
                <td>{{ tag.createTime?.slice(0, 16).replace('T', ' ') || '-' }}</td>
                <td>
                  <button class="icon-button" type="button" @click="editTag(tag)"><EditOutlined /></button>
                  <button class="icon-button danger" type="button" @click="removeTag(tag)"><DeleteOutlined /></button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>

      <aside class="tag-form">
        <h3>{{ editingId ? '编辑标签' : '新增标签' }}</h3>
        <label>
          标签名称
          <input v-model="form.name" placeholder="例如：星轨" />
        </label>
        <label>
          标签颜色
          <input v-model="form.color" placeholder="#7b61ff" />
        </label>
        <div class="color-list">
          <button
            v-for="color in colors"
            :key="color"
            :class="{ active: form.color === color }"
            :style="{ background: color }"
            type="button"
            @click="form.color = color"
          />
        </div>
        <footer>
          <button class="ghost-button" type="button" @click="resetForm">清空</button>
          <button class="primary-button" type="button" @click="submitForm">{{ editingId ? '保存标签' : '新增标签' }}</button>
        </footer>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.tag-admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.tag-hero,
.tag-panel,
.tag-form,
.tag-stats article {
  border: 1px solid rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.tag-hero {
  min-height: 220px;
  padding: 32px;
  border-radius: 34px;
  color: #ffffff;
  background:
    radial-gradient(circle at 76% 18%, rgba(255, 179, 244, 0.44), transparent 28%),
    linear-gradient(115deg, rgba(41, 70, 174, 0.92), rgba(128, 99, 235, 0.66)),
    url('../../assets/hero.png') center / cover no-repeat;
}

.tag-hero h2 {
  margin: 8px 0 10px;
  font-size: 40px;
}

.tag-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.84);
}

.tag-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.tag-stats article {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 3px 12px;
  padding: 18px;
  border-radius: 24px;
}

.tag-stats svg {
  grid-row: span 2;
  width: 44px;
  height: 44px;
  padding: 10px;
  border-radius: 16px;
  color: #ffffff;
  background: linear-gradient(135deg, #6578ff, #df8cff);
}

.tag-stats span {
  color: var(--muted);
  font-size: 13px;
}

.tag-stats strong {
  font-size: 24px;
}

.tag-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.tag-panel,
.tag-form {
  padding: 20px;
  border-radius: 28px;
}

.tag-panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-panel h3,
.tag-form h3 {
  margin: 0 0 14px;
}

.tag-panel header button,
.ghost-button,
.primary-button,
.icon-button {
  border: 0;
  border-radius: 14px;
}

.tag-panel header button,
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

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  border-radius: 22px;
  background: rgba(113, 120, 255, 0.08);
}

.tag-cloud button {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  border: 0;
  border-radius: 999px;
  color: #ffffff;
  font-weight: 800;
  background: var(--tag-color);
}

.tag-cloud span {
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
}

.tag-table {
  margin-top: 18px;
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 700px;
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
  background: rgba(113, 120, 255, 0.08);
}

.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  margin-right: 8px;
  border-radius: 50%;
  vertical-align: middle;
}

.icon-button {
  display: inline-grid;
  width: 32px;
  height: 32px;
  place-items: center;
  margin-right: 6px;
  color: #5360ad;
  background: rgba(113, 120, 255, 0.1);
}

.icon-button.danger {
  color: #d95477;
  background: rgba(238, 113, 146, 0.14);
}

.tag-form label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
  color: #4d58a4;
  font-size: 13px;
  font-weight: 800;
}

.tag-form input {
  height: 42px;
  padding: 0 13px;
  border: 1px solid rgba(116, 112, 210, 0.18);
  border-radius: 15px;
  outline: none;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.7);
}

.color-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.color-list button {
  width: 32px;
  height: 32px;
  border: 3px solid transparent;
  border-radius: 50%;
}

.color-list button.active {
  border-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(113, 120, 255, 0.24);
}

.tag-form footer {
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

.empty-panel,
.empty-cell {
  height: 180px;
  color: #6670bd;
  font-weight: 800;
  text-align: center;
}
</style>
