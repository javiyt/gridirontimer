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
import yt.javi.gridirontimer.presentation.viewmodel.AppTimerSettings

@Composable
fun CustomTimerScreen(navController: NavController) {
    var flagMinutes by remember { mutableIntStateOf((AppTimerSettings.flagGameDurationMs / 60_000L).toInt()) }
    var tackleMinutes by remember { mutableIntStateOf((AppTimerSettings.tackleGameDurationMs / 60_000L).toInt()) }
    var timeoutSeconds by remember { mutableIntStateOf((AppTimerSettings.timeoutDurationMs / 1_000L).toInt()) }
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
                text = stringResource(R.string.settings),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colors.onSurface)
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingStepper(
                label = stringResource(R.string.flag_game_clock),
                valueText = "$flagMinutes min",
                onDecrease = { flagMinutes = (flagMinutes - 1).coerceAtLeast(1) },
                onIncrease = { flagMinutes += 1 }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingStepper(
                label = stringResource(R.string.tackle_game_clock),
                valueText = "$tackleMinutes min",
                onDecrease = { tackleMinutes = (tackleMinutes - 1).coerceAtLeast(1) },
                onIncrease = { tackleMinutes += 1 }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingStepper(
                label = stringResource(R.string.timeout_clock),
                valueText = "$timeoutSeconds s",
                onDecrease = { timeoutSeconds = (timeoutSeconds - 5).coerceAtLeast(5) },
                onIncrease = { timeoutSeconds += 5 }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    AppTimerSettings.flagGameDurationMs = flagMinutes * 60L * 1_000L
                    AppTimerSettings.tackleGameDurationMs = tackleMinutes * 60L * 1_000L
                    AppTimerSettings.timeoutDurationMs = timeoutSeconds * 1_000L
                    navController.popBackStack()
                },
                modifier = Modifier.width(110.dp),
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun SettingStepper(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Text(
        text = label,
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colors.onSurface
        )
    )
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDecrease,
            colors = ButtonDefaults.secondaryButtonColors()
        ) {
            Text("-")
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = valueText,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colors.onBackground)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Button(
            onClick = onIncrease,
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Text("+")
        }
    }
}
