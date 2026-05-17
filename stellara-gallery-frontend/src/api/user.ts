export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

export interface UserVO {
  id: number
  userAccount: string
  userName?: string
  userAvatar?: string
  userCover?: string
  userProfile?: string
  userRole?: string
  userStatus?: number
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}

export interface AuditLogVO {
  id: number
  operatorId?: number
  operatorAccount?: string
  targetUserId?: number
  action: string
  detail?: string
  createTime?: string
}

export interface UserLoginRequest {
  userAccount: string
  userPassword: string
}

export interface UserRegisterRequest extends UserLoginRequest {
  checkPassword: string
  userName?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface UserQueryRequest {
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: 'ascend' | 'descend'
  keyword?: string
  id?: number
  userAccount?: string
  userName?: string
  userRole?: string
  userStatus?: number
}

export interface UserChangePasswordRequest {
  oldPassword: string
  newPassword: string
  checkPassword: string
}

export interface UserResetPasswordRequest {
  id: number
  newPassword: string
}

export interface UserUpdateRequest {
  id: number
  userName?: string
  userAvatar?: string
  userCover?: string
  userProfile?: string
  userRole?: string
  userStatus?: number
}

export interface UserAddRequest {
  userAccount: string
  userPassword: string
  userName?: string
  userAvatar?: string
  userCover?: string
  userProfile?: string
  userRole?: string
  userStatus?: number
}

const API_BASE = 'http://127.0.0.1:8123/api'
const LOGIN_USER_STORAGE_KEY = 'stellara-login-user'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })
  const result = (await response.json()) as BaseResponse<T>
  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export function loginUser(data: UserLoginRequest) {
  return request<UserVO>('/user/login', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function registerUser(data: UserRegisterRequest) {
  return request<number>('/user/register', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function logoutUser() {
  return request<boolean>('/user/logout', {
    method: 'POST',
  })
}

export function getLoginUser() {
  return request<UserVO>('/user/get/login')
}

export function getUserById(id: number) {
  return request<UserVO>(`/user/get?id=${encodeURIComponent(id)}`)
}

export function listUsersByPage(data: UserQueryRequest) {
  return request<PageResult<UserVO>>('/user/list/page', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function addUser(data: UserAddRequest) {
  return request<number>('/user/add', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function updateUser(data: UserUpdateRequest) {
  return request<boolean>('/user/update', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function updateMyUser(data: Omit<UserUpdateRequest, 'id'>) {
  return request<boolean>('/user/update/my', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteUser(id: number) {
  return request<boolean>('/user/delete', {
    method: 'POST',
    body: JSON.stringify({ id }),
  })
}

export function changeMyPassword(data: UserChangePasswordRequest) {
  return request<boolean>('/user/password/change', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function resetUserPassword(data: UserResetPasswordRequest) {
  return request<boolean>('/user/password/reset', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function listAuditLogs(data: { current?: number; pageSize?: number }) {
  return request<PageResult<AuditLogVO>>('/user/audit/list/page', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function cacheLoginUser(user: UserVO) {
  localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(user))
}

export function getCachedLoginUser() {
  const rawUser = localStorage.getItem(LOGIN_USER_STORAGE_KEY)
  if (!rawUser) {
    return null
  }
  try {
    return JSON.parse(rawUser) as UserVO
  } catch {
    localStorage.removeItem(LOGIN_USER_STORAGE_KEY)
    return null
  }
}

export function clearCachedLoginUser() {
  localStorage.removeItem(LOGIN_USER_STORAGE_KEY)
}
