package com.wakepoint.app.feature.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wakepoint.app.R

@Composable
fun AuthScreen() {
    Text(text = stringResource(R.string.auth_title))
}
