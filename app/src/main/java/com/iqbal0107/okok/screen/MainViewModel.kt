package com.iqbal0107.okok.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbal0107.okok.model.Category
import com.iqbal0107.okok.model.WishlistItem
import com.iqbal0107.okok.network.WishkuApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

enum class ApiStatus { LOADING, SUCCESS, FAILED }

class MainViewModel : ViewModel() {

    var data = mutableStateOf<List<WishlistItem>>(emptyList())
        private set

    var categories = mutableStateOf<List<Category>>(emptyList())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val result = WishkuApi.service.getWishlist("Bearer $token")
                data.value = result.data
                android.util.Log.d("WISHLIST", "Data: ${result.data}")
                // DEBUG TAMBAHAN
                result.data.forEach { item ->
                    Log.d("API_DEBUG", "id=${item.id} name=${item.name} photo_url=${item.photo_url}")
                }
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.e("API_DEBUG", "retrieveData GAGAL: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun retrieveCategories(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = WishkuApi.service.getCategories("Bearer $token")
                categories.value = result.data
            } catch (e: Exception) {
            }
        }
    }

    fun saveData(
        token: String,
        name: String,
        price: String,
        notes: String,
        categoryId: Int,
        bitmap: Bitmap?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // DEBUG TAMBAHAN
            Log.d("API_DEBUG", "=== saveData ===")
            Log.d("API_DEBUG", "bitmap ada: ${bitmap != null}")
            try {
                val photoPart = bitmap?.toMultipartBody()
                Log.d("API_DEBUG", "photoPart ada: ${photoPart != null}")
                val result = WishkuApi.service.addWishlist(
                    "Bearer $token",
                    name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    price.toRequestBody("text/plain".toMediaTypeOrNull()),
                    notes.toRequestBody("text/plain".toMediaTypeOrNull()),
                    categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    photoPart
                )
                Log.d("API_DEBUG", "saveData success=${result.success} photo_url=${result.data?.photo_url}")
                if (result.success) retrieveData(token)
                else errorMessage.value = "Error: ${result.message}"
            } catch (e: Exception) {
                Log.e("API_DEBUG", "saveData EXCEPTION: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun updateData(
        token: String,
        id: Int,
        name: String,
        price: String,
        notes: String,
        categoryId: Int,
        bitmap: Bitmap?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = WishkuApi.service.updateWishlist(
                    "Bearer $token",
                    id,
                    "PUT".toRequestBody("text/plain".toMediaTypeOrNull()),
                    name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    price.toRequestBody("text/plain".toMediaTypeOrNull()),
                    notes.toRequestBody("text/plain".toMediaTypeOrNull()),
                    categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap?.toMultipartBody()
                )
                if (result.success) retrieveData(token)
                else errorMessage.value = "Error: ${result.message}"
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(token: String, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = WishkuApi.service.deleteWishlist("Bearer $token", id)
                if (result.success) retrieveData(token)
                else errorMessage.value = "Error: ${result.message}"
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        Log.d("API_DEBUG", "toMultipartBody ukuran: ${byteArray.size} bytes")
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size
        )
        return MultipartBody.Part.createFormData("photo", "photo.jpg", requestBody)
    }

    fun clearMessage() {
        errorMessage.value = null
    }

    fun clearData() {
        data.value = emptyList()
        categories.value = emptyList()
        status.value = ApiStatus.SUCCESS
    }
}