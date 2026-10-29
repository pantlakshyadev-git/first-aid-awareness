package com.ben.firstaid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ben.firstaid.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.coroutines.cancellation.CancellationException

internal val AppBlue = Color(0xFF0F5257)
internal val AppBlueDark = Color(0xFF0B3C40)
internal val SoftBlue = Color(0xFFE6F4F5)
internal val PrimaryContainerBlue = Color(0xFFD3ECF0)
internal val CardBorder = Color(0xFFD4E3E5)
internal val EmergencyRed = Color(0xFFC92A2A)
internal val PageBackground = Color(0xFFF7FAFA)

@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppBlue,
            onPrimary = Color.White,
            primaryContainer = PrimaryContainerBlue,
            onPrimaryContainer = AppBlueDark,
            secondary = AppBlue,
            onSecondary = Color.White,
            secondaryContainer = SoftBlue,
            background = PageBackground,
            surface = Color.White,
            surfaceVariant = SoftBlue,
            onSurfaceVariant = Color(0xFF4A5568),
            error = EmergencyRed,
            outline = CardBorder,
        ),
        content = content,
    )
}

/** Scrolling page, centred and limited in width so text stays readable on wide windows. */
@Composable
internal fun Page(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
internal fun PageTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AppBlue,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun AppCard(
    modifier: Modifier = Modifier.fillMaxWidth(),
    containerColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val border = BorderStroke(1.dp, CardBorder)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            content = content,
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            content = content,
        )
    }
}

/** A soft blue rounded container with a drawn icon inside. */
@Composable
internal fun IconBadge(icon: AppIcon) {
    Box(
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SoftBlue),
        contentAlignment = Alignment.Center,
    ) {
        IconGlyph(icon, Modifier.size(24.dp), AppBlue)
    }
}

@Composable
internal fun InfoCard(
    title: String,
    body: String,
    icon: AppIcon? = null,
    onClick: (() -> Unit)? = null,
) {
    AppCard(onClick = onClick) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconBadge(icon)
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (body.isNotBlank()) {
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** A heading with a bulleted list. */
@Composable
internal fun BulletSection(heading: String, items: List<String>, containerColor: Color = Color.White) {
    AppCard(containerColor = containerColor) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(heading, fontWeight = FontWeight.Bold, color = AppBlue, style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("\u2022 ", fontWeight = FontWeight.Bold, color = AppBlue)
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
internal fun RadioRow(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
internal fun MissingItem(message: String, onBack: () -> Unit) {
    Page {
        Text(message)
        Button(onClick = onBack) { Text(tr("Back")) }
    }
}

// ---------------------------------------------------------------------------
// Links and phone calls
// ---------------------------------------------------------------------------

internal fun UriHandler.tryOpen(uri: String): Boolean = try {
    openUri(uri)
    true
} catch (e: Exception) {
    false
}

/** Opens the phone dialer with 112 on Android and iOS. On desktop it shows plain text instead. */
@Composable
internal fun CallEmergencyButton(modifier: Modifier = Modifier) {
    if (!canPlaceCalls) {
        Text(
            tr("On a computer, dial {0} from a phone.", EMERGENCY_NUMBER),
            modifier = modifier,
            fontWeight = FontWeight.Bold,
        )
        return
    }
    val uriHandler = LocalUriHandler.current
    var failed by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Button(
            onClick = { failed = !uriHandler.tryOpen("tel:$EMERGENCY_NUMBER") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        ) {
            IconGlyph(AppIcon.Emergency, Modifier.size(18.dp), Color.White)
            Spacer(Modifier.width(8.dp))
            Text(tr("Call {0}", EMERGENCY_NUMBER), fontWeight = FontWeight.Bold)
        }
        if (failed) {
            Text(
                tr("Could not open the phone dialer. Please dial {0} manually.", EMERGENCY_NUMBER),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Kit images (bundled in composeResources/drawable/kit/<item id>.png)
// ---------------------------------------------------------------------------

@Composable
internal fun KitImage(item: KitItem, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(null, item.id) {
        value = try {
            Res.readBytes("drawable/kit/${item.id}.png").decodeToImageBitmap()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
    val loaded = bitmap
    if (loaded != null) {
        Image(bitmap = loaded, contentDescription = item.name, modifier = modifier, contentScale = ContentScale.Fit)
    } else {
        Box(modifier.background(SoftBlue, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Text(item.name.take(1).uppercase(), color = AppBlue, fontWeight = FontWeight.Bold)
        }
    }
}
