import { request } from './user'

export interface TagVO {
  id: number
  name: string
  color?: string
  useCount?: number
  createTime?: string
  updateTime?: string
}

export interface TagRequest {
  id?: number
  name: string
  color?: string
}

export function listEnabledTags() {
  return request<TagVO[]>('/tag/list/enabled')
}

export function listTags() {
  return request<TagVO[]>('/tag/list')
}

export function addTag(data: TagRequest) {
  return request<number>('/tag/add', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function updateTag(data: TagRequest) {
  return request<boolean>('/tag/update', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteTag(id: number) {
  return request<boolean>('/tag/delete', {
    method: 'POST',
    body: JSON.stringify({ id }),
  })
}
