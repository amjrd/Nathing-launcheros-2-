package com.amjrd.nathinglauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LauncherApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    lateinit var widgetHost: AppWidgetHost
    lateinit var widgetManager: AppWidgetManager
    var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private val pickWidget = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            widgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                getPreferences(MODE_PRIVATE).edit().putInt("widget_id", widgetId).apply()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetManager = AppWidgetManager.getInstance(this)
        widgetHost = AppWidgetHost(this, 0x4E4F53)
        widgetHost.startListening()
        widgetId = getPreferences(MODE_PRIVATE).getInt("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID)
        setContent { NathingLauncher() }
    }

    override fun onDestroy() {
        widgetHost.stopListening()
        super.onDestroy()
    }

    fun addSystemWidget() {
        val id = widgetHost.allocateAppWidgetId()
        widgetId = id
        pickWidget.launch(Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
    }

    fun removeSystemWidget() {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) widgetHost.deleteAppWidgetId(widgetId)
        getPreferences(MODE_PRIVATE).edit().remove("widget_id").apply()
        widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }

    fun openWidgetInfo() {
        val info = widgetManager.getAppWidgetInfo(widgetId) ?: return
        openAppInfo(info.provider.packageName)
    }

    fun openAppInfo(packageName: String) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
        })
    }

    fun openWallpaperPicker() {
        runCatching { startActivity(Intent(Intent.ACTION_SET_WALLPAPER)) }
    }

    fun openNotificationAccess() {
        runCatching { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }
}

@Composable
private fun NathingLauncher() {
    val context = LocalContext.current
    var drawer by remember { mutableStateOf(false) }
    var google by remember { mutableStateOf(false) }
    var lock by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val apps = remember { loadApps(context.packageManager) }

    BackHandler(enabled = drawer || google || lock || menu) { when { menu -> menu = false; lock -> lock = false; google -> google = false; drawer -> drawer = false } }

    Box(Modifier.fillMaxSize().background(Color(0xFF101010))) {
        HomeContent(apps.take(4), widgetId, onAddWidget = { addSystemWidget() }, onWidgetInfo = { openWidgetInfo() }, onRemoveWidget = { removeSystemWidget() }, onMenu = { menu = true }, onLock = { lock = true })
        AnimatedVisibility(
            visible = drawer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Drawer(apps = apps, onClose = { drawer = false })
        }
        if (!drawer && !google && !lock && !menu) {
            Box(
                Modifier.fillMaxSize().pointerInput(Unit) {
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
            )
        }
    }
    if (google) GooglePage(onClose = { google = false })
    if (lock) LockPage(onUnlock = { lock = false })
    if (menu) LauncherMenu(
        onDismiss = { menu = false },
        onWallpaper = { menu = false; openWallpaperPicker() },
        onNotifications = { menu = false; openNotificationAccess() },
        onAddWidget = { menu = false; addSystemWidget() },
        onLock = { menu = false; lock = true }
    )
}

@Composable
private fun HomeContent(dockApps: List<LauncherApp>, widgetId: Int, onAddWidget: () -> Unit, onWidgetInfo: () -> Unit, onRemoveWidget: () -> Unit, onMenu: () -> Unit, onLock: () -> Unit) {
    val time = remember { mutableStateOf(currentTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            time.value = currentTime()
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "NATHING",
                color = Color.White.copy(alpha = .62f),
                style = MaterialTheme.typography.labelLarge
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(time.value, color = Color.White, style = MaterialTheme.typography.displayLarge)
                TextButton(onClick = onMenu) { Text("⋮", color = Color.White) }
            }
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                AndroidView(
                    factory = { ctx ->
                        val info = AppWidgetManager.getInstance(ctx).getAppWidgetInfo(widgetId)
                        widgetHost.createView(ctx, widgetId, info)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp)
                )
                Row {
                    TextButton(onClick = onWidgetInfo) { Text("APP INFO") }
                    TextButton(onClick = onRemoveWidget) { Text("REMOVE") }
                }
            } else {
                HomeWidget("SYSTEM WIDGET", "ADD REAL ANDROID WIDGET", Modifier.fillMaxWidth(), onAddWidget)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeWidget("TOOLS", "ADD / EDIT", Modifier.weight(1f), onAddWidget)
                HomeWidget("MEDIA", "SYSTEM", Modifier.weight(1f), onAddWidget)
            }
        }

        Dock(dockApps)
    }
}

@Composable
private fun HomeWidget(title: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Column(
        modifier
            .combinedClickable(onClick = { onClick?.invoke() }, onLongClick = { onClick?.invoke() })
            .background(Color.White.copy(alpha = .055f), RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 15.dp)
    ) {
        Text(
            title,
            color = Color.White.copy(alpha = .42f),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = Color.White.copy(alpha = .9f),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun Dock(dockApps: List<LauncherApp>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "DOCK",
            color = Color.White.copy(alpha = .28f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 6.dp)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = .07f), RoundedCornerShape(26.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.forEach { AppIcon(it, 52.dp) }
        }
    }
}

@Composable
private fun Drawer(apps: List<LauncherApp>, onClose: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(query, apps) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF101010))
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, drag -> total += drag },
                    onDragEnd = {
                        if (total > 120f) onClose()
                        total = 0f
                    },
                    onDragCancel = { total = 0f }
                )
            }
            .padding(top = 28.dp, start = 18.dp, end = 18.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().padding(bottom = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.width(42.dp).height(4.dp)
                    .background(Color.White.copy(alpha = .28f), RoundedCornerShape(4.dp))
            )
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("APPLICATIONS", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    "${filteredApps.size} applications",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = .45f)
                )
            }
            TextButton(onClick = onClose) {
                Text("CLOSE", color = Color.White.copy(alpha = .72f))
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("Search applications", color = Color.White.copy(alpha = .4f)) },
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White.copy(alpha = .45f),
                unfocusedBorderColor = Color.White.copy(alpha = .18f),
                cursorColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = .05f),
                unfocusedContainerColor = Color.White.copy(alpha = .035f)
            )
        )

        Spacer(Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppIcon(app, 58.dp)
            }
        }
    }
}

@Composable
private fun AppIcon(app: LauncherApp, size: Dp, dark: Boolean = false) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
        IconButton(onClick = {
            context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) }
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
    pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), PackageManager.MATCH_ALL)
        .map { LauncherApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }

private fun currentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun currentDate(): String =
    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

@Composable
private fun GooglePage(onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF101010)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GOOGLE", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { }) { Text("OPEN GOOGLE") }
            TextButton(onClick = onClose) { Text("BACK") }
        }
    }
}

@Composable
private fun LockPage(onUnlock: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(currentTime(), color = Color.White, style = MaterialTheme.typography.displayLarge)
            Text(currentDate(), color = Color.White.copy(alpha = .6f))
            Spacer(Modifier.height(32.dp))
            Text("NOTHING OS", color = Color.White.copy(alpha = .5f))
            Spacer(Modifier.height(32.dp))
            Button(onClick = onUnlock) { Text("UNLOCK") }
        }
    }
}

@Composable
private fun LauncherMenu(
    onDismiss: () -> Unit,
    onWallpaper: () -> Unit,
    onNotifications: () -> Unit,
    onAddWidget: () -> Unit,
    onLock: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NOTHING LAUNCHER") },
        text = {
            Column {
                TextButton(onClick = onAddWidget, modifier = Modifier.fillMaxWidth()) { Text("SYSTEM WIDGETS") }
                TextButton(onClick = onWallpaper, modifier = Modifier.fillMaxWidth()) { Text("WALLPAPER") }
                TextButton(onClick = onNotifications, modifier = Modifier.fillMaxWidth()) { Text("NOTIFICATIONS") }
                TextButton(onClick = onLock, modifier = Modifier.fillMaxWidth()) { Text("LOCKSCREEN") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CLOSE") } }
    )
}
