package com.sahayak.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shape scale.
 *
 * M3E increases corner radii across the board for a softer,
 * more approachable look compared to standard M3.
 */
val SahayakShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),    // default M3: 4dp
    small      = RoundedCornerShape(16.dp),   // default M3: 8dp
    medium     = RoundedCornerShape(20.dp),   // default M3: 12dp  — used by Card
    large      = RoundedCornerShape(28.dp),   // default M3: 16dp  — hero cards
    extraLarge = RoundedCornerShape(32.dp),   // default M3: 28dp  — bottom sheets
)
