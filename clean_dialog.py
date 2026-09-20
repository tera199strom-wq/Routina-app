file_path = "/app/applet/app/src/main/java/com/example/ui/screens/MessageEditorDialog.kt"

with open(file_path, "r") as f:
    content = f.read()

# Keep content up to line `@Composable\nfun MascotSelectionSection(` (first occurrence) + its full body, remove duplicate
first_idx = content.find("@Composable\nfun MascotSelectionSection")
second_idx = content.find("@Composable\nfun MascotSelectionSection", first_idx + 1)

if second_idx != -1:
    content = content[:second_idx]

with open(file_path, "w") as f:
    f.write(content)

print("Duplicates cleaned!")
