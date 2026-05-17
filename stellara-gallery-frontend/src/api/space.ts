import { request } from './user'

export interface SpaceVO {
  id: number
  spaceName: string
  spaceType: number
  spaceLevel: number
  userId: number
  maxSize: number
  maxCount: number
  totalSize: number
  totalCount: number
  spaceStatus: number
  createTime?: string
  updateTime?: string
}

export function getMySpace() {
  return request<SpaceVO>('/space/my')
}
