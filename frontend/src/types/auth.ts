import type { User } from './user'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  nickname: string
  phone: string
}

export interface LoginResult {
  token: string
  tokenType: string
  user: User
}
