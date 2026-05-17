import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import GalleryPage from '../pages/gallery/GalleryPage.vue'
import LoginPage from '../pages/login/LoginPage.vue'
import PlaceholderPage from '../pages/common/PlaceholderPage.vue'
import UserAdminPage from '../pages/admin/UserAdminPage.vue'
import ProfilePage from '../pages/profile/ProfilePage.vue'
import { cacheLoginUser, clearCachedLoginUser, getCachedLoginUser, getLoginUser } from '../api/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: LoginPage,
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: BasicLayout,
      redirect: '/gallery',
      children: [
        {
          path: 'gallery',
          name: 'Gallery',
          component: GalleryPage,
          meta: { title: '公共图库' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: ProfilePage,
          meta: { title: '个人主页' },
        },
        {
          path: 'space/my',
          name: 'MySpace',
          component: PlaceholderPage,
          meta: { title: '我的空间', subtitle: '这里将展示个人空间、私有图片和创作合集。' },
        },
        {
          path: 'space/team',
          name: 'TeamSpace',
          component: PlaceholderPage,
          meta: { title: '星域空间', subtitle: '这里将展示团队空间和协作图库。' },
        },
        {
          path: 'favorites',
          name: 'Favorites',
          component: PlaceholderPage,
          meta: { title: '收藏夹', subtitle: '这里将展示收藏图片和灵感分组。' },
        },
        {
          path: 'recycle',
          name: 'Recycle',
          component: PlaceholderPage,
          meta: { title: '回收站', subtitle: '这里将管理已删除图片的恢复与清理。' },
        },
        {
          path: 'admin/user',
          name: 'UserAdmin',
          component: UserAdminPage,
          meta: { title: '用户管理', subtitle: '管理平台用户、角色、状态与空间信息', requiresAdmin: true },
        },
        {
          path: 'admin/picture',
          name: 'PictureAdmin',
          component: PlaceholderPage,
          meta: { title: '图片管理', subtitle: '这里将提供图片审核、编辑和批量管理入口。', requiresAdmin: true },
        },
        {
          path: 'admin/category',
          name: 'CategoryAdmin',
          component: PlaceholderPage,
          meta: { title: '分类管理', subtitle: '这里将维护图库分类体系。', requiresAdmin: true },
        },
        {
          path: 'admin/tag',
          name: 'TagAdmin',
          component: PlaceholderPage,
          meta: { title: '标签管理', subtitle: '这里将维护图片标签和检索关键词。', requiresAdmin: true },
        },
        {
          path: 'admin/space',
          name: 'SpaceAdmin',
          component: PlaceholderPage,
          meta: { title: '空间管理', subtitle: '这里将管理用户空间、容量和权限。', requiresAdmin: true },
        },
        {
          path: 'admin/settings',
          name: 'SystemSettings',
          component: PlaceholderPage,
          meta: { title: '系统设置', subtitle: '这里将配置站点基础信息与系统参数。', requiresAdmin: true },
        },
        {
          path: 'viewer/:pictureId?',
          name: 'Viewer',
          component: PlaceholderPage,
          meta: { title: '观赏模式', subtitle: '这里将提供沉浸式图片浏览体验。' },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  if (!to.matched.some((record) => record.meta.requiresAdmin)) {
    return true
  }
  const cachedUser = getCachedLoginUser()
  if (cachedUser?.userRole === 'admin') {
    return true
  }
  try {
    const loginUser = await getLoginUser()
    cacheLoginUser(loginUser)
    return loginUser.userRole === 'admin' ? true : '/gallery'
  } catch {
    clearCachedLoginUser()
    return '/login'
  }
})

export default router
