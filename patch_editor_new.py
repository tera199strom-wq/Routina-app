import re

file_path = "/app/applet/app/src/main/java/com/example/ui/screens/MessageEditorDialog.kt"

with open(file_path, "r") as f:
    content = f.read()

# 1. Replace section B under activeEditTab == "BUBBLE"
old_bubble_section_b = """                        // B. Pengaturan Karakter
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "2. Gambar Karakter 🐾",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Default Blue Cat
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedImageUri = null },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedImageUri == null) GreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (selectedImageUri == null) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(BlueAccent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_mascot_1),
                                                contentDescription = null,
                                                modifier = Modifier.size(30.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Kucing Biru",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (selectedImageUri == null) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(text = "Bawaan", fontSize = 10.sp, color = GrayLight)
                                        }
                                    }
                                }

                                // Gallery Picker
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedImageUri != null) BlueAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (selectedImageUri != null) BlueAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selectedImageUri != null) BlueAccent.copy(alpha = 0.2f) else GrayLight.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (selectedImageUri != null) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(Uri.parse(selectedImageUri))
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(30.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = GrayLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (selectedImageUri != null) "Foto Galeri" else "Pilih Foto",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (selectedImageUri != null) BlueAccent else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(text = if (selectedImageUri != null) "Kustom" else "Dari HP", fontSize = 10.sp, color = GrayLight)
                                        }
                                    }
                                }
                            }
                        }"""

new_bubble_section_b = """                        // B. Pengaturan Karakter
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "2. Gambar Karakter 🐾",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )

                            MascotSelectionSection(
                                selectedImageUri = selectedImageUri,
                                onImageSelected = { selectedImageUri = it },
                                onGalleryClick = { galleryLauncher.launch("image/*") }
                            )
                        }"""

content = content.replace(old_bubble_section_b, new_bubble_section_b)

# 2. Replace activeEditTab == "CHARACTER" block
old_character_tab = """                if (activeEditTab == "CHARACTER") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Pilih Gambar Karakter:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Default Blue Cat Button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedImageUri = null },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedImageUri == null) GreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (selectedImageUri == null) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BlueAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_mascot_1),
                                            contentDescription = null,
                                            modifier = Modifier.size(34.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Kucing Biru",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedImageUri == null) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Bawaan",
                                            fontSize = 11.sp,
                                            color = GrayLight
                                        )
                                    }
                                }
                            }

                            // Gallery Picker Button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedImageUri != null) BlueAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (selectedImageUri != null) BlueAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selectedImageUri != null) BlueAccent.copy(alpha = 0.2f) else GrayLight.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedImageUri != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(Uri.parse(selectedImageUri))
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier.size(34.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = GrayLight,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (selectedImageUri != null) "Foto Galeri" else "Pilih Foto",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedImageUri != null) BlueAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (selectedImageUri != null) "Kustom" else "Dari HP",
                                            fontSize = 11.sp,
                                            color = GrayLight
                                        )
                                    }
                                }
                            }
                        }"""

new_character_tab = """                if (activeEditTab == "CHARACTER") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Pilih Gambar Karakter:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        MascotSelectionSection(
                            selectedImageUri = selectedImageUri,
                            onImageSelected = { selectedImageUri = it },
                            onGalleryClick = { galleryLauncher.launch("image/*") }
                        )"""

content = content.replace(old_character_tab, new_character_tab)

# 3. Replace MascotPickerRow with MascotSelectionSection definition
picker_start = content.find("@Composable\nfun MascotPickerRow")
if picker_start != -1:
    content = content[:picker_start]

new_section_def = """@Composable
fun MascotSelectionSection(
    selectedImageUri: String?,
    onImageSelected: (String?) -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultMascots = listOf(
        "res:img_mascot_1",
        "res:img_mascot_2",
        "res:img_mascot_3",
        "res:img_mascot_4"
    )
    val isGallerySelected = selectedImageUri != null && !selectedImageUri.startsWith("res:")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tombol Pilih Foto (Persegi Panjang Agak Bulat di Atas Gambar Karakter)
        Button(
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGallerySelected) BlueAccent else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isGallerySelected) Color.White else MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(
                1.5.dp,
                if (isGallerySelected) BlueAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGallerySelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(selectedImageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Foto Galeri Terpilih ✓",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pilih Foto dari Galeri 📷",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Gambar Karakter (di bawah tombol, tanpa judul, bentuk card & agak gede biar kelihatan)
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
        ) {
            items(defaultMascots.size) { index ->
                val mascotUri = defaultMascots[index]
                val isSelected = selectedImageUri == mascotUri || (selectedImageUri == null && index == 0)

                Card(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onImageSelected(mascotUri) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val resName = mascotUri.removePrefix("res:")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_mascot_1),
                            contentDescription = "Pilih Karakter ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
"""

content = content + new_section_def

with open(file_path, "w") as f:
    f.write(content)

print("Patch applied successfully!")
