import { http } from '@/utils/request'
import type { LoginParams, LoginResult, RegisterParams } from '@/types/auth'
import type { User } from '@/types/user'

/** 登录 */
export function login(params: LoginParams): Promise<LoginResult> {
  return http.post<LoginResult>('/api/auth/login', params)
}

/** 注册 */
export function register(params: RegisterParams): Promise<User> {
  return http.post<User>('/api/auth/register', params)
}
