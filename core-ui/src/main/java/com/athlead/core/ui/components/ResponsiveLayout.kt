package com.athlead.core.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Breakpoints {
    val Compact: ClosedRange<Dp> = 0.dp..599.dp      // Phones
    val Medium: ClosedRange<Dp> = 600.dp..839.dp     // Tablets (portrait)
    val Expanded: ClosedRange<Dp> = 840.dp..9999.dp  // Tablets (landscape), foldables
}

@Composable
fun ResponsiveLayout(
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit = compactContent,
    expandedContent: @Composable () -> Unit = mediumContent
) {
    BoxWithConstraints {
        when {
            maxWidth < 600.dp -> compactContent()
            maxWidth < 840.dp -> mediumContent()
            else -> expandedContent()
        }
    }
}
