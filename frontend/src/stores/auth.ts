import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '@/api/auth'
import type { LoginParams, RegisterParams } from '@/types/auth'
import type { User } from '@/types/user'
import {
  clearAuth,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
} from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<User | null>(getStoredUser())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const displayName = computed(() => user.value?.nickname || user.value?.username || '未登录')
  const avatar = computed(() => user.value?.avatar || '')

  function setSession(newToken: string, newUser: User): void {
    token.value = newToken
    user.value = newUser
    setToken(newToken)
    setStoredUser(newUser)
  }

  function updateUser(newUser: User): void {
    user.value = newUser
    setStoredUser(newUser)
  }

  async function login(params: LoginParams): Promise<User> {
    const result = await loginApi(params)
    setSession(result.token, result.user)
    return result.user
  }

  async function register(params: RegisterParams): Promise<User> {
    return registerApi(params)
  }

  function logout(): void {
    clearAuth()
    token.value = null
    user.value = null
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    displayName,
    avatar,
    login,
    register,
    logout,
    setSession,
    updateUser,
  }
})
