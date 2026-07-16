package com.wakepoint.app.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointBorder
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointLogo
import com.wakepoint.app.core.design.WakepointMessageDialog
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
            .padding(horizontal = 28.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.logo_square),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(112.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = WakepointInk,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onKakaoLogin: () -> String?,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onDismissMessage: () -> Unit = {}
) {
    val context = LocalContext.current

    AuthMessageDialog(uiState = uiState, onDismiss = onDismissMessage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
    ) {
        AuthTopBar()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WakepointLogo(horizontal = false)
            Text(
                text = stringResource(R.string.auth_intro),
                style = MaterialTheme.typography.headlineLarge,
                color = WakepointInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp, bottom = 28.dp)
            )
            WakepointTextField(
                value = uiState.loginEmail,
                onValueChange = onLoginEmailChange,
                placeholder = stringResource(R.string.field_email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            WakepointTextField(
                value = uiState.loginPassword,
                onValueChange = onLoginPasswordChange,
                placeholder = stringResource(R.string.auth_password),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(20.dp))
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
            Spacer(modifier = Modifier.height(24.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(24.dp))
            KakaoLoginButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onClick = {
                    val url = onKakaoLogin() ?: return@KakaoLoginButton
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    color = WakepointInk,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(enabled = !uiState.isSubmitting, onClick = onForgotPassword)
                )
                Text(text = "|", color = WakepointMuted)
                Text(
                    text = stringResource(R.string.auth_signup),
                    color = WakepointInk,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(onClick = onSignUp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.auth_terms),
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(72.dp))
            Text(
                text = stringResource(R.string.auth_copyright),
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = WakepointBorder)
        Text(
            text = stringResource(R.string.auth_or),
            style = MaterialTheme.typography.bodySmall,
            color = WakepointTextSecondary
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = WakepointBorder)
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
            .height(45.dp)
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
    onBack: () -> Unit,
    onDismissMessage: () -> Unit = {}
) {
    AuthMessageDialog(uiState = uiState, onDismiss = onDismissMessage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
    ) {
        AuthTopBar(title = stringResource(R.string.signup_title), onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            Spacer(modifier = Modifier.height(12.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(WakepointCanvas),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = WakepointInk
            )
        }
    }
}

@Composable
private fun AuthMessageDialog(uiState: AuthUiState, onDismiss: () -> Unit) {
    val isError = uiState.errorMessage != null
    val message = uiState.errorMessage ?: uiState.infoMessage
    if (message != null) {
        WakepointMessageDialog(
            message = message,
            onDismiss = onDismiss,
            isError = isError
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
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = WakepointInk)
        WakepointTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = hint,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions
        )
    }
}
