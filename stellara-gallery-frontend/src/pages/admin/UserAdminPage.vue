<script setup lang="ts">
import {
  BarChartOutlined,
  CrownFilled,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  EyeOutlined,
  FileTextOutlined,
  FolderAddOutlined,
  GiftOutlined,
  MoreOutlined,
  PlusOutlined,
  SearchOutlined,
  SendOutlined,
  TeamOutlined,
  UserAddOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  addUser,
  deleteUser,
  getUserById,
  listAuditLogs,
  listUsersByPage,
  resetUserPassword,
  updateUser,
  type AuditLogVO,
  type UserVO,
} from '../../api/user'

type RoleFilter = 'all' | 'user' | 'admin'
type StatusFilter = 'all' | 0 | 1

const avatarAuras = [
  'linear-gradient(135deg, #b7c4ff, #f5b3ff)',
  'linear-gradient(135deg, #d4c5ff, #9ed8ff)',
  'linear-gradient(135deg, #242a66, #988cff)',
  'linear-gradient(135deg, #ffc2dc, #9fb6ff)',
  'linear-gradient(135deg, #ffd9be, #f6edff)',
  'linear-gradient(135deg, #8bb8ff, #d5b2ff)',
]

const users = ref<UserVO[]>([])
const loading = ref(false)
const errorMessage = ref('')
const searchText = ref('')
const roleFilter = ref<RoleFilter>('all')
const statusFilter = ref<StatusFilter>('all')
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalUsers = ref(0)
const activeUsers = ref(0)
const adminUsers = ref(0)
const addModalOpen = ref(false)
const adding = ref(false)
const detailModalOpen = ref(false)
const editModalOpen = ref(false)
const detailLoading = ref(false)
const editing = ref(false)
const currentDetail = ref<UserVO | null>(null)
const noticeMessage = ref('')
const auditModalOpen = ref(false)
const auditLoading = ref(false)
const auditLogs = ref<AuditLogVO[]>([])
const auditTotal = ref(0)
const addForm = reactive({
  userAccount: '',
  userPassword: '',
  userName: '',
  userRole: 'user',
  userStatus: 0,
})
const editForm = reactive({
  id: 0,
  userName: '',
  userAvatar: '',
  userCover: '',
  userProfile: '',
  userRole: 'user',
  userStatus: 0,
})

const buildQuery = () => {
  const keyword = searchText.value.trim()
  const query: Record<string, number | string> = {
    current: current.value,
    pageSize: pageSize.value,
    sortField: 'create_time',
    sortOrder: 'descend',
  }
  if (keyword) {
    query.keyword = keyword
  }
  if (roleFilter.value !== 'all') {
    query.userRole = roleFilter.value
  }
  if (statusFilter.value !== 'all') {
    query.userStatus = statusFilter.value
  }
  return query
}

const fetchUsers = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const page = await listUsersByPage(buildQuery())
    users.value = page.records ?? []
    total.value = Number(page.total ?? 0)
    current.value = Number(page.current ?? current.value)
    pageSize.value = Number(page.size ?? pageSize.value)
  } catch (error) {
    users.value = []
    total.value = 0
    errorMessage.value = error instanceof Error ? error.message : '用户列表加载失败'
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const [allPage, activePage, adminPage] = await Promise.all([
      listUsersByPage({ current: 1, pageSize: 1 }),
      listUsersByPage({ current: 1, pageSize: 1, userStatus: 0 }),
      listUsersByPage({ current: 1, pageSize: 1, userRole: 'admin' }),
    ])
    totalUsers.value = Number(allPage.total ?? 0)
    activeUsers.value = Number(activePage.total ?? 0)
    adminUsers.value = Number(adminPage.total ?? 0)
  } catch {
    totalUsers.value = total.value
  }
}

const stats = computed(() => [
  { label: '用户总数', value: formatNumber(totalUsers.value || total.value), trend: '来自后端分页统计', icon: UserOutlined, tone: 'violet' },
  { label: '活跃用户', value: formatNumber(activeUsers.value), trend: '状态为正常', icon: TeamOutlined, tone: 'mint' },
  { label: '管理员', value: formatNumber(adminUsers.value), trend: '角色为 admin', icon: CrownFilled, tone: 'purple' },
  { label: '本周新增', value: formatNumber(weeklyNewUsers.value), trend: '按 createTime 计算', icon: GiftOutlined, tone: 'peach' },
])

const weeklyNewUsers = computed(() => {
  const weekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000
  return users.value.filter((user) => user.createTime && new Date(user.createTime).getTime() >= weekAgo).length
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const pageNumbers = computed(() => {
  const pages = totalPages.value
  const start = Math.max(1, Math.min(current.value - 2, pages - 4))
  return Array.from({ length: Math.min(5, pages) }, (_, index) => start + index)
})

const userRatio = computed(() => {
  const base = totalUsers.value || 1
  return {
    normal: Math.round(((totalUsers.value - adminUsers.value) / base) * 1000) / 10,
    admin: Math.round((adminUsers.value / base) * 1000) / 10,
    blocked: Math.round(((totalUsers.value - activeUsers.value) / base) * 1000) / 10,
  }
})

const formatNumber = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '暂无记录')
const getDisplayName = (user: UserVO) => user.userName || user.userAccount || `用户 ${user.id}`
const getRoleText = (role?: string) => (role === 'admin' ? '管理员' : '普通用户')
const getStatusText = (status?: number) => (status === 1 ? '禁用' : '正常')
const getAura = (index: number) => avatarAuras[index % avatarAuras.length]
const showNotice = (message: string) => {
  noticeMessage.value = message
  window.setTimeout(() => {
    if (noticeMessage.value === message) {
      noticeMessage.value = ''
    }
  }, 2600)
}

const applyFilters = async () => {
  current.value = 1
  await fetchUsers()
  await fetchStats()
}

const changePage = async (page: number) => {
  current.value = Math.min(Math.max(page, 1), totalPages.value)
  await fetchUsers()
}

const toggleUserStatus = async (user: UserVO) => {
  const nextStatus = user.userStatus === 1 ? '启用' : '禁用'
  if (!window.confirm(`确认${nextStatus}用户「${getDisplayName(user)}」吗？`)) {
    return
  }
  await updateUser({
    id: user.id,
    userStatus: user.userStatus === 1 ? 0 : 1,
  })
  await fetchUsers()
  await fetchStats()
  showNotice(`${nextStatus}用户成功`)
}

const resetAddForm = () => {
  addForm.userAccount = ''
  addForm.userPassword = ''
  addForm.userName = ''
  addForm.userRole = 'user'
  addForm.userStatus = 0
}

const submitAddUser = async () => {
  errorMessage.value = ''
  adding.value = true
  try {
    await addUser({
      userAccount: addForm.userAccount.trim(),
      userPassword: addForm.userPassword,
      userName: addForm.userName.trim() || undefined,
      userRole: addForm.userRole,
      userStatus: addForm.userStatus,
    })
    addModalOpen.value = false
    resetAddForm()
    current.value = 1
    await fetchUsers()
    await fetchStats()
    showNotice('新增用户成功')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '新增用户失败'
  } finally {
    adding.value = false
  }
}

const openUserDetail = async (user: UserVO) => {
  detailModalOpen.value = true
  detailLoading.value = true
  currentDetail.value = null
  errorMessage.value = ''
  try {
    currentDetail.value = await getUserById(user.id)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户详情加载失败'
  } finally {
    detailLoading.value = false
  }
}

const openEditUser = async (user: UserVO) => {
  editModalOpen.value = true
  detailLoading.value = true
  errorMessage.value = ''
  try {
    const detail = await getUserById(user.id)
    currentDetail.value = detail
    editForm.id = detail.id
    editForm.userName = detail.userName ?? ''
    editForm.userAvatar = detail.userAvatar ?? ''
    editForm.userCover = detail.userCover ?? ''
    editForm.userProfile = detail.userProfile ?? ''
    editForm.userRole = detail.userRole ?? 'user'
    editForm.userStatus = detail.userStatus ?? 0
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户详情加载失败'
  } finally {
    detailLoading.value = false
  }
}

const submitEditUser = async () => {
  editing.value = true
  errorMessage.value = ''
  try {
    await updateUser({
      id: editForm.id,
      userName: editForm.userName.trim() || undefined,
      userAvatar: editForm.userAvatar.trim() || undefined,
      userCover: editForm.userCover.trim() || undefined,
      userProfile: editForm.userProfile.trim() || undefined,
      userRole: editForm.userRole,
      userStatus: editForm.userStatus,
    })
    editModalOpen.value = false
    await fetchUsers()
    await fetchStats()
    showNotice('编辑用户成功')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '编辑用户失败'
  } finally {
    editing.value = false
  }
}

const handleDeleteUser = async (user: UserVO) => {
  if (!window.confirm(`确认删除用户「${getDisplayName(user)}」吗？该操作会进入逻辑删除。`)) {
    return
  }
  try {
    await deleteUser(user.id)
    await fetchUsers()
    await fetchStats()
    showNotice('删除用户成功')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '删除用户失败'
  }
}

const handleResetPassword = async (user: UserVO) => {
  const newPassword = window.prompt(`请输入用户「${getDisplayName(user)}」的新密码（至少 8 位）`)
  if (!newPassword) {
    return
  }
  try {
    await resetUserPassword({ id: user.id, newPassword })
    showNotice('重置密码成功')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '重置密码失败'
  }
}

const openAuditLogs = async () => {
  auditModalOpen.value = true
  auditLoading.value = true
  errorMessage.value = ''
  try {
    const page = await listAuditLogs({ current: 1, pageSize: 12 })
    auditLogs.value = page.records ?? []
    auditTotal.value = Number(page.total ?? 0)
  } catch (error) {
    auditLogs.value = []
    auditTotal.value = 0
    errorMessage.value = error instanceof Error ? error.message : '审计日志加载失败'
  } finally {
    auditLoading.value = false
  }
}

const quickActions = [
  { title: '创建用户', desc: '快速创建新用户账号', icon: UserAddOutlined },
  { title: '批量导出', desc: '导出用户明细到文件', icon: FolderAddOutlined },
  { title: '发送通知', desc: '向用户发送系统通知', icon: SendOutlined },
  { title: '查看日志', desc: '查看用户操作日志', icon: FileTextOutlined, action: openAuditLogs },
]

onMounted(async () => {
  await fetchUsers()
  await fetchStats()
})
</script>

<template>
  <section class="user-admin-page">
    <div v-if="noticeMessage" class="notice-toast">{{ noticeMessage }}</div>

    <header class="user-hero">
      <div class="hero-copy">
        <h2>用户管理 ✨</h2>
        <p>管理平台用户、角色、状态与空间信息</p>
      </div>
      <div class="castle" aria-hidden="true">
        <span class="tower tower-a" />
        <span class="tower tower-b" />
        <span class="tower tower-c" />
        <span class="moon" />
      </div>
      <div class="stat-grid">
        <article v-for="item in stats" :key="item.label" class="stat-card" :class="`is-${item.tone}`">
          <div class="stat-icon">
            <component :is="item.icon" />
          </div>
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <em>{{ item.trend }}</em>
          </div>
        </article>
      </div>
    </header>

    <div class="admin-grid">
      <section class="users-panel">
        <div class="filter-toolbar">
          <label class="admin-search">
            <SearchOutlined />
            <input v-model="searchText" placeholder="搜索用户昵称 / 账号 / ID" @keydown.enter="applyFilters" />
          </label>

          <div class="segmented">
            <span>角色:</span>
            <button :class="{ selected: roleFilter === 'all' }" @click="roleFilter = 'all'; applyFilters()">全部</button>
            <button :class="{ selected: roleFilter === 'user' }" @click="roleFilter = 'user'; applyFilters()">普通用户</button>
            <button :class="{ selected: roleFilter === 'admin' }" @click="roleFilter = 'admin'; applyFilters()">管理员</button>
          </div>

          <div class="segmented">
            <span>状态:</span>
            <button :class="{ selected: statusFilter === 'all' }" @click="statusFilter = 'all'; applyFilters()">全部</button>
            <button :class="{ selected: statusFilter === 0 }" @click="statusFilter = 0; applyFilters()">正常</button>
            <button :class="{ selected: statusFilter === 1 }" @click="statusFilter = 1; applyFilters()">禁用</button>
          </div>

          <button class="primary-action" @click="addModalOpen = true">
            <PlusOutlined />
            新增用户
          </button>
          <button class="soft-action">
            <DownloadOutlined />
            导出数据
          </button>
          <button class="soft-action">
            <DeleteOutlined />
            批量禁用
          </button>
        </div>

        <div class="table-shell">
          <table class="user-table">
            <thead>
              <tr>
                <th>用户</th>
                <th>账号</th>
                <th>角色</th>
                <th>状态</th>
                <th>公开作品</th>
                <th>团队空间</th>
                <th>最后登录</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody v-if="!loading && users.length">
              <tr v-for="(user, index) in users" :key="user.id">
                <td>
                  <div class="identity-cell">
                    <img v-if="user.userAvatar" class="mock-avatar image-avatar" :src="user.userAvatar" :alt="getDisplayName(user)" />
                    <span v-else class="mock-avatar" :style="{ background: getAura(index) }">{{ getDisplayName(user).slice(0, 1) }}</span>
                    <div>
                      <strong>{{ getDisplayName(user) }}</strong>
                      <small>ID: {{ user.id }}</small>
                    </div>
                  </div>
                </td>
                <td>{{ user.userAccount }}</td>
                <td>
                  <span class="role-tag" :class="{ admin: user.userRole === 'admin' }">{{ getRoleText(user.userRole) }}</span>
                </td>
                <td>
                  <span class="status-tag" :class="{ disabled: user.userStatus === 1 }">{{ getStatusText(user.userStatus) }}</span>
                </td>
                <td>0</td>
                <td>0</td>
                <td>{{ formatTime(user.lastLoginTime) }}</td>
                <td>
                  <div class="table-actions">
                    <button @click="openUserDetail(user)"><EyeOutlined />查看</button>
                    <button @click="openEditUser(user)"><EditOutlined />编辑</button>
                    <button :class="{ enable: user.userStatus === 1 }" @click="toggleUserStatus(user)">
                      {{ user.userStatus === 1 ? '启用' : '禁用' }}
                      <MoreOutlined />
                    </button>
                    <button @click="handleResetPassword(user)">重置密码</button>
                    <button class="danger" @click="handleDeleteUser(user)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="loading" class="table-state">正在加载后端用户数据...</div>
          <div v-else-if="errorMessage" class="table-state error-state">{{ errorMessage }}</div>
          <div v-else-if="!users.length" class="table-state">暂无匹配用户</div>
        </div>

        <footer class="pagination-row">
          <span>共 {{ formatNumber(total) }} 条</span>
          <button class="page-size">10条/页</button>
          <div class="pager">
            <button :disabled="current <= 1" @click="changePage(current - 1)">‹</button>
            <button v-for="page in pageNumbers" :key="page" :class="{ current: page === current }" @click="changePage(page)">
              {{ page }}
            </button>
            <span v-if="totalPages > 5">...</span>
            <button v-if="totalPages > 5" @click="changePage(totalPages)">{{ totalPages }}</button>
            <button :disabled="current >= totalPages" @click="changePage(current + 1)">›</button>
          </div>
          <span>前往</span>
          <button class="page-input">{{ current }}</button>
          <span>页</span>
        </footer>
      </section>

      <aside class="insight-column">
        <section class="insight-card overview-card">
          <div class="card-title">
            <h3>用户概览 ✨</h3>
            <span>本周数据</span>
          </div>
          <div class="overview-body">
            <div class="donut">
              <div>
                <strong>{{ formatNumber(totalUsers || total) }}</strong>
                <span>总用户数</span>
              </div>
            </div>
            <ul class="legend-list">
              <li><i class="normal" />普通用户 <strong>{{ userRatio.normal }}%</strong><span>{{ formatNumber(Math.max(totalUsers - adminUsers, 0)) }}</span></li>
              <li><i class="admin" />管理员 <strong>{{ userRatio.admin }}%</strong><span>{{ formatNumber(adminUsers) }}</span></li>
              <li><i class="blocked" />禁用用户 <strong>{{ userRatio.blocked }}%</strong><span>{{ formatNumber(Math.max(totalUsers - activeUsers, 0)) }}</span></li>
            </ul>
          </div>
          <div class="trend-head">
            <span>新增趋势（近7天）</span>
            <a>查看更多 ›</a>
          </div>
          <div class="trend-chart">
            <svg viewBox="0 0 260 96" role="img" aria-label="新增趋势折线图">
              <defs>
                <linearGradient id="trendFill" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stop-color="#8767ff" stop-opacity="0.45" />
                  <stop offset="100%" stop-color="#8767ff" stop-opacity="0" />
                </linearGradient>
              </defs>
              <path d="M8 68 L48 58 L88 60 L128 72 L168 54 L208 36 L248 22 L248 92 L8 92 Z" fill="url(#trendFill)" />
              <path d="M8 68 L48 58 L88 60 L128 72 L168 54 L208 36 L248 22" fill="none" stroke="#7b61ff" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
              <g fill="#ffffff" stroke="#7b61ff" stroke-width="3">
                <circle cx="8" cy="68" r="4" /><circle cx="48" cy="58" r="4" /><circle cx="88" cy="60" r="4" /><circle cx="128" cy="72" r="4" /><circle cx="168" cy="54" r="4" /><circle cx="208" cy="36" r="4" /><circle cx="248" cy="22" r="4" />
              </g>
            </svg>
            <div class="chart-days">
              <span>05-06</span><span>05-07</span><span>05-08</span><span>05-09</span><span>05-10</span><span>05-11</span><span>05-12</span>
            </div>
          </div>
        </section>

        <section class="insight-card shortcut-card">
          <div class="card-title">
            <h3>快捷操作 ✨</h3>
          </div>
          <button v-for="action in quickActions" :key="action.title" class="quick-action" @click="action.action?.()">
            <component :is="action.icon" />
            <span>
              <strong>{{ action.title }}</strong>
              <small>{{ action.desc }}</small>
            </span>
            <BarChartOutlined />
          </button>
        </section>
      </aside>
    </div>

    <div v-if="addModalOpen" class="modal-mask">
      <form class="user-modal" @submit.prevent="submitAddUser">
        <div class="modal-title">
          <div>
            <h3>新增用户 ✨</h3>
            <p>创建一个后端真实用户账号</p>
          </div>
          <button type="button" @click="addModalOpen = false">×</button>
        </div>
        <label>
          <span>账号</span>
          <input v-model="addForm.userAccount" required minlength="4" placeholder="至少 4 位" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="addForm.userPassword" required minlength="8" type="password" placeholder="至少 8 位" />
        </label>
        <label>
          <span>昵称</span>
          <input v-model="addForm.userName" placeholder="可选" />
        </label>
        <div class="modal-row">
          <label>
            <span>角色</span>
            <select v-model="addForm.userRole">
              <option value="user">普通用户</option>
              <option value="admin">管理员</option>
            </select>
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="addForm.userStatus">
              <option :value="0">正常</option>
              <option :value="1">禁用</option>
            </select>
          </label>
        </div>
        <p v-if="errorMessage" class="modal-error">{{ errorMessage }}</p>
        <div class="modal-actions">
          <button type="button" class="soft-action" @click="addModalOpen = false">取消</button>
          <button type="submit" class="primary-action" :disabled="adding">{{ adding ? '创建中...' : '确认创建' }}</button>
        </div>
      </form>
    </div>

    <div v-if="detailModalOpen" class="modal-mask">
      <section class="user-modal detail-modal">
        <div class="modal-title">
          <div>
            <h3>用户详情 ✨</h3>
            <p>查看后端返回的用户基础信息</p>
          </div>
          <button type="button" @click="detailModalOpen = false">×</button>
        </div>
        <div v-if="detailLoading" class="table-state">正在加载用户详情...</div>
        <p v-else-if="errorMessage" class="modal-error">{{ errorMessage }}</p>
        <div v-else-if="currentDetail" class="detail-list">
          <div class="detail-profile">
            <img v-if="currentDetail.userAvatar" class="detail-avatar" :src="currentDetail.userAvatar" :alt="getDisplayName(currentDetail)" />
            <span v-else class="detail-avatar fallback">{{ getDisplayName(currentDetail).slice(0, 1) }}</span>
            <div>
              <strong>{{ getDisplayName(currentDetail) }}</strong>
              <small>{{ currentDetail.userAccount }}</small>
            </div>
          </div>
          <dl>
            <dt>ID</dt><dd>{{ currentDetail.id }}</dd>
            <dt>角色</dt><dd>{{ getRoleText(currentDetail.userRole) }}</dd>
            <dt>状态</dt><dd>{{ getStatusText(currentDetail.userStatus) }}</dd>
            <dt>个人简介</dt><dd>{{ currentDetail.userProfile || '暂无简介' }}</dd>
            <dt>最后登录</dt><dd>{{ formatTime(currentDetail.lastLoginTime) }}</dd>
            <dt>创建时间</dt><dd>{{ formatTime(currentDetail.createTime) }}</dd>
            <dt>更新时间</dt><dd>{{ formatTime(currentDetail.updateTime) }}</dd>
          </dl>
        </div>
      </section>
    </div>

    <div v-if="editModalOpen" class="modal-mask">
      <form class="user-modal" @submit.prevent="submitEditUser">
        <div class="modal-title">
          <div>
            <h3>编辑用户 ✨</h3>
            <p>更新昵称、头像、角色和状态</p>
          </div>
          <button type="button" @click="editModalOpen = false">×</button>
        </div>
        <div v-if="detailLoading" class="table-state">正在加载用户信息...</div>
        <template v-else>
          <label>
            <span>昵称</span>
            <input v-model="editForm.userName" placeholder="用户昵称" />
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
            <textarea v-model="editForm.userProfile" rows="3" placeholder="写一点用户简介" />
          </label>
          <div class="modal-row">
            <label>
              <span>角色</span>
              <select v-model="editForm.userRole">
                <option value="user">普通用户</option>
                <option value="admin">管理员</option>
              </select>
            </label>
            <label>
              <span>状态</span>
              <select v-model.number="editForm.userStatus">
                <option :value="0">正常</option>
                <option :value="1">禁用</option>
              </select>
            </label>
          </div>
          <p v-if="errorMessage" class="modal-error">{{ errorMessage }}</p>
          <div class="modal-actions">
            <button type="button" class="soft-action" @click="editModalOpen = false">取消</button>
            <button type="submit" class="primary-action" :disabled="editing">{{ editing ? '保存中...' : '保存修改' }}</button>
          </div>
        </template>
      </form>
    </div>

    <div v-if="auditModalOpen" class="modal-mask">
      <section class="user-modal audit-modal">
        <div class="modal-title">
          <div>
            <h3>操作审计 ✨</h3>
            <p>共 {{ auditTotal }} 条用户模块操作记录</p>
          </div>
          <button type="button" @click="auditModalOpen = false">×</button>
        </div>
        <div v-if="auditLoading" class="table-state">正在加载审计日志...</div>
        <p v-else-if="errorMessage" class="modal-error">{{ errorMessage }}</p>
        <div v-else-if="auditLogs.length" class="audit-list">
          <article v-for="log in auditLogs" :key="log.id">
            <strong>{{ log.action }}</strong>
            <span>{{ log.detail || '暂无说明' }}</span>
            <small>操作者：{{ log.operatorAccount || log.operatorId || '-' }} / 目标用户：{{ log.targetUserId || '-' }} / {{ formatTime(log.createTime) }}</small>
          </article>
        </div>
        <div v-else class="table-state">暂无审计日志</div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.user-admin-page {
  position: relative;
  min-height: calc(100vh - 120px);
  overflow: hidden;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 32px;
  color: #25308c;
  background:
    radial-gradient(circle at 88% 18%, rgba(255, 156, 226, 0.38), transparent 24%),
    radial-gradient(circle at 42% 0%, rgba(133, 170, 255, 0.54), transparent 34%),
    linear-gradient(135deg, rgba(64, 89, 201, 0.2), rgba(236, 230, 255, 0.84) 42%, rgba(236, 244, 255, 0.74)),
    url('../../assets/stellara-login-bg.png') center / cover no-repeat;
  box-shadow: 0 30px 80px rgba(71, 70, 166, 0.25);
}

.user-admin-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    radial-gradient(circle, rgba(255, 255, 255, 0.95) 0 1px, transparent 2px),
    linear-gradient(115deg, transparent 0 64%, rgba(255, 255, 255, 0.48) 64.1%, transparent 64.5%);
  background-size: 92px 92px, 100% 100%;
  opacity: 0.52;
}

.notice-toast {
  position: fixed;
  z-index: 40;
  top: 96px;
  left: 50%;
  padding: 10px 18px;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 999px;
  color: #ffffff;
  background: linear-gradient(135deg, #5f78ff, #df82ff);
  box-shadow: 0 14px 34px rgba(88, 78, 190, 0.32);
  font-size: 13px;
  font-weight: 900;
  transform: translateX(-50%);
}

.user-hero,
.admin-grid {
  position: relative;
  z-index: 1;
}

.user-hero {
  min-height: 214px;
  padding: 28px 24px 22px;
  overflow: hidden;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.04)),
    linear-gradient(110deg, rgba(52, 78, 189, 0.58), rgba(143, 126, 255, 0.3), rgba(255, 184, 233, 0.18));
}

.hero-copy h2 {
  margin: 0;
  color: #ffffff;
  font-size: 30px;
  line-height: 1.15;
  text-shadow: 0 8px 24px rgba(48, 60, 155, 0.45);
}

.hero-copy p {
  margin: 10px 0 0;
  color: rgba(246, 249, 255, 0.9);
  font-size: 15px;
  font-weight: 600;
}

.castle {
  position: absolute;
  right: 176px;
  top: 24px;
  width: 230px;
  height: 120px;
  opacity: 0.88;
  filter: drop-shadow(0 0 24px rgba(255, 255, 255, 0.72));
}

.castle::before {
  position: absolute;
  right: 22px;
  bottom: 10px;
  width: 164px;
  height: 42px;
  border-radius: 22px 22px 8px 8px;
  content: '';
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(188, 199, 255, 0.56));
}

.tower {
  position: absolute;
  bottom: 36px;
  width: 28px;
  border-radius: 12px 12px 4px 4px;
  background: linear-gradient(180deg, #ffffff, rgba(183, 196, 255, 0.68));
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

.tower-a { right: 128px; height: 54px; }
.tower-b { right: 86px; height: 82px; }
.tower-c { right: 48px; height: 62px; }

.moon {
  position: absolute;
  right: 16px;
  top: 18px;
  width: 44px;
  height: 44px;
  border: 2px solid rgba(255, 255, 255, 0.76);
  border-radius: 50%;
}

.stat-grid {
  display: grid;
  margin-top: 20px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card,
.users-panel,
.insight-card {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.68);
  box-shadow: 0 18px 42px rgba(78, 77, 171, 0.18);
  backdrop-filter: blur(22px);
}

.stat-card {
  display: flex;
  min-height: 98px;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 20px;
}

.stat-icon {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border-radius: 20px;
  color: #ffffff;
  font-size: 24px;
  box-shadow: inset 0 0 0 8px rgba(255, 255, 255, 0.22), 0 10px 22px rgba(92, 81, 200, 0.2);
}

.is-violet .stat-icon { background: linear-gradient(135deg, #6f76ff, #9a6dff); }
.is-mint .stat-icon { background: linear-gradient(135deg, #5ad6bf, #63cb94); }
.is-purple .stat-icon { background: linear-gradient(135deg, #8d6bff, #6e5dff); }
.is-peach .stat-icon { background: linear-gradient(135deg, #ffad73, #ff7fab); }

.stat-card span,
.stat-card em {
  display: block;
  font-style: normal;
}

.stat-card span {
  color: #5863ad;
  font-size: 13px;
  font-weight: 700;
}

.stat-card strong {
  display: block;
  margin: 4px 0;
  color: #19228e;
  font-size: 25px;
}

.stat-card em {
  color: #5b73b8;
  font-size: 12px;
}

.admin-grid {
  display: grid;
  margin-top: 18px;
  grid-template-columns: minmax(880px, 1fr) 278px;
  gap: 16px;
}

.users-panel {
  min-width: 0;
  padding: 14px 16px 16px;
  border-radius: 22px;
}

.filter-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.admin-search,
.segmented,
.soft-action,
.primary-action,
.page-size,
.pager button,
.page-input {
  border: 1px solid rgba(124, 122, 218, 0.2);
  background: rgba(255, 255, 255, 0.54);
}

.admin-search {
  display: flex;
  width: 220px;
  height: 38px;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border-radius: 18px;
  color: #7480c4;
}

.admin-search input {
  width: 100%;
  border: 0;
  outline: 0;
  color: #24308c;
  background: transparent;
  font-size: 13px;
}

.segmented {
  display: flex;
  height: 38px;
  align-items: center;
  gap: 4px;
  padding: 0 6px 0 10px;
  border-radius: 18px;
  color: #5260ae;
  font-size: 12px;
  font-weight: 700;
}

.segmented button {
  min-width: 46px;
  height: 26px;
  border: 0;
  border-radius: 13px;
  color: #5260ae;
  background: transparent;
  font-size: 12px;
  font-weight: 700;
}

.segmented .selected {
  color: #ffffff;
  background: linear-gradient(135deg, #6576ff, #b375ff);
  box-shadow: 0 8px 16px rgba(112, 96, 255, 0.28);
}

.soft-action,
.primary-action {
  display: inline-flex;
  height: 38px;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 0 13px;
  border-radius: 16px;
  color: #5260b1;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.primary-action {
  margin-left: auto;
  border-color: rgba(255, 255, 255, 0.72);
  color: #ffffff;
  background: linear-gradient(135deg, #4f72ff, #dd7cff);
  box-shadow: 0 10px 24px rgba(117, 95, 255, 0.34);
}

.table-shell {
  overflow: hidden;
  border: 1px solid rgba(125, 124, 215, 0.2);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.52);
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  color: #4651a4;
  font-size: 13px;
}

.user-table th {
  height: 44px;
  color: #35439b;
  background: rgba(231, 232, 255, 0.66);
  font-weight: 800;
  text-align: left;
}

.user-table th,
.user-table td {
  padding: 0 14px;
  border-bottom: 1px solid rgba(122, 120, 205, 0.14);
  white-space: nowrap;
}

.user-table tbody tr {
  height: 52px;
  transition: background 0.18s ease;
}

.user-table tbody tr:hover {
  background: rgba(235, 236, 255, 0.66);
}

.identity-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mock-avatar {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  place-items: center;
  border: 2px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  color: #ffffff;
  font-size: 14px;
  font-weight: 900;
  box-shadow: 0 6px 14px rgba(89, 86, 180, 0.24);
}

.image-avatar {
  display: block;
  object-fit: cover;
}

.identity-cell strong,
.identity-cell small {
  display: block;
}

.identity-cell strong {
  color: #31419b;
  font-size: 13px;
}

.identity-cell small {
  color: #6772b8;
  font-size: 12px;
}

.role-tag,
.status-tag {
  display: inline-flex;
  height: 24px;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.role-tag {
  color: #4771df;
  background: rgba(117, 167, 255, 0.18);
}

.role-tag.admin {
  color: #7451d8;
  background: rgba(172, 128, 255, 0.22);
}

.status-tag {
  color: #31966c;
  background: rgba(88, 210, 152, 0.18);
}

.status-tag.disabled {
  color: #d34d78;
  background: rgba(255, 139, 178, 0.22);
}

.table-actions {
  display: flex;
  gap: 8px;
}

.table-actions button {
  display: inline-flex;
  height: 25px;
  align-items: center;
  gap: 4px;
  padding: 0 9px;
  border: 1px solid rgba(122, 119, 219, 0.18);
  border-radius: 10px;
  color: #5262b8;
  background: rgba(255, 255, 255, 0.62);
  font-size: 12px;
  font-weight: 700;
}

.table-actions button:disabled,
.pager button:disabled {
  cursor: not-allowed;
  opacity: 0.48;
}

.table-actions .enable {
  color: #2d9b6f;
  background: rgba(103, 219, 159, 0.16);
}

.table-actions .danger {
  color: #d34d78;
  background: rgba(255, 139, 178, 0.16);
}

.table-state {
  display: grid;
  min-height: 180px;
  place-items: center;
  color: #5c66b2;
  font-size: 14px;
  font-weight: 800;
}

.error-state {
  color: #d34d78;
}

.pagination-row {
  display: flex;
  height: 44px;
  align-items: center;
  gap: 10px;
  color: #5260ac;
  font-size: 13px;
}

.page-size,
.page-input,
.pager button {
  height: 28px;
  border-radius: 10px;
  color: #5260ad;
  font-weight: 700;
}

.page-size {
  width: 108px;
}

.pager {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.pager button,
.page-input {
  min-width: 30px;
  padding: 0 8px;
}

.pager .current {
  color: #ffffff;
  background: linear-gradient(135deg, #6978ff, #a886ff);
}

.insight-column {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
}

.insight-card {
  padding: 18px;
  border-radius: 22px;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.card-title h3 {
  margin: 0;
  color: #ffffff;
  font-size: 17px;
  text-shadow: 0 4px 16px rgba(66, 70, 170, 0.38);
}

.card-title span {
  color: #eef2ff;
  font-size: 12px;
  font-weight: 700;
}

.insight-card {
  background:
    linear-gradient(140deg, rgba(97, 112, 255, 0.38), rgba(255, 255, 255, 0.58) 46%, rgba(255, 255, 255, 0.66)),
    rgba(255, 255, 255, 0.58);
}

.overview-body {
  display: grid;
  grid-template-columns: 118px 1fr;
  gap: 14px;
  align-items: center;
}

.donut {
  display: grid;
  width: 112px;
  height: 112px;
  place-items: center;
  border-radius: 50%;
  background: conic-gradient(#6d7cff 0 72%, #8c65ff 72% 92%, #ff8ba9 92% 100%);
  box-shadow: 0 12px 28px rgba(91, 88, 200, 0.24);
}

.donut::before {
  position: absolute;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  content: '';
  background: rgba(239, 242, 255, 0.92);
}

.donut div {
  position: relative;
  z-index: 1;
  text-align: center;
}

.donut strong,
.donut span {
  display: block;
}

.donut strong {
  color: #233198;
  font-size: 17px;
}

.donut span {
  color: #5260a8;
  font-size: 11px;
  font-weight: 800;
}

.legend-list {
  display: grid;
  gap: 9px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.legend-list li {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  column-gap: 7px;
  align-items: center;
  color: #5260ab;
  font-size: 12px;
  font-weight: 800;
}

.legend-list i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-list .normal { background: #6d7cff; }
.legend-list .admin { background: #ffad72; }
.legend-list .blocked { background: #ff819f; }

.legend-list span {
  grid-column: 2 / 4;
  color: #5360a6;
  font-weight: 500;
}

.trend-head {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  color: #5360ab;
  font-size: 12px;
  font-weight: 800;
}

.trend-head a {
  color: #7c70d8;
}

.trend-chart {
  margin-top: 8px;
  padding: 6px 4px 0;
  border-top: 1px dashed rgba(106, 112, 200, 0.22);
}

.trend-chart svg {
  display: block;
  width: 100%;
  height: 96px;
}

.chart-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  color: #6670b4;
  font-size: 10px;
  text-align: center;
}

.shortcut-card {
  padding-bottom: 16px;
}

.quick-action {
  display: grid;
  width: 100%;
  min-height: 52px;
  grid-template-columns: 32px 1fr 18px;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  padding: 9px 11px;
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 14px;
  color: #5c63b4;
  text-align: left;
  background: rgba(255, 255, 255, 0.64);
}

.quick-action > svg:first-child {
  width: 28px;
  height: 28px;
  padding: 5px;
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(135deg, #6077ff, #b170ff);
}

.quick-action strong,
.quick-action small {
  display: block;
}

.quick-action strong {
  color: #3341a0;
  font-size: 13px;
}

.quick-action small {
  margin-top: 2px;
  color: #7580bf;
  font-size: 11px;
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

.user-modal {
  width: min(430px, calc(100vw - 48px));
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 28px;
  background:
    radial-gradient(circle at 82% 10%, rgba(255, 176, 237, 0.42), transparent 30%),
    linear-gradient(140deg, rgba(255, 255, 255, 0.9), rgba(235, 235, 255, 0.78));
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

.user-modal label {
  display: grid;
  gap: 7px;
  margin-top: 12px;
  color: #4855a8;
  font-size: 13px;
  font-weight: 800;
}

.user-modal input,
.user-modal select,
.user-modal textarea {
  width: 100%;
  border: 1px solid rgba(124, 122, 218, 0.2);
  border-radius: 16px;
  outline: 0;
  padding: 0 13px;
  color: #25308c;
  background: rgba(255, 255, 255, 0.64);
}

.user-modal input,
.user-modal select {
  height: 42px;
}

.user-modal textarea {
  min-height: 82px;
  resize: vertical;
  padding-top: 11px;
  font: inherit;
}

.modal-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
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

.modal-actions .primary-action {
  margin-left: 0;
}

.detail-modal {
  width: min(500px, calc(100vw - 48px));
}

.audit-modal {
  width: min(720px, calc(100vw - 48px));
}

.audit-list {
  display: grid;
  max-height: 520px;
  gap: 10px;
  overflow: auto;
}

.audit-list article {
  padding: 12px 14px;
  border: 1px solid rgba(124, 122, 218, 0.16);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.5);
}

.audit-list strong,
.audit-list span,
.audit-list small {
  display: block;
}

.audit-list strong {
  color: #29369c;
  font-size: 14px;
}

.audit-list span {
  margin-top: 4px;
  color: #5662ae;
  font-size: 13px;
}

.audit-list small {
  margin-top: 6px;
  color: #7880be;
  font-size: 12px;
}

.detail-profile {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border: 1px solid rgba(124, 122, 218, 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.5);
}

.detail-avatar {
  width: 58px;
  height: 58px;
  border: 2px solid rgba(255, 255, 255, 0.78);
  border-radius: 20px;
  object-fit: cover;
  box-shadow: 0 10px 22px rgba(89, 86, 180, 0.24);
}

.detail-avatar.fallback {
  display: grid;
  place-items: center;
  color: #ffffff;
  font-size: 24px;
  font-weight: 900;
  background: linear-gradient(135deg, #7c8cff, #df8dff);
}

.detail-profile strong,
.detail-profile small {
  display: block;
}

.detail-profile strong {
  color: #28369c;
  font-size: 18px;
}

.detail-profile small {
  margin-top: 4px;
  color: #6a74b8;
}

.detail-list dl {
  display: grid;
  grid-template-columns: 86px 1fr;
  gap: 10px 14px;
  margin: 16px 0 0;
}

.detail-list dt,
.detail-list dd {
  min-width: 0;
  margin: 0;
  font-size: 13px;
}

.detail-list dt {
  color: #5662ae;
  font-weight: 900;
}

.detail-list dd {
  color: #26328f;
  overflow-wrap: anywhere;
}

@media (max-width: 1440px) {
  .admin-grid {
    grid-template-columns: minmax(820px, 1fr) 260px;
  }

  .filter-toolbar {
    flex-wrap: wrap;
  }

  .primary-action {
    margin-left: 0;
  }
}
</style>
