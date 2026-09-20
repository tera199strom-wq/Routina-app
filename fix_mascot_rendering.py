import re

# 1. Patch MascotRepository.kt
repo_path = "/app/applet/app/src/main/java/com/example/data/repository/MascotRepository.kt"
with open(repo_path, "r") as f:
    repo_code = f.read()
repo_code = repo_code.replace("imageUri = null", 'imageUri = "res:img_mascot_1"')
with open(repo_path, "w") as f:
    f.write(repo_code)

# 2. Patch MascotDialogueComponent.kt
dialogue_path = "/app/applet/app/src/main/java/com/example/ui/components/MascotDialogueComponent.kt"
with open(dialogue_path, "r") as f:
    dialogue_code = f.read()

old_dialogue_img = """        if (!imageUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(imageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Custom Mascot Character",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(sizeDp.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_app_logo),
                contentDescription = "Blue Cat Mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(sizeDp.dp)
            )
        }"""

new_dialogue_img = """        val context = LocalContext.current
        if (!imageUri.isNullOrBlank()) {
            if (imageUri.startsWith("res:")) {
                val resName = imageUri.removePrefix("res:")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                Image(
                    painter = painterResource(id = if (resId != 0) resId else R.drawable.img_mascot_1),
                    contentDescription = "Mascot Character",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(sizeDp.dp)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Custom Mascot Character",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(sizeDp.dp)
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_1),
                contentDescription = "Mascot Character",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(sizeDp.dp)
            )
        }"""

dialogue_code = dialogue_code.replace(old_dialogue_img, new_dialogue_img)
with open(dialogue_path, "w") as f:
    f.write(dialogue_code)

# 3. Patch MascotFloatingService.kt
service_path = "/app/applet/app/src/main/java/com/example/service/MascotFloatingService.kt"
with open(service_path, "r") as f:
    service_code = f.read()

old_service_img = """                val imageUri = currentMessage?.imageUri
                if (!imageUri.isNullOrBlank()) {
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
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "Floating Mascot",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(82.dp)
                    )
                }"""

new_service_img = """                val imageUri = currentMessage?.imageUri
                val context = LocalContext.current
                if (!imageUri.isNullOrBlank()) {
                    if (imageUri.startsWith("res:")) {
                        val resName = imageUri.removePrefix("res:")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_mascot_1),
                            contentDescription = "Floating Mascot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(82.dp)
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(imageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Floating Mascot",
                            placeholder = painterResource(id = R.drawable.img_mascot_1),
                            error = painterResource(id = R.drawable.img_mascot_1),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(82.dp)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_1),
                        contentDescription = "Floating Mascot",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(82.dp)
                    )
                }"""

service_code = service_code.replace(old_service_img, new_service_img)
with open(service_path, "w") as f:
    f.write(service_code)

# 4. Patch MascotSettingsScreen.kt fallback
settings_path = "/app/applet/app/src/main/java/com/example/ui/screens/MascotSettingsScreen.kt"
with open(settings_path, "r") as f:
    settings_code = f.read()

settings_code = settings_code.replace("R.drawable.img_app_logo", "R.drawable.img_mascot_1")
with open(settings_path, "w") as f:
    f.write(settings_code)

# 5. Patch MessageEditorDialog.kt fallbacks
editor_path = "/app/applet/app/src/main/java/com/example/ui/screens/MessageEditorDialog.kt"
with open(editor_path, "r") as f:
    editor_code = f.read()

editor_code = editor_code.replace("R.drawable.img_app_logo", "R.drawable.img_mascot_1")
editor_code = editor_code.replace("com.example.R.drawable.img_app_logo", "R.drawable.img_mascot_1")
with open(editor_path, "w") as f:
    f.write(editor_code)

print("All mascot image rendering scripts patched!")
