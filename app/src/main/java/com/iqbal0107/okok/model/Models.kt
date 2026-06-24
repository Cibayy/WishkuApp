package com.iqbal0107.okok.model

data class WishlistItem(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val notes: String? = "",
    val photo_url: String? = null,
    val category_id: Int? = null,
    val category_name: String? = null
)

data class Category(
    val id: Int = 0,
    val name: String = "",
    val item_count: Int = 0
)

data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val photo_url: String = ""
)

data class GoogleLoginRequest(
    val id_token: String
)

data class LoginData(
    val token: String = "",
    val user: User = User()
)

data class LoginResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: LoginData? = null
)

data class WishlistListResponse(
    val success: Boolean = false,
    val data: List<WishlistItem> = emptyList()
)

data class WishlistItemResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: WishlistItem? = null
)

data class CategoryListResponse(
    val success: Boolean = false,
    val data: List<Category> = emptyList()
)

data class SimpleResponse(
    val success: Boolean = false,
    val message: String = ""
)