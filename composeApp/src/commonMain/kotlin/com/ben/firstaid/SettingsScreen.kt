package com.ben.firstaid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsScreen(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    Page {
        PageTitle(tr("Settings"), tr("Personalize how First Aid Awareness looks on this device."))
        AppCard {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("Appearance"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(tr("Choose your preferred colour theme."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                ThemeOption(ThemeMode.System, themeMode, tr("System default"), tr("Follow your device appearance."), onThemeModeChange)
                ThemeOption(ThemeMode.Light, themeMode, tr("Light"), tr("Bright surfaces and dark text."), onThemeModeChange)
                ThemeOption(ThemeMode.Dark, themeMode, tr("Dark"), tr("Dark surfaces with comfortable contrast."), onThemeModeChange)
            }
        }
        Text(
            tr("Your choice is saved on this device and will be restored next time you open the app."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** [title] and [description] arrive already translated, so each text is translated by a plain call in SettingsScreen above. */
@Composable
private fun ThemeOption(
    mode: ThemeMode,
    selectedMode: ThemeMode,
    title: String,
    description: String,
    onSelected: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = selectedMode == mode, role = Role.RadioButton, onClick = { onSelected(mode) })
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selectedMode == mode, onClick = null)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
