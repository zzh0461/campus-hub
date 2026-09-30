import { http } from '@/utils/request'
import type { UploadResult } from '@/types/common'

/** 图片上传:multipart/form-data 字段名为 file（挂在 market 路由下,复用现有网关路由） */
export function uploadImage(file: File): Promise<UploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<UploadResult>('/api/market/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 删除已上传的图片文件(用于表单移除缩略图时清理临时文件) */
export function deleteImage(url: string): Promise<void> {
  const filename = url.substring(url.lastIndexOf('/') + 1)
  return http.delete<void>(`/api/market/uploads/${filename}`)
}