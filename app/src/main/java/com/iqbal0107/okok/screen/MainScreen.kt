@file:Suppress("DEPRECATION")

package com.iqbal0107.okok.screen

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.iqbal0107.okok.R
import com.iqbal0107.okok.model.GoogleLoginRequest
import com.iqbal0107.okok.model.User
import com.iqbal0107.okok.model.WishlistItem
import com.iqbal0107.okok.network.UserDataStore
import com.iqbal0107.okok.network.WishkuApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private const val WEB_CLIENT_ID =
    "57316136151-nr7s9e1c1mhogtnsq7oalr0fp0b33jgu.apps.googleusercontent.com"

// Palet warna untuk kategori item
private val categoryColors = listOf(
    Color(0xFFEEEDFE) to Color(0xFF534AB7), // Ungu
    Color(0xFFE1F5EE) to Color(0xFF0F6E56), // Teal
    Color(0xFFFAECE7) to Color(0xFF993C1D), // Coral
    Color(0xFFFAEEDA) to Color(0xFF854F0B), // Amber
)

@Suppress("DEPRECATION")
private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp${format.format(amount.toLong())}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val token by dataStore.tokenFlow.collectAsState(initial = "")
    val user by dataStore.userFlow.collectAsState(initial = User())

    val viewModel: MainViewModel = viewModel()
    var showDialog by remember { mutableStateOf(false) }
    var showWishlistDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WishlistItem?>(null) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showWishlistDialog = true
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            viewModel.retrieveData(token)
            viewModel.retrieveCategories(token)
        } else {
            viewModel.clearData()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F7F5),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                ),
                title = {
                    // Teks logo dengan warna split menggunakan Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Wish",
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A1A),
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "ku",
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            color = Color(0xFF7F77DD),
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        } else {
                            showDialog = true
                        }
                    }) {
                        if (user.email.isNotEmpty()) {
                            // Avatar dengan inisial user
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEDFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(2).uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF534AB7)
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.account_circle_24),
                                contentDescription = "Profil",
                                tint = Color(0xFF888780)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (user.email.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val options = CropImageContractOptions(
                            null, CropImageOptions(
                                imageSourceIncludeGallery = false,
                                imageSourceIncludeCamera = true,
                                fixAspectRatio = true
                            )
                        )
                        launcher.launch(options)
                    },
                    containerColor = Color(0xFF534AB7),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Wishlist")
                }
            }
        }
    ) { innerPadding ->
        ScreenContent(
            viewModel = viewModel,
            token = token,
            onEdit = { editingItem = it },
            modifier = Modifier.padding(innerPadding)
        )

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false },
                onConfirmation = {
                    CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore, token) }
                    showDialog = false
                }
            )
        }

        if (showWishlistDialog) {
            WishlistDialog(
                bitmap = bitmap,
                categories = viewModel.categories.value,
                onDismissRequest = { showWishlistDialog = false },
                onConfirmation = { nama, harga, catatan, categoryId ->
                    viewModel.saveData(token, nama, harga, catatan, categoryId, bitmap)
                    showWishlistDialog = false
                }
            )
        }

        editingItem?.let { item ->
            EditWishlistDialog(
                item = item,
                categories = viewModel.categories.value,
                onDismissRequest = { editingItem = null },
                onConfirmation = { nama, harga, catatan, categoryId ->
                    viewModel.updateData(token, item.id, nama, harga, catatan, categoryId, null)
                    editingItem = null
                }
            )
        }
    }
}

@Composable
fun ScreenContent(
    viewModel: MainViewModel,
    token: String,
    onEdit: (WishlistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }

    when (status) {
        ApiStatus.LOADING -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF534AB7))
            }
        }

        ApiStatus.SUCCESS -> {
            Column(modifier = modifier.fillMaxSize()) {

                // --- HERO BUDGET CARD ---
                if (data.isNotEmpty()) {
                    val totalBudget = data.sumOf { it.price }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF534AB7), Color(0xFF7F77DD))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL BUDGET DIBUTUHKAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xB3FFFFFF),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatRupiah(totalBudget),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${data.size} item di wishlist kamu",
                                    fontSize = 12.sp,
                                    color = Color(0x99FFFFFF)
                                )
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0x33FFFFFF)
                                ) {
                                    Text(
                                        text = "💜 Wishku",
                                        fontSize = 11.sp,
                                        color = Color(0xEEFFFFFF),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- FILTER CHIP KATEGORI ---
                val categories = listOf("Semua") + data.mapNotNull { it.category_name }.distinct()
                val filteredData = if (selectedCategory == "Semua") data
                else data.filter { it.category_name == selectedCategory }

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isActive = cat == selectedCategory
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActive) Color(0xFF534AB7) else Color(0xFFF1EFE8)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isActive) Color.White else Color(0xFF888780),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WISHLIST KAMU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF888780),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // --- GRID ITEM ---
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp)
                ) {
                    items(filteredData) { item ->
                        ListItem(
                            item = item,
                            colorIndex = data.indexOf(item) % categoryColors.size,
                            onEdit = { onEdit(item) },
                            onDelete = { viewModel.deleteData(token, item.id) }
                        )
                    }
                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Gagal memuat data..",
                    fontSize = 15.sp,
                    color = Color(0xFF5F5E5A)
                )
                Text(
                    "Periksa koneksi internetmu",
                    fontSize = 13.sp,
                    color = Color(0xFF888780),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Button(
                    onClick = { viewModel.retrieveData(token) },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF534AB7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Coba lagi")
                }
            }
        }
    }
}

@Composable
fun ListItem(
    item: WishlistItem,
    colorIndex: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    val (bgColor, textColor) = categoryColors[colorIndex]

    val baseUrl = "https://wishku-production.up.railway.app/"
    val fullImageUrl = item.photo_url?.let { path ->
        when {
            path.startsWith("http") -> path
            path.startsWith("storage/") -> baseUrl + path
            else -> baseUrl + "storage/" + path
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x12000000)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Gambar / placeholder berwarna
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                if (fullImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(fullImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.broken_img),
                        error = painterResource(R.drawable.broken_img),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.broken_img),
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onEdit() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x59000000))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Ubah",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = { showConfirm = true },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x59000000))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Info item
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = formatRupiah(item.price),
                    fontSize = 12.sp,
                    color = Color(0xFF534AB7),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // --- PENAMBAHAN TAMPILAN CATATAN ---
                if (!item.notes.isNullOrEmpty()) {
                    Text(
                        text = item.notes,
                        fontSize = 11.sp,
                        color = Color(0xFF888780),
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // ------------------------------------

                item.category_name?.let { cat ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = bgColor,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 10.sp,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Hapus item",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            },
            text = {
                Text(
                    "Yakin ingin menghapus \"${item.name}\" dari wishlist?",
                    color = Color(0xFF5F5E5A),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFA32D2D))
                ) {
                    Text("Hapus", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF888780))
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        android.util.Log.e("IMAGE", "Error: ${result.error}")
        return null
    }
    val uri = result.uriContent ?: return null
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        android.util.Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val response = WishkuApi.service.loginGoogle(GoogleLoginRequest(googleId.idToken))
            if (response.success && response.data != null) {
                dataStore.saveSession(response.data.token, response.data.user)
            }
        } catch (e: GoogleIdTokenParsingException) {
            android.util.Log.e("SIGN-IN", "Error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore, token: String) {
    try {
        if (token.isNotEmpty()) {
            WishkuApi.service.logout("Bearer $token")
        }
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        dataStore.clearSession()
    } catch (e: ClearCredentialException) {
        android.util.Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    } catch (e: Exception) {
        android.util.Log.e("SIGN-IN", "Error: ${e.message}")
    }
}