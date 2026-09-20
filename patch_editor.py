import re

file_path = "/app/applet/app/src/main/java/com/example/ui/screens/MessageEditorDialog.kt"

with open(file_path, "r") as f:
    content = f.read()

# The helper composable we'll append to the end of the file
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(defaultMascots.size) { index ->
            val mascot = defaultMascots[index]
            val isSelected = selectedImageUri == mascot.first || (selectedImageUri == null && index == 0)
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onImageSelected(mascot.first) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlueAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val resId = LocalContext.current.resources.getIdentifier(mascot.first.removePrefix("res:"), "drawable", LocalContext.current.packageName)
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_app_logo),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = mascot.second,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Bawaan", fontSize = 11.sp, color = GrayLight)
                    }
                }
            }
        }

        // Gallery Picker
        item {
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onGalleryClick() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGallerySelected) BlueAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isGallerySelected) BlueAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isGallerySelected) BlueAccent.copy(alpha = 0.2f) else GrayLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGallerySelected) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(android.net.Uri.parse(selectedImageUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.size(34.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Image,
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
                            color = if (isGallerySelected) BlueAccent else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Upload Gambar", fontSize = 11.sp, color = GrayLight)
                    }
                }
            }
        }
    }
}
"""

# Replace the first Row layout for Mascot
# We use regex to find the Row block starting from `Row(\n                                modifier = Modifier.fillMaxWidth(),\n                                horizontalArrangement = Arrangement.spacedBy(10.dp)` up to `// Gallery Picker` and the card after it.

pattern1 = r'Row\(\s*modifier = Modifier\.fillMaxWidth\(\),\s*horizontalArrangement = Arrangement\.spacedBy\(10\.dp\)\s*\)\s*\{\s*// Default Blue Cat.*?Galeri.*?\n\s*\}\s*\}'
replacement1 = 'MascotPickerRow(selectedImageUri = selectedImageUri, onImageSelected = { selectedImageUri = it }, onGalleryClick = { galleryLauncher.launch("image/*") })'

content = re.sub(pattern1, replacement1, content, flags=re.DOTALL)

# Same for second block
pattern2 = r'Row\(\s*modifier = Modifier\.fillMaxWidth\(\),\s*horizontalArrangement = Arrangement\.spacedBy\(10\.dp\)\s*\)\s*\{\s*// Default Blue Cat Button.*?Galeri.*?\n\s*\}\s*\}'
content = re.sub(pattern2, replacement1, content, flags=re.DOTALL)

with open(file_path, "w") as f:
    f.write(content + "\n" + helper_composable)

print("Patched MessageEditorDialog.kt")
