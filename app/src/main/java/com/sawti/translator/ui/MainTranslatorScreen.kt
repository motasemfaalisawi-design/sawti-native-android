package com.sawti.translator.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TranslationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val original: String,
    val translated: String,
    val direction: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTranslatorScreen(
    isListening: Boolean,
    spokenText: String,
    translatedText: String,
    rmsDb: Float,
    sourceLang: String,
    targetLang: String,
    onToggleListening: () -> Unit,
    onSwapLanguages: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var history by remember { mutableStateOf(listOf<TranslationHistoryItem>()) }

    LaunchedEffect(translatedText) {
        if (translatedText.isNotBlank() && spokenText.isNotBlank()) {
            val exists = history.any { it.original == spokenText && it.translated == translatedText }
            if (!exists) {
                history = listOf(
                    TranslationHistoryItem(
                        original = spokenText,
                        translated = translatedText,
                        direction = "$sourceLang -> $targetLang"
                    )
                ) + history.take(30)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "صوتي",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "نيتيف أصلي",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = onSwapLanguages,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (sourceLang == "ar") "العربية" else "English",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "تبديل اللغة",
                            tint = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp).size(18.dp)
                        )
                        Text(
                            text = if (targetLang == "en") "English" else "العربية",
                            color = Color(0xFFF472B6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (sourceLang == "ar") "الكلام الملتقط (عربي)" else "Speech Input (English)",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isListening) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "يستمع الآن...",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (spokenText.isNotBlank()) spokenText else "تحدث بالصوت وسيظهر كلامك هنا فوراً...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (spokenText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.35f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (targetLang == "en") "الترجمة الفورية (إنجليزية)" else "Instant Translation (Arabic)",
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )

                            if (translatedText.isNotBlank()) {
                                Row {
                                    IconButton(
                                        onClick = { onSpeak(translatedText) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "نطق الترجمة",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(translatedText))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ الترجمة",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (translatedText.isNotBlank()) translatedText else "الترجمة الفورية ستظهر هنا بسرعة البرق...",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (translatedText.isNotBlank()) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.35f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clickable { onToggleListening() }
            ) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isListening) Color(0xFFEF4444) else Color(0xFF2563EB),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "إيقاف الميكروفون" else "بدء الاستماع",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Text(
                text = if (isListening) "انقر للإيقاف (يستمع ويتحدث معك)" else "انقر على الميكروفون وابدأ الكلام",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isListening) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            if (history.isNotEmpty()) {
                Text(
                    text = "سجل الترجمة اللحظي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(history) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.original,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = item.translated,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                                IconButton(
                                    onClick = { onSpeak(item.translated) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "نطق",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
