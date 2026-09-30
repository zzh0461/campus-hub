import { http } from '@/utils/request'
import type { PageParams, PageResult } from '@/types/common'
import type {
  Product,
  ProductCategory,
  ProductPublishParams,
  ProductQuery,
  ProductSearchParams,
  ProductStatus,
  ProductUpdateParams,
} from '@/types/market'

/** 商品分类(筛选 UI 所需的补充契约,见 README) */
export function getProductCategories(): Promise<ProductCategory[]> {
  return http.get<ProductCategory[]>('/api/market/categories')
}

/** 商品分页列表 */
export function getProducts(params: ProductQuery): Promise<PageResult<Product>> {
  return http.get<PageResult<Product>>('/api/market/products', { params })
}

/** ES 搜索(内部由后端对接 Elasticsearch,前端不关心) */
export function searchProducts(params: ProductSearchParams): Promise<PageResult<Product>> {
  return http.get<PageResult<Product>>('/api/market/search', { params })
}

/** 热门商品推荐(首页) */
export function getRecommendedProducts(): Promise<Product[]> {
  return http.get<Product[]>('/api/market/products/recommend')
}

/** 我的商品 */
export function getMyProducts(params: PageParams & { status?: ProductStatus }): Promise<PageResult<Product>> {
  return http.get<PageResult<Product>>('/api/market/products/my', { params })
}

/** 商品详情 */
export function getProduct(id: number): Promise<Product> {
  return http.get<Product>(`/api/market/products/${id}`)
}

/** 发布商品 */
export function publishProduct(params: ProductPublishParams): Promise<Product> {
  return http.post<Product>('/api/market/products', params)
}

/** 编辑商品(补充契约,见 README) */
export function updateProduct(id: number, params: ProductUpdateParams): Promise<Product> {
  return http.put<Product>(`/api/market/products/${id}`, params)
}

/** 删除商品(仅卖家本人,后端做归属校验) */
export function deleteProduct(id: number): Promise<void> {
  return http.delete<void>(`/api/market/products/${id}`)
}

/** 收藏商品 */
export function favoriteProduct(id: number): Promise<{ favorite: boolean }> {
  return http.post<{ favorite: boolean }>(`/api/market/products/${id}/favorite`)
}

/** 取消收藏 */
export function unfavoriteProduct(id: number): Promise<{ favorite: boolean }> {
  return http.delete<{ favorite: boolean }>(`/api/market/products/${id}/favorite`)
}
