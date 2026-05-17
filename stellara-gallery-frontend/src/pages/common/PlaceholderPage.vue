<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => String(route.meta.title ?? '星璃云图'))
const subtitle = computed(() => String(route.meta.subtitle ?? '当前页面为占位内容，后续再补充业务功能。'))
</script>

<template>
  <section class="page-card placeholder-card">
    <span class="page-kicker">Coming Soon</span>
    <h2>{{ title }}</h2>
    <p>{{ subtitle }}</p>
  </section>
</template>
