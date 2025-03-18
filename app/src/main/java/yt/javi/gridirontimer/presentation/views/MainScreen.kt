package yt.javi.gridirontimer.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import yt.javi.gridirontimer.R
import yt.javi.gridirontimer.presentation.MainActivity.Screen
import yt.javi.gridirontimer.presentation.theme.GridironTimerTheme

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.navigate(Screen.Timer.createRoute(20L * 60L * 1000L)) },
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Text(text = stringResource(R.string.flag))
        }

        Button(
            onClick = { navController.navigate(Screen.Timer.createRoute(12L * 60L * 1000L)) },
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Text(text = stringResource(R.string.tackle))
        }


//        Button(
//            onClick = { navController.navigate(Screen.CustomTimer.route) },
//            colors = ButtonDefaults.primaryButtonColors()
//        ) {
//            Text(text = "Custom")
//        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    GridironTimerTheme {
        MainScreen(rememberSwipeDismissableNavController())
    }
}