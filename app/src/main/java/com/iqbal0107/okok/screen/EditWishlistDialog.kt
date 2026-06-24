package com.iqbal0107.okok.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.iqbal0107.okok.model.Category
import com.iqbal0107.okok.model.WishlistItem

@Composable
fun EditWishlistDialog(
    item: WishlistItem,
    categories: List<Category>,
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String, price: String, notes: String, categoryId: Int) -> Unit
) {
    var nama by remember { mutableStateOf(item.name) }
    var harga by remember { mutableStateOf(item.price.toInt().toString()) }
    var catatan by remember { mutableStateOf(item.notes ?: "") }
    var selectedCategory by remember {
        mutableStateOf(categories.find { it.id == item.category_id })
    }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Ubah Wishlist", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Barang") },
                    maxLines = 1,
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it },
                    label = { Text("Harga") },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") },
                    maxLines = 2,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Box(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedCategory?.name ?: "Pilih Kategori")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Batal")
                    }
                    OutlinedButton(
                        onClick = {
                            onConfirmation(nama, harga, catatan, selectedCategory?.id ?: 0)
                        },
                        enabled = nama.isNotEmpty() && harga.isNotEmpty(),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}