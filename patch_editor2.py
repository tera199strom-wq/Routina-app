import re

file_path = "/app/applet/app/src/main/java/com/example/ui/screens/MessageEditorDialog.kt"

with open(file_path, "r") as f:
    content = f.read()

helper_composable = """
@Composable
fun MascotPickerRow(
    selectedImageUri: String?,
    onImageSelected: (String?) -> Unit,
    onGalleryClick: () -> Unit
) {
    val defaultMascots = listOf(
        Pair("res:img_mascot_1", "Desain 1"),
        Pair("res:img_mascot_2", "Desain 2"),
        Pair("res:img_mascot_3", "Desain 3"),
        Pair("res:img_mascot_4", "Desain 4")
    )
    val isGallerySelected = selectedImageUri != null && !selectedImageUri.startsWith("res:")

    androidx.compose.foundation.lazy.LazyRow(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        items(defaultMascots.size) { index ->
            val mascot = defaultMascots[index]
            val isSelected = selectedImageUri == mascot.first || (selectedImageUri == null && index == 0)
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier
                    .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .androidx.compose.foundation.clickable { onImageSelected(mascot.first) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = if (isSelected) com.example.ui.theme.GreenPrimary.copy(alpha = 0.12f) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) com.example.ui.theme.GreenPrimary else androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier.padding(10.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .androidx.compose.foundation.layout.size(40.dp)
                            .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .androidx.compose.foundation.background(com.example.ui.theme.BlueAccent.copy(alpha = 0.2f)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        val resId = androidx.compose.ui.platform.LocalContext.current.resources.getIdentifier(mascot.first.removePrefix("res:"), "drawable", androidx.compose.ui.platform.LocalContext.current.packageName)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = if (resId != 0) resId else com.example.R.drawable.img_app_logo),
                            contentDescription = null,
                            modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.size(34.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.width(10.dp))
                    androidx.compose.foundation.layout.Column {
                        androidx.compose.material3.Text(
                            text = mascot.second,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) com.example.ui.theme.GreenPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        androidx.compose.material3.Text(text = "Bawaan", fontSize = 11.sp, color = com.example.ui.theme.GrayLight)
                    }
                }
            }
        }

        // Gallery Picker
        item {
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier
                    .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .androidx.compose.foundation.clickable { onGalleryClick() },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = if (isGallerySelected) com.example.ui.theme.BlueAccent.copy(alpha = 0.12f) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isGallerySelected) com.example.ui.theme.BlueAccent else androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier.padding(10.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .androidx.compose.foundation.layout.size(40.dp)
                            .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .androidx.compose.foundation.background(if (isGallerySelected) com.example.ui.theme.BlueAccent.copy(alpha = 0.2f) else com.example.ui.theme.GrayLight.copy(alpha = 0.2f)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        if (isGallerySelected) {
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(android.net.Uri.parse(selectedImageUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.size(34.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Image,
                                contentDescription = null,
                                tint = com.example.ui.theme.GrayLight,
                                modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.size(20.dp)
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.width(10.dp))
                    androidx.compose.foundation.layout.Column {
                        androidx.compose.material3.Text(
                            text = "Galeri",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isGallerySelected) com.example.ui.theme.BlueAccent else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        androidx.compose.material3.Text(text = "Upload Gambar", fontSize = 11.sp, color = com.example.ui.theme.GrayLight)
                    }
                }
            }
        }
    }
}
"""

# Let's find exactly the blocks to replace.
# 1. Custom mode character block
target1 = """Row(
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
                                                painter = painterResource(id = R.drawable.img_app_logo),
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
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Galeri",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (selectedImageUri != null) BlueAccent else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(text = "Upload Gambar", fontSize = 10.sp, color = GrayLight)
                                        }
                                    }
                                }
                            }"""

# 2. Preset mode character block
target2 = """Row(
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
                                            painter = painterResource(id = R.drawable.img_app_logo),
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
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Galeri",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedImageUri != null) BlueAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Upload Gambar",
                                            fontSize = 11.sp,
                                            color = GrayLight
                                        )
                                    }
                                }
                            }
                        }"""

replacement = 'MascotPickerRow(selectedImageUri = selectedImageUri, onImageSelected = { selectedImageUri = it }, onGalleryClick = { galleryLauncher.launch("image/*") })'

# We do a basic text replacement after stripping space differences
def standardize(text):
    return re.sub(r'\s+', ' ', text)

content_std = standardize(content)
target1_std = standardize(target1)
target2_std = standardize(target2)

if target1_std in content_std:
    print("Found target 1")
    # let's just do a manual replace using the exact strings by reading line by line
else:
    print("Could not find target 1")

