import axios, {
  type AxiosAdapter,
  type AxiosError,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import type { ApiResponse } from '@/types/common'
import { clearAuth, getToken } from '@/utils/auth'
import { emitToast, emitUnauthorized } from '@/utils/events'
import { endLoading, startLoading } from '@/utils/loading'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'
// axios 1.8 的 defaults.adapter 是"适配器名字数组"而非函数,
// 必须用 getAdapter 解析成真正可调用的适配器,否则真实请求会直接抛异常发不出去
const defaultAdapter: AxiosAdapter = axios.getAdapter(axios.defaults.adapter ?? 'xhr')

const mockRouter: AxiosAdapter = async (config) => {
  if (USE_MOCK) {
    const { mockAdapter } = await import('@/mocks')
    return mockAdapter(config)
  }
  return defaultAdapter(config)
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  adapter: mockRouter,
})

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  startLoading()
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function handleAuthError(): void {
  clearAuth()
  emitUnauthorized()
  emitToast('warning', '登录已过期,请重新登录')
}

function routeToLogin(): void {
  void import('@/router').then(({ default: router }) => {
    if (router.currentRoute.value.path !== '/login') {
      void router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath },
      })
    }
  })
}

function resolveErrorMessage(error: AxiosError): string {
  const status = error.response?.status
  const body = error.response?.data as Partial<ApiResponse> | undefined
  if (body?.message) return body.message
  if (status === 401) return '未登录或登录已过期'
  if (status === 403) return '没有权限执行该操作'
  if (status && status >= 500) return '服务器异常,请稍后重试'
  if (error.code === 'ECONNABORTED') return '请求超时,请稍后重试'
  if (!status) return '网络连接失败,请检查后端服务是否启动'
  return '请求失败,请稍后重试'
}

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    endLoading()
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data as unknown as AxiosResponse
      }
      if (body.code === 401) {
        handleAuthError()
        routeToLogin()
      } else if (body.code === 403) {
        emitToast('error', body.message || '没有权限执行该操作')
      } else {
        emitToast('error', body.message || '请求失败,请稍后重试')
      }
      throw new Error(body.message || '请求失败')
    }
    return response
  },
  (error: AxiosError) => {
    endLoading()
    const status = error.response?.status
    if (status === 401) {
      // 登录页上的 401 是账号密码错误，直接展示后端提示，不走「登录过期」流程
      void import('@/router').then(({ default: router }) => {
        if (router.currentRoute.value.path === '/login') {
          emitToast('error', resolveErrorMessage(error))
        } else {
          handleAuthError()
          routeToLogin()
        }
      })
    } else if (status === 403) {
      emitToast('error', '没有权限执行该操作')
    } else if (status && status >= 500) {
      emitToast('error', '服务器异常,请稍后重试')
    } else {
      emitToast('error', resolveErrorMessage(error))
    }
    return Promise.reject(error)
  },
)

/** 类型化请求方法:直接返回后端 data 字段 */
export const http = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config) as Promise<T>
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config) as Promise<T>
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config) as Promise<T>
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config) as Promise<T>
  },
}

export default service
