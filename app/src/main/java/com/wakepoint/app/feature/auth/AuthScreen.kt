package com.wakepoint.app.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.MessageText
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointLogo
import com.wakepoint.app.core.design.WakepointTextField

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        WakepointLogo()
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onKakaoLogin: () -> String?,
    onSignUp: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        AuthTopBar()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WakepointLogo(horizontal = false)
            Text(
                text = stringResource(R.string.auth_intro),
                style = MaterialTheme.typography.bodyLarge
            )
            WakepointTextField(
                value = uiState.loginEmail,
                onValueChange = onLoginEmailChange,
                placeholder = stringResource(R.string.field_email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            WakepointTextField(
                value = uiState.loginPassword,
                onValueChange = onLoginPasswordChange,
                placeholder = stringResource(R.string.auth_password),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            AuthMessage(uiState = uiState)
            WakepointButton(
                text = if (uiState.isSubmitting) {
                    stringResource(R.string.auth_processing)
                } else {
                    stringResource(R.string.auth_login)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onClick = onSignIn
            )
            KakaoLoginButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onClick = {
                    val url = onKakaoLogin() ?: return@KakaoLoginButton
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(text = "|")
                Text(
                    text = stringResource(R.string.auth_signup),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(onClick = onSignUp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.auth_terms),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.auth_copyright),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun KakaoLoginButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.kakao_login_medium_narrow),
            contentDescription = stringResource(R.string.auth_kakao_login),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 183.dp, height = 45.dp)
        )
    }
}

@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AuthTopBar(title = stringResource(R.string.signup_title), onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SignUpField(
                value = uiState.signUpName,
                onValueChange = onNameChange,
                label = stringResource(R.string.field_name),
                hint = stringResource(R.string.signup_name_hint)
            )
            SignUpField(
                value = uiState.signUpEmail,
                onValueChange = onEmailChange,
                label = stringResource(R.string.field_email),
                hint = stringResource(R.string.signup_email_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            SignUpField(
                value = uiState.signUpPassword,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.field_password),
                hint = stringResource(R.string.signup_password_hint),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            SignUpField(
                value = uiState.signUpPasswordConfirm,
                onValueChange = onPasswordConfirmChange,
                label = stringResource(R.string.field_password_confirm),
                hint = stringResource(R.string.signup_password_confirm_hint),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            AuthMessage(uiState = uiState)
            WakepointButton(
                text = if (uiState.isSubmitting) {
                    stringResource(R.string.auth_processing)
                } else {
                    stringResource(R.string.auth_signup)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onClick = onSubmit
            )
        }
    }
}

@Composable
private fun AuthTopBar(
    title: String? = null,
    onBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun AuthMessage(uiState: AuthUiState) {
    val message = uiState.errorMessage ?: uiState.infoMessage
    if (message != null) {
        MessageText(
            text = message,
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SignUpField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        WakepointTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = hint,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions
        )
    }
}
