import os

color_file = "/app/applet/app/src/main/java/com/example/ui/theme/Color.kt"
with open(color_file, "r") as f:
    content = f.read()

content = content.replace("val LightBackground = Color(0xFFFFFFFF)", "val LightBackground = Color.Transparent")
with open(color_file, "w") as f:
    f.write(content)

main_activity = "/app/applet/app/src/main/java/com/example/MainActivity.kt"
with open(main_activity, "r") as f:
    main_content = f.read()

replacement = """
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor

"""

if "import androidx.compose.foundation.background" not in main_content:
    main_content = main_content.replace("import android.os.Bundle", replacement.strip() + "\nimport android.os.Bundle")

surface_replacement = """
            RoutinaTheme(darkTheme = userSettings.isDarkMode) {
                val backgroundBrush = if (!userSettings.isDarkMode) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF7F7F7)),
                        start = Offset(Float.POSITIVE_INFINITY, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                } else {
                    SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.background)
                }
                Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                        RoutinaNavGraph(viewModel = habitViewModel)
                    }
                }
            }
"""

import re
main_content = re.sub(r'RoutinaTheme\(darkTheme = userSettings.isDarkMode\) \{[\s\S]*?RoutinaNavGraph\(viewModel = habitViewModel\)[\s\S]*?\}[\s\S]*?\}', surface_replacement.strip(), main_content)

with open(main_activity, "w") as f:
    f.write(main_content)

