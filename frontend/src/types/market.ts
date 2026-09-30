import type { PageParams } from './common'

export type ProductStatus = 'ON_SALE' | 'OFF_SHELF' | 'SOLD'

export type ProductSort = 'latest' | 'priceAsc' | 'priceDesc'

export interface ProductCategory {
  id: number
  name: string
}

export interface Product {
  id: number
  title: string
  categoryId: number
  categoryName: string
  price: number
  originalPrice: number
  description: string
  images: string[]
  sellerId: number
  sellerName: string
  sellerAvatar: string
  status: ProductStatus
  favoriteCount: number
  viewCount: number
  favorite: boolean
  createdAt: string
}

export interface ProductQuery extends PageParams {
  keyword?: string
  categoryId?: number
  minPrice?: number
  maxPrice?: number
  sort?: ProductSort
}

export interface ProductSearchParams extends PageParams {
  keyword: string
  categoryId?: number
  sort?: ProductSort
}

export interface ProductPublishParams {
  title: string
  categoryId: number
  price: number
  description: string
  images: string[]
}

export interface ProductUpdateParams extends ProductPublishParams {
  status?: ProductStatus
}
