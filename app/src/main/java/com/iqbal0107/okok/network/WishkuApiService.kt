package com.iqbal0107.okok.network

import com.iqbal0107.okok.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://wishku-production.up.railway.app/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface WishkuApiService {

    @POST("auth/google")
    suspend fun loginGoogle(@Body request: GoogleLoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): SimpleResponse

    @GET("wishlist")
    suspend fun getWishlist(@Header("Authorization") token: String): WishlistListResponse

    @Multipart
    @POST("wishlist")
    suspend fun addWishlist(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("notes") notes: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part photo: MultipartBody.Part?
    ): WishlistItemResponse

    @Multipart
    @POST("wishlist/{id}")
    suspend fun updateWishlist(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("notes") notes: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part photo: MultipartBody.Part?
    ): WishlistItemResponse

    @DELETE("wishlist/{id}")
    suspend fun deleteWishlist(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): SimpleResponse

    @GET("categories")
    suspend fun getCategories(@Header("Authorization") token: String): CategoryListResponse
}

object WishkuApi {
    val service: WishkuApiService by lazy {
        retrofit.create(WishkuApiService::class.java)
    }
}