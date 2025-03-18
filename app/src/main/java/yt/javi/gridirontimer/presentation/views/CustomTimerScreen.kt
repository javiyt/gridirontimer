package yt.javi.gridirontimer.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import yt.javi.gridirontimer.presentation.MainActivity.Screen.Timer

@Composable
fun CustomTimerScreen(navController: NavController) {
    var minutes by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select minutes",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { minutes = (minutes - 1).coerceAtLeast(0) },
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "$minutes",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { minutes++ },
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text("+")
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                navController.navigate(Timer.createRoute(minutes * 60L * 1000L))
            },
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Text("Start")
        }
    }
}