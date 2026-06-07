package com.athlead.core.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.athlead.core.ui.theme.Azure
import com.athlead.core.ui.theme.CornerRadius
import com.athlead.core.ui.theme.Verificado

enum class ButtonVariant {
    Primary,
    Secondary,
    Success
}

@Composable
fun AthleedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = Azure,
            contentColor = Color.White
        )
        ButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Azure
        )
        ButtonVariant.Success -> ButtonDefaults.buttonColors(
            containerColor = Verificado,
            contentColor = Color.White
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(CornerRadius.Medium)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
