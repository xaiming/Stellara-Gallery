<script setup lang="ts">
import {
  AppstoreOutlined,
  CloudOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  FolderOpenOutlined,
  HeartOutlined,
  HomeOutlined,
  PictureOutlined,
  SettingOutlined,
  TagsOutlined,
  TeamOutlined,
  ThunderboltFilled,
  UserOutlined,
} from '@ant-design/icons-vue'
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import moonLogo from '../../assets/stellara-moon-logo.png'

const route = useRoute()

const menuItems = [
  { title: '公共图库', path: '/gallery', icon: HomeOutlined },
  { title: '我的空间', path: '/space/my', icon: FolderOpenOutlined },
  { title: '星域空间', path: '/space/team', icon: TeamOutlined },
  { title: '收藏夹', path: '/favorites', icon: HeartOutlined },
  { title: '回收站', path: '/recycle', icon: DeleteOutlined },
  { title: '用户管理', path: '/admin/user', icon: UserOutlined },
  { title: '图片管理', path: '/admin/picture', icon: PictureOutlined },
  { title: '分类管理', path: '/admin/category', icon: AppstoreOutlined },
  { title: '标签管理', path: '/admin/tag', icon: TagsOutlined },
  { title: '空间管理', path: '/admin/space', icon: DatabaseOutlined },
  { title: '系统设置', path: '/admin/settings', icon: SettingOutlined },
]

const isActive = (path: string) => computed(() => route.path === path)
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-logo">
        <img :src="moonLogo" alt="星璃云图月亮图标" />
      </div>
      <div>
        <h1>星璃云图</h1>
        <p>Stellara Gallery</p>
      </div>
    </div>

    <nav class="sidebar-nav" aria-label="主菜单">
      <RouterLink
        v-for="item in menuItems"
        :key="item.path"
        :class="['menu-item', { active: isActive(item.path).value }]"
        :to="item.path"
      >
        <component :is="item.icon" />
        <span>{{ item.title }}</span>
      </RouterLink>
    </nav>

    <div class="sidebar-footer">
      <RouterLink class="viewer-button" to="/viewer">
        <ThunderboltFilled />
        <span>观赏模式</span>
      </RouterLink>

      <div class="storage-card">
        <div class="storage-title">
          <CloudOutlined />
          <span>存储空间</span>
        </div>
        <div class="progress-track">
          <div class="progress-fill" />
        </div>
        <div class="storage-value">1.2 TB / 2 TB</div>
      </div>
    </div>
  </aside>
</template>
