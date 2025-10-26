package com.nkmathew.kikuyuflashcards.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkmathew.kikuyuflashcards.ui.theme.*

@Composable
fun FlashCard(
    text: String,
    isEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(text) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    }

    val gradient = if (isEnglish) {
        Brush.verticalGradient(
            colors = listOf(EnglishCardPrimary, EnglishCardSecondary)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(KikuyuCardPrimary, KikuyuCardSecondary)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextOnDark,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
        }
    }
}