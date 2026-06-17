package com.wakepoint.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointLogo
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointTextField

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
            .padding(horizontal = 24.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.logo_square),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(128.dp)
        )
        Spacer(modifier = Modifier.weight(1.5f))
        Text(
            text = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = WakepointInk
        )
    }
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        AuthTopBar()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 88.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WakepointLogo(horizontal = false)
            Text(
                text = stringResource(R.string.auth_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = WakepointInk,
                modifier = Modifier.padding(top = 28.dp, bottom = 28.dp)
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
            AuthMessage(uiState = uiState)
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(12.dp))
            WakepointButton(
                text = stringResource(R.string.auth_kakao_login),
                icon = Icons.Rounded.ChatBubble,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    color = WakepointInk,
                    style = MaterialTheme.typography.bodyMedium
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
fun SignUpScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
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
            AuthMessage(uiState = uiState)
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
            .background(WakepointParchment),
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
private fun AuthMessage(uiState: AuthUiState) {
    val message = uiState.errorMessage ?: uiState.infoMessage
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (uiState.errorMessage != null) WakepointDanger else WakepointMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
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
