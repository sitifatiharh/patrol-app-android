package com.rahma.findingpatrol.ui.finding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable

@Composable
fun FindingCard(
    data: FindingCardData,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {

            // Fake Image Box → nanti kamu ganti AsyncImage Coil
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFECECEC), RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                // findingid
                Text(data.id.toString(), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                // Area & Patrol
                Row {
                    Text(data.area, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(data.patrol, fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(8.dp))

                // Title (finding + countermeasure jika ada)
                Text(data.title, fontSize = 12.sp)

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Priority (optional)
                    data.priority?.let {
                        Text(it, fontSize = 12.sp, color = Color(0xFFCE6A00))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Date label + value
                    Text("${data.dateLabel}: ${data.dateValue}", fontSize = 12.sp, color = Color.Black)
                }

                Spacer(Modifier.height(4.dp))

                // Status
                Text(
                    data.status,
                    fontSize = 12.sp,
                    color = Color(0xFF5A5A5A)
                )
            }
        }
    }
}
