package com.amjrd.nathinglauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

data class LauncherApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NathingLauncher() }
    }
}

@Composable
private fun NathingLauncher() {
    val context = LocalContext.current
    var drawer by remember { mutableStateOf(false) }
    val apps = remember { loadApps(context.packageManager) }

    BackHandler(enabled = drawer) { drawer = false }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, drag -> total += drag },
                    onDragEnd = {
                        if (total < -90f) drawer = true
                        total = 0f
                    },
                    onDragCancel = { total = 0f }
                )
            }
    ) {
        HomeContent(apps.take(4))
        AnimatedVisibility(visible = drawer, enter = fadeIn(), exit = fadeOut()) {
            Drawer(apps, onClose = { drawer = false })
        }
    }
}

@Composable
private fun HomeContent(dockApps: List<LauncherApp>) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("NATHING", color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelLarge)
            Text("02:00", color = Color.White, style = MaterialTheme.typography.displayMedium)
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Launcher 2.0.0", color = Color.White.copy(alpha = .45f))
            Row(
                Modifier.fillMaxWidth()
                    .background(Color.White.copy(alpha = .07f), RoundedCornerShape(24.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dockApps.forEach { AppIcon(it, 54.dp) }
            }
        }
    }
}

@Composable
private fun Drawer(apps: List<LauncherApp>, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(top = 42.dp, start = 18.dp, end = 18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Apps", style = MaterialTheme.typography.headlineMedium, color = Color.Black)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Close") }
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppIcon(app, 58.dp, dark = true)
            }
        }
    }
}

@Composable
private fun AppIcon(app: LauncherApp, size: Dp, dark: Boolean = false) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        IconButton(onClick = {
            context.startActivity(
                context.packageManager.getLaunchIntentForPackage(app.packageName)
            )
        }) {
            Icon(
                BitmapPainter(app.icon.toBitmap(size.value.toInt(), size.value.toInt()).asImageBitmap()),
                contentDescription = app.label,
                modifier = Modifier.size(size)
            )
        }
        Text(
            app.label,
            color = if (dark) Color.Black else Color.White,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun loadApps(pm: PackageManager): List<LauncherApp> =
    pm.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.MATCH_ALL
    )
        .map { LauncherApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
