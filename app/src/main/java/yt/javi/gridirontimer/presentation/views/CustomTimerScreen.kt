package yt.javi.gridirontimer.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import yt.javi.gridirontimer.R
import yt.javi.gridirontimer.presentation.MainActivity.Screen.Timer

@Composable
fun CustomTimerScreen(navController: NavController) {
    var minutes by remember { mutableIntStateOf(0) }
    val bg = Brush.radialGradient(
        colors = listOf(Color(0xFF16243A), MaterialTheme.colors.background),
        radius = 360f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Select minutes",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { minutes = (minutes - 1).coerceAtLeast(0) },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("-")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "$minutes",
                    style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colors.onBackground)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { minutes++ },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    navController.navigate(Timer.createRoute(minutes * 60L * 1000L))
                },
                modifier = Modifier.width(110.dp),
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text(stringResource(R.string.start))
            }
        }
    }
}
