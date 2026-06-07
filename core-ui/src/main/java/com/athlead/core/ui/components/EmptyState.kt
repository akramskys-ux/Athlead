package com.athlead.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athlead.core.ui.theme.Gris40
import com.athlead.core.ui.theme.Gris60
import com.athlead.core.ui.theme.Gris80
import com.athlead.core.ui.theme.Spacing

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.SpacingXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Gris40
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingL))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Gris80,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingS))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Gris60,
            textAlign = TextAlign.Center
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(Spacing.SpacingXL))

            AthleedButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}
