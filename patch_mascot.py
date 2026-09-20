import re

# Update MascotDialogueComponent.kt
file_path = "/app/applet/app/src/main/java/com/example/ui/components/MascotDialogueComponent.kt"
with open(file_path, "r") as f:
    content = f.read()

pattern_img = r'if \(imageUri != null\) \{\s*AsyncImage\([\s\S]*?\} else \{\s*Image\([\s\S]*?\}\s*\}'
replacement_img = """if (imageUri != null) {
            if (imageUri.startsWith("res:")) {
                val resName = imageUri.removePrefix("res:")
                val resId = LocalContext.current.resources.getIdentifier(resName, "drawable", LocalContext.current.packageName)
                Image(
                    painter = painterResource(id = if (resId != 0) resId else R.drawable.img_app_logo),
                    contentDescription = "Mascot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(sizeDp.dp)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Mascot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(sizeDp.dp)
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_app_logo),
                contentDescription = "Mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(sizeDp.dp)
            )
        }"""
content = re.sub(pattern_img, replacement_img, content, flags=re.DOTALL)
with open(file_path, "w") as f:
    f.write(content)

# Update MascotFloatingService.kt
file_path2 = "/app/applet/app/src/main/java/com/example/service/MascotFloatingService.kt"
with open(file_path2, "r") as f:
    content2 = f.read()

pattern_img2 = r'if \(imageUri != null\) \{\s*AsyncImage\([\s\S]*?\} else \{\s*Image\([\s\S]*?\}\s*\}'
replacement_img2 = """if (imageUri != null) {
                    if (imageUri.startsWith("res:")) {
                        val resName = imageUri.removePrefix("res:")
                        val resId = LocalContext.current.resources.getIdentifier(resName, "drawable", LocalContext.current.packageName)
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_app_logo),
                            contentDescription = "Floating Mascot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(82.dp)
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(imageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Floating Mascot",
                            placeholder = painterResource(id = R.drawable.img_app_logo),
                            error = painterResource(id = R.drawable.img_app_logo),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(82.dp)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "Floating Mascot",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(82.dp)
                    )
                }"""
content2 = re.sub(pattern_img2, replacement_img2, content2, flags=re.DOTALL)
with open(file_path2, "w") as f:
    f.write(content2)

print("Patched rendering logic")
