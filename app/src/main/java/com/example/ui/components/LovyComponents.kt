package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.LovyAccent
import com.example.ui.theme.LovyPrimary
import com.example.ui.theme.LovySecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SecurityShieldBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LovyAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    isOnline: Boolean = false,
    hasStoryRing: Boolean = false,
    contentDescription: String = "Avatar Pengguna",
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val imageModifier = Modifier
            .size(if (hasStoryRing) size - 6.dp else size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)

        // Story Ring Gradient
        if (hasStoryRing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(LovyPrimary, LovySecondary, LovyAccent, LovyPrimary)
                        ),
                        shape = CircleShape
                    )
            )
        }

        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = imageModifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = imageModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }

        // Online Status Dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
                    .padding(2.dp)
                    .background(OnlineGreen, CircleShape)
                    .testTag("online_status_dot")
            )
        }
    }
}

@Composable
fun RadarScanningCanvas(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarAnimation")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha1"
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f

            // Static concentric grid circles
            val ringCount = 4
            for (i in 1..ringCount) {
                val radius = maxRadius * (i.toFloat() / ringCount)
                drawCircle(
                    color = LovyPrimary.copy(alpha = 0.12f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Crosshair lines
            drawLine(
                color = LovyPrimary.copy(alpha = 0.15f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = LovyPrimary.copy(alpha = 0.15f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )

            if (isScanning) {
                // Expanding Pulse Ring
                drawCircle(
                    color = LovyPrimary.copy(alpha = pulseAlpha1),
                    radius = maxRadius * pulseScale1,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Radar Sweep beam line
                val rad = Math.toRadians(sweepAngle.toDouble())
                val sweepTarget = Offset(
                    (center.x + maxRadius * cos(rad)).toFloat(),
                    (center.y + maxRadius * sin(rad)).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(LovyPrimary, LovySecondary.copy(alpha = 0f)),
                        start = center,
                        end = sweepTarget
                    ),
                    start = center,
                    end = sweepTarget,
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Center radar core point
            drawCircle(
                color = LovyPrimary,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
fun E2EEBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("e2ee_security_banner"),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Terenkripsi End-to-End",
                tint = LovyPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pesan & foto terenkripsi End-to-End (AES-256). Ketuk info privasi.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SecurityShieldBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Dialog to inspect raw ciphertext on the wire vs local decrypted text
@Composable
fun CiphertextInspectorDialog(
    plainText: String,
    cipherText: String,
    iv: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = LovyPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inspektur Enkripsi E2EE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Lovy Chat menjamin privasi total. Data di server Firebase disimpan dalam bentuk Ciphertext acak yang mustahil dibaca siapapun.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Teks Asli (Plaintext di HP Kamu):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Surface(
                    color = LovyPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = plainText,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Ciphertext (Yang Disimpan di Firebase):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = cipherText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = LovyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Algoritma: AES-256-GCM | IV: $iv",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary)
            ) {
                Text("Tutup", color = Color.White)
            }
        }
    )
}

// Dialog for verifying mutual safety numbers (like Signal)
@Composable
fun SafetyNumberDialog(
    userName: String,
    safetyNumber: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = OnlineGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifikasi Nomor Keamanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bandingkan nomor keamanan berikut dengan perangkat $userName untuk memastikan tidak ada penyadapan (Man-in-the-Middle).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Simulasi QR Code Keamanan",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = safetyNumber,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = LovyPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary)
            ) {
                Text("Terverifikasi Cocok", color = Color.White)
            }
        }
    )
}

// Lightbox full image preview
@Composable
fun PhotoLightboxDialog(
    photoUrl: String,
    caption: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = OnlineGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Foto Terenkripsi E2EE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                AsyncImage(
                    model = photoUrl,
                    contentDescription = caption ?: "Foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                if (!caption.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
