package com.ben.firstaid

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Every fixed piece of interface text goes through tr(text) so it can be shown in Hindi.
// Text that comes from the JSON data (situations, lessons, kit, quiz) is translated in the data instead.
// Keep each tr(...) text on ONE line with no $ signs: a test checks it against the Hindi list in UiStrings.kt.

// ---------------------------------------------------------------------------
// Home
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Guided first aid: situation list -> questions -> guidance
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Learn
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// First-aid kit
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Search
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Emergency, sources, about
// ---------------------------------------------------------------------------

