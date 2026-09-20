import re

file_path = "/app/applet/app/src/main/java/com/example/ui/screens/MascotSettingsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

pattern_img = r'if \(message\.imageUri != null\) \{\s*AsyncImage\([\s\S]*?\} else \{\s*Image\([\s\S]*?\}\s*\}'
replacement_img = """if (message.imageUri != null) {
                        if (message.imageUri.startsWith("res:")) {
                            val resName = message.imageUri.removePrefix("res:")
                            val resId = LocalContext.current.resources.getIdentifier(resName, "drawable", LocalContext.current.packageName)
                            Image(
                                painter = painterResource(id = if (resId != 0) resId else R.drawable.img_app_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .rotate(message.rotation)
                                    .scale(message.scale),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(message.imageUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .rotate(message.rotation)
                                    .scale(message.scale),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .rotate(message.rotation)
                                .scale(message.scale),
                            contentScale = ContentScale.Fit
                        )
                    }"""

content = re.sub(pattern_img, replacement_img, content, flags=re.DOTALL)
with open(file_path, "w") as f:
    f.write(content)

