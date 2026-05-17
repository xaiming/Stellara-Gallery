import { request } from './user'

export interface CategoryVO {
  id: number
  name: string
  icon?: string
  sort?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface CategoryRequest {
  id?: number
  name: string
  icon?: string
  sort?: number
  status?: number
}

export function listEnabledCategories() {
  return request<CategoryVO[]>('/category/list/enabled')
}

export function listCategories() {
  return request<CategoryVO[]>('/category/list')
}

export function addCategory(data: CategoryRequest) {
  return request<number>('/category/add', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function updateCategory(data: CategoryRequest) {
  return request<boolean>('/category/update', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteCategory(id: number) {
  return request<boolean>('/category/delete', {
    method: 'POST',
    body: JSON.stringify({ id }),
  })
}
