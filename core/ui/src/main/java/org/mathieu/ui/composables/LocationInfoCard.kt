package org.mathieu.ui.composables

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mathieu.domain.models.location.LocationPreview

/**
 * A card composable where we can display data of a specific location.
 * This card is also clickable, triggering a callback when the user taps on it.
 *
 * @param location The [LocationPreview] object representing the details of the location to be displayed.
 * @param onLocationClick A callback function that gets triggered when the card is clicked. It provides the clicked [LocationPreview] as a parameter.
 */
@Composable
fun LocationInfoCard(
    location: LocationPreview,
    onLocationClick: (LocationPreview) -> Unit
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onLocationClick(location)
                triggerVibration(context)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Name of the location
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Type of the location
            Text(
                text = location.type,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            )

            // Dimension of the location
            Text(
                text = "Dimension: ${location.dimension}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Triggers a vibration on the device if the device has a vibrator.
 * This function checks if the device supports vibration and vibrates for a short duration (200 milliseconds).
 *
 * @param context The context used to access the system's vibrator service.
 */
fun triggerVibration(context: Context) {

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE) // Vibrate for 200 ms
        vibrator.vibrate(vibrationEffect)
    }
}