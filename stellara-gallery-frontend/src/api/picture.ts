import { API_BASE, request, type BaseResponse, type PageResult } from './user'

export interface PictureVO {
  id: number
  url: string
  thumbnailUrl?: string
  name: string
  introduction?: string
  categoryId?: number
  categoryName?: string
  tags?: string[]
  picSize?: number
  picWidth?: number
  picHeight?: number
  picScale?: number
  picFormat?: string
  picColor?: string
  userId: number
  userName?: string
  userAvatar?: string
  spaceId?: number
  isPublic: number
  reviewStatus: number
  reviewMessage?: string
  reviewerId?: number
  reviewTime?: string
  viewCount: number
  likeCount: number
  favoriteCount: number
  downloadCount: number
  liked?: boolean
  favorited?: boolean
  createTime?: string
  updateTime?: string
}

export interface PictureQueryRequest {
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: 'ascend' | 'descend'
  keyword?: string
  categoryId?: number
  tag?: string
  userId?: number
  spaceId?: number
  isPublic?: number
  reviewStatus?: number
}

export interface PictureUpdateRequest {
  id: number
  name: string
  introduction?: string
  categoryId?: number
  tags?: string[]
  isPublic?: number
}

export interface PictureReviewRequest {
  id: number
  reviewStatus: number
  reviewMessage?: string
}

export interface PictureUploadForm {
  file: File
  name: string
  introduction?: string
  categoryId?: number
  tags?: string[]
  isPublic?: number
}

async function uploadRequest<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  })
  const result = (await response.json()) as BaseResponse<T>
  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export function uploadPicture(data: PictureUploadForm) {
  const formData = new FormData()
  formData.append('file', data.file)
  formData.append('name', data.name)
  formData.append('introduction', data.introduction ?? '')
  if (data.categoryId) {
    formData.append('categoryId', String(data.categoryId))
  }
  formData.append('tags', JSON.stringify(data.tags ?? []))
  formData.append('isPublic', String(data.isPublic ?? 0))
  return uploadRequest<PictureVO>('/picture/upload', formData)
}

export function getPictureById(id: number) {
  return request<PictureVO>(`/picture/get?id=${encodeURIComponent(id)}`)
}

export function listPublicPictures(data: PictureQueryRequest) {
  return request<PageResult<PictureVO>>('/picture/list/public/page', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function listMyPictures(data: PictureQueryRequest) {
  return request<PageResult<PictureVO>>('/picture/list/my/page', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function listAdminPictures(data: PictureQueryRequest) {
  return request<PageResult<PictureVO>>('/picture/list/admin/page', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function listFavoritePictures(data: PictureQueryRequest) {
  return request<PageResult<PictureVO>>('/picture/favorite/list/page', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function updatePicture(data: PictureUpdateRequest) {
  return request<boolean>('/picture/update', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deletePicture(id: number) {
  return request<boolean>('/picture/delete', {
    method: 'POST',
    body: JSON.stringify({ id }),
  })
}

export function reviewPicture(data: PictureReviewRequest) {
  return request<boolean>('/picture/review', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function batchReviewPicture(ids: number[], reviewStatus: number, reviewMessage?: string) {
  return request<boolean>('/picture/review/batch', {
    method: 'POST',
    body: JSON.stringify({ ids, reviewStatus, reviewMessage }),
  })
}

export function toggleLike(id: number) {
  return request<boolean>('/picture/like', {
    method: 'POST',
    body: JSON.stringify({ id }),
  })
}

export function toggleFavorite(id: number) {
  return request<boolean>('/picture/favorite', {
    method: 'POST',
    body: JSON.stringify({ id }),
  })
}

export function downloadPicture(id: number) {
  return request<string>(`/picture/download?id=${encodeURIComponent(id)}`)
}
