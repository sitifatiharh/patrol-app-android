package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun FindingCard(
    data: FindingCardData,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // 1. Gambar (Kiri Center)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F2F5)), 
                contentAlignment = Alignment.Center
            ) {
                // Tampilkan gambar dari server menggunakan Coil
                if (data.image.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Finding Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = null, // Jangan tampilkan apa-apa jika error, biar box abu di bawahnya kelihatan
                        placeholder = null
                    )
                }
                
                // Icon placeholder (akan tertutup jika gambar berhasil dimuat)
                // Jika gambar loading atau error (dan AsyncImage transparan/gagal), icon ini akan terlihat
                if (data.image.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "No Image",
                        tint = Color(0xFF6C757D)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Konten Teks (Kanan)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // --- Baris Atas: ID & Status ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ID (No. 022) -> BOLD HITAM
                    Text(
                        text = "No. ${data.id.toString().padStart(3, '0')}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Status Badge (Pojok Kanan Atas)
                    StatusBadge(status = data.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Baris Kedua: Patrol Type • Area ---
                Text(
                    text = "${data.patrol} • ${data.area}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- Baris Ketiga: Deskripsi ---
                Text(
                    text = data.title,
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- Baris Bawah: Priority & Date ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority (Pojok Kiri Bawah)
                    PriorityIndicator(priority = data.priority ?: "Low")

                    // Date (Pojok Kanan Bawah)
                    DateIndicator(label = data.dateLabel, value = data.dateValue)
                }
            }
        }
    }
}

// --- Helper Composable untuk Status Badge ---
@Composable
fun StatusBadge(status: String) {
    val (bgColor, contentColor, borderColor) = when (status.lowercase()) {
        "open" -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Color(0xFFFFCDD2)) // Merah
        "assigned" -> Triple(Color(0xFFFFF8E1), Color(0xFFF57C00), Color(0xFFFFE082)) // Kuning/Orange
        "ready to recheck" -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), Color(0xFFBBDEFB)) // Biru
        "closed" -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), Color(0xFFC8E6C9)) // Hijau
        else -> Triple(Color.LightGray, Color.Black, Color.Gray)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.wrapContentSize()
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

// --- Helper Composable untuk Priority ---
@Composable
fun PriorityIndicator(priority: String) {
    val color = when (priority.lowercase()) {
        "high" -> Color(0xFFD32F2F) // Merah
        "medium" -> Color(0xFFF57C00) // Orange
        "low" -> Color(0xFF388E3C) // Hijau
        else -> Color.Gray
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Dot Lingkaran
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = priority,
            fontSize = 12.sp,
            color = Color(0xFF5A5A5A),
            fontWeight = FontWeight.Medium
        )
    }
}

// --- Helper Composable untuk Date ---
@Composable
fun DateIndicator(label: String, value: String) {
    val isOverdue = label.equals("overdue", ignoreCase = true)
    val isFixed = label.equals("fixed", ignoreCase = true)
    val isChecked = label.equals("checked", ignoreCase = true)

    val contentColor = when {
        isOverdue -> Color(0xFFD32F2F) // Merah Teks
        isFixed -> Color(0xFF388E3C)   // Hijau Teks
        isChecked -> Color(0xFF512DA8) // Ungu/Biru Tua Teks
        else -> Color(0xFF5A5A5A)      // Abu-abu standard (Due)
    }

    val icon: ImageVector = when {
        isOverdue -> Icons.Default.Warning
        isFixed || isChecked -> Icons.Default.CheckCircle
        else -> Icons.Default.CalendarToday
    }

    if (isOverdue) {
        // Style alert untuk Overdue
        Surface(
            color = Color(0xFFFFEBEE), // Background Merah Muda
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$label: $value",
                    fontSize = 11.sp,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Style standard
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFixed || isChecked) contentColor else Color(0xFF4285F4),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Teks Label (Fixed:)
            if (!label.equals("Due", ignoreCase = true)) {
                 Text(
                    text = "$label: ",
                    fontSize = 11.sp,
                    color = contentColor,
                    fontWeight = FontWeight.Medium
                )
            } else {
                 // Untuk Due, biasanya label tidak terlalu ditonjolkan atau bisa dihilangkan jika ingin persis icon + tanggal
                 Text(
                    text = "Due: ",
                    fontSize = 11.sp,
                    color = contentColor,
                    fontWeight = FontWeight.Medium
                )
            }
           
            Text(
                text = value,
                fontSize = 11.sp,
                color = if (isFixed || isChecked) contentColor else Color(0xFF4285F4),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
