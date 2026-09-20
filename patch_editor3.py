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
                                    .data(android.net.Uri.parse(selectedImageUri!!))
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

replacement = 'MascotPickerRow(selectedImageUri = selectedImageUri, onImageSelected = { selectedImageUri = it }, onGalleryClick = { galleryLauncher.launch("image/*") })'

lines = content.split('\n')
new_lines = []
skip = False
for i, line in enumerate(lines):
    if skip:
        if "                            }" in line and len(line) == 29:
            # check if it's the end of the Gallery Picker card
            if i > 2 and "                                }" in lines[i-1]:
               skip = False
        continue

    if "Row(" in line and "modifier = Modifier.fillMaxWidth()" in lines[i+1] and "horizontalArrangement = Arrangement.spacedBy(10.dp)" in lines[i+2]:
        if "// Default Blue Cat" in lines[i+4] or "// Default Blue Cat Button" in lines[i+4]:
            new_lines.append("                            " + replacement)
            skip = True
            continue

    new_lines.append(line)

final_content = "\n".join(new_lines) + "\n" + helper_composable
with open(file_path, "w") as f:
    f.write(final_content)

print("Safely patched")
