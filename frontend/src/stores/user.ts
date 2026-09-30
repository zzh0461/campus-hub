import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getMe, updateMe } from '@/api/user'
import type { UpdateProfileParams } from '@/types/user'
import type { User } from '@/types/user'
import { useAuthStore } from '@/stores/auth'

export const useUserStore = defineStore('user', () => {
  const profile = ref<User | null>(null)
  const loading = ref(false)

  async function fetchProfile(force = false): Promise<User> {
    if (profile.value && !force) return profile.value
    loading.value = true
    try {
      const result = await getMe()
      profile.value = result
      const authStore = useAuthStore()
      if (authStore.user) authStore.updateUser(result)
      return result
    } finally {
      loading.value = false
    }
  }

  async function saveProfile(params: UpdateProfileParams): Promise<User> {
    const result = await updateMe(params)
    profile.value = result
    const authStore = useAuthStore()
    if (authStore.user) authStore.updateUser(result)
    return result
  }

  function reset(): void {
    profile.value = null
  }

  return { profile, loading, fetchProfile, saveProfile, reset }
})
