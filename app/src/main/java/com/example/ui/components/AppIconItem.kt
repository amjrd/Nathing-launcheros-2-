package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.IconPackStyle
import com.example.ui.theme.LocalLauncherTheme
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingBorder
import com.example.ui.theme.NothingDarkSurface
import com.example.ui.theme.NothingElevated
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingWhite
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 12-lobed scalloped flower badge shape signature to Nothing OS 3.0 / 5.0 "Colour" Icon Pack.
 * Matches user reference Screenshot 4 ("Colour").
 */
class ScallopedFlowerShape(private val lobes: Int = 12) : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val path = Path()
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = min(centerX, centerY)
    val depth = radius * 0.12f
    val baseRadius = radius - depth
    val totalPoints = lobes * 8
    for (i in 0..totalPoints) {
      val theta = (i.toFloat() / totalPoints) * (2f * Math.PI.toFloat())
      val r = baseRadius + depth * (0.5f + 0.5f * cos(lobes * theta))
      val x = centerX + r * cos(theta)
      val y = centerY + r * sin(theta)
      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return Outline.Generic(path)
  }
}

val PASTEL_COLOUR_BADGE_PALETTE = listOf(
  Color(0xFFD6E4FF), // Soft Periwinkle (Screenshot 4)
  Color(0xFFD7F2D8), // Soft Sage Green
  Color(0xFFFDE4CF), // Soft Peach
  Color(0xFFF9D8E6), // Soft Blush Pink
  Color(0xFFE8DEF8), // Soft Lavender
  Color(0xFFFFF2BF), // Soft Warm Butter
  Color(0xFFD4F1F4)  // Soft Aqua
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
  app: AppItem,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  iconSize: Dp = 54.dp,
  showLabel: Boolean = true,
  iconPack: IconPackStyle = IconPackStyle.MONOCHROME,
  accentColor: Color = NothingRed
) {
  val theme = LocalLauncherTheme.current
  val isDark = theme.isDark
  val haptic = LocalHapticFeedback.current

  val pastelBg = remember(app.label) {
    PASTEL_COLOUR_BADGE_PALETTE[abs(app.label.hashCode()) % PASTEL_COLOUR_BADGE_PALETTE.size]
  }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .combinedClickable(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onClick()
        },
        onLongClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onLongClick?.invoke()
        }
      )
      .padding(4.dp)
      .testTag("app_item_${app.packageName}"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier.size(iconSize),
      contentAlignment = Alignment.Center
    ) {
      when (iconPack) {
        // 1. COLOUR PACK (Screenshot 4: 12-lobed Scalloped Flower Badge)
        IconPackStyle.COLOUR -> {
          val flowerShape = remember { ScallopedFlowerShape(lobes = 12) }
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(flowerShape)
              .background(pastelBg)
              .border(1.dp, pastelBg.copy(alpha = 0.5f), flowerShape),
            contentAlignment = Alignment.Center
          ) {
            if (app.icon != null) {
              val bitmap = remember(app.icon, isDark) {
                drawableToBitmap(app.icon, applyGrayscale = false, isDark = false)
              }
              Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                  .size(iconSize * 0.62f)
                  .clip(CircleShape)
              )
            } else {
              val iconVector = getIconVectorForApp(app.label)
              Icon(
                imageVector = iconVector,
                contentDescription = app.label,
                tint = Color(0xFF1E1E1E),
                modifier = Modifier.size(iconSize * 0.52f)
              )
            }
          }
        }

        // 2. SYSTEM DEFAULT PACK (Screenshot 4: Concentric Circle Badge)
        IconPackStyle.SYSTEM_DEFAULT -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isDark) NothingDarkSurface else Color.White)
              .border(2.dp, theme.border.copy(alpha = 0.7f), CircleShape)
              .padding(3.dp)
              .border(1.dp, theme.border.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            if (app.icon != null) {
              val bitmap = remember(app.icon) {
                drawableToBitmap(app.icon, applyGrayscale = false, isDark = false)
              }
              Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                  .size(iconSize * 0.68f)
                  .clip(CircleShape)
              )
            } else {
              val iconVector = getIconVectorForApp(app.label)
              Icon(
                imageVector = iconVector,
                contentDescription = app.label,
                tint = accentColor,
                modifier = Modifier.size(iconSize * 0.55f)
              )
            }
          }
        }

        // 3. NOTHING MONOCHROME PACK (Screenshot 1 & 2: Nothing OS Circular Glyph)
        IconPackStyle.MONOCHROME -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (isDark) NothingDarkSurface else Color.White)
              .border(
                width = 1.dp,
                color = if (isDark) theme.border else Color(0xFFE2E2E2),
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            if (app.icon != null) {
              val bitmap = remember(app.icon, isDark) {
                drawableToBitmap(app.icon, applyGrayscale = true, isDark = isDark)
              }
              Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                  .size(iconSize * 0.70f)
                  .clip(CircleShape)
              )
            } else {
              val iconVector = getIconVectorForApp(app.label)
              Icon(
                imageVector = iconVector,
                contentDescription = app.label,
                tint = if (isDark) NothingWhite else Color(0xFF161616),
                modifier = Modifier.size(iconSize * 0.55f)
              )
            }
          }
        }

        // 4. MINIMAL DARK PACK
        IconPackStyle.MINIMAL_DARK -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(NothingElevated)
              .border(1.dp, theme.border, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            if (app.icon != null) {
              val bitmap = remember(app.icon, isDark) {
                drawableToBitmap(app.icon, applyGrayscale = true, isDark = true)
              }
              Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                  .size(iconSize * 0.70f)
                  .clip(CircleShape)
              )
            } else {
              val iconVector = getIconVectorForApp(app.label)
              Icon(
                imageVector = iconVector,
                contentDescription = app.label,
                tint = NothingWhite,
                modifier = Modifier.size(iconSize * 0.55f)
              )
            }
          }
        }
      }

      // Unclipped Badge on Top-Right of icon
      Box(modifier = Modifier.align(Alignment.TopEnd)) {
        AppBadges(app = app, accentColor = accentColor, isDark = isDark)
      }
    }

    if (showLabel) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = app.label,
        color = theme.textPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun AppBadges(
  app: AppItem,
  accentColor: Color,
  isDark: Boolean
) {
  // Notification Dot / Badge (Nothing OS Signature style)
  if (app.notificationCount > 0) {
    Box(
      modifier = Modifier
        .padding(2.dp)
        .clip(CircleShape)
        .background(accentColor)
        .border(1.dp, if (isDark) NothingBlack else Color.White, CircleShape)
        .padding(horizontal = if (app.notificationCount > 1) 4.dp else 0.dp),
      contentAlignment = Alignment.Center
    ) {
      if (app.notificationCount > 1) {
        Text(
          text = if (app.notificationCount > 99) "99+" else app.notificationCount.toString(),
          color = NothingWhite,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      } else {
        Box(modifier = Modifier.size(7.dp))
      }
    }
  } else if (app.isPinned) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .padding(1.dp)
        .clip(CircleShape)
        .background(accentColor)
    )
  }
}

private fun drawableToBitmap(drawable: Drawable, applyGrayscale: Boolean, isDark: Boolean): Bitmap {
  val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
  val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)

  if (applyGrayscale) {
    val paint = Paint()
    val matrix = ColorMatrix().apply {
      setSaturation(0f)
      if (!isDark) {
        // Theme Jour (Light Mode): Keep icons crisp with dark glyph definition
        val contrast = 1.1f
        val scale = FloatArray(20) { 0f }.apply {
          this[0] = contrast
          this[6] = contrast
          this[12] = contrast
          this[18] = 1f
        }
        postConcat(ColorMatrix(scale))
      } else {
        // Theme Nuit (Dark): Boost contrast for sharp white/grey glyphs
        val contrast = 1.35f
        val scale = FloatArray(20) { 0f }.apply {
          this[0] = contrast
          this[6] = contrast
          this[12] = contrast
          this[18] = 1f
        }
        postConcat(ColorMatrix(scale))
      }
    }
    paint.colorFilter = ColorMatrixColorFilter(matrix)
    val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), paint)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    canvas.restoreToCount(layer)
  } else {
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
  }
  return bitmap
}

fun getIconVectorForApp(name: String): ImageVector {
  return when {
    name.contains("Nothing X", true) || name.contains("Ear", true) || name.contains("Headphone", true) -> Icons.Default.Headphones
    name.contains("Composer", true) || name.contains("Synthesizer", true) -> Icons.Default.GraphicEq
    name.contains("Recorder", true) || name.contains("Voice", true) -> Icons.Default.Mic
    name.contains("Weather", true) -> Icons.Default.Cloud
    name.contains("Phone", true) || name.contains("Call", true) || name.contains("Dialer", true) -> Icons.Default.Call
    name.contains("Message", true) || name.contains("Mail", true) || name.contains("Gmail", true) -> Icons.Default.Email
    name.contains("Camera", true) -> Icons.Default.CameraAlt
    name.contains("Photo", true) || name.contains("Gallery", true) -> Icons.Default.Image
    name.contains("Chrome", true) || name.contains("Browser", true) || name.contains("Web", true) -> Icons.Default.Language
    name.contains("Setting", true) -> Icons.Default.Settings
    name.contains("Clock", true) || name.contains("Alarm", true) || name.contains("Time", true) -> Icons.Default.Schedule
    name.contains("Calculator", true) || name.contains("Calc", true) -> Icons.Default.Calculate
    name.contains("Calendar", true) -> Icons.Default.CalendarToday
    name.contains("Music", true) || name.contains("Audio", true) || name.contains("YT Music", true) -> Icons.Default.MusicNote
    name.contains("YouTube", true) || name.contains("Video", true) -> Icons.Default.PlayArrow
    name.contains("Map", true) || name.contains("Navigation", true) -> Icons.Default.Map
    name.contains("File", true) -> Icons.Default.Folder
    name.contains("Drive", true) -> Icons.Default.CloudQueue
    name.contains("Safety", true) || name.contains("Security", true) -> Icons.Default.Security
    name.contains("Store", true) || name.contains("Shop", true) -> Icons.Default.ShoppingCart
    name.contains("Telegram", true) -> Icons.Default.Send
    name.contains("Chat", true) || name.contains("Discord", true) || name.contains("GPT", true) -> Icons.Default.Chat
    name.contains("Note", true) || name.contains("Keep", true) -> Icons.Default.EditNote
    name.contains("TV", true) -> Icons.Default.Tv
    name.contains("Meet", true) -> Icons.Default.Videocam
    name.contains("Contact", true) -> Icons.Default.ContactPage
    else -> Icons.Default.Android
  }
}