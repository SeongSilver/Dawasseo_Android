package com.wakepoint.app.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointLogo
import com.wakepoint.app.core.design.WakepointMessage
import com.wakepoint.app.core.design.WakepointMessageKind
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointScreenPreviews
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextButton
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTheme

// 브랜드 에셋 치수 — 디자인 토큰 대상이 아니다 (DESIGN.md 7절 / CLAUDE.md 6절).
/** 스플래시 로고 심볼 96dp (로그인은 WakepointLogo 기본값 72dp). */
private const val SPLASH_SYMBOL_SIZE = 96

/** 카카오 로그인 공식 이미지 에셋의 고유 치수. 변형 없이 그대로 쓴다. */
private val KAKAO_BUTTON_WIDTH = 183.dp
private val KAKAO_BUTTON_HEIGHT = 45.dp

/** 비활성 상태의 카카오 버튼 불투명도 (design-handoff `Login-submitting`). */
private const val KAKAO_DISABLED_ALPHA = 0.38f

/** 스플래시 — design-handoff `Splash`. */
@Composable
fun SplashScreen() {
    val colors = LocalWakepointColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            WakepointLogo(horizontal = false, symbolSize = SPLASH_SYMBOL_SIZE)
        }
        Text(
            text = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WakepointSpacing.formHorizontal)
                .padding(bottom = WakepointSpacing.xxl)
        )
    }
}

/**
 * 로그인 — design-handoff `Login-default` · `Login-submitting` · `Login-error` · `Login-info`.
 *
 * 오류·안내는 팝업이 아니라 비밀번호 필드 아래의 메시지 영역으로 보여 준다.
 * `onDismissMessage` 는 내비게이션 호출부 시그니처 유지를 위해 남겨 둔다(인라인 메시지는 닫기 버튼이 없다).
 */
@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onKakaoLogin: () -> String?,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onDismissMessage: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = LocalWakepointColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.formHorizontal)
                .padding(top = WakepointSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WakepointLogo(horizontal = false)
            Spacer(modifier = Modifier.height(WakepointSpacing.md))
            Text(
                text = stringResource(R.string.auth_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(WakepointSpacing.xxl))
            WakepointTextField(
                value = uiState.loginEmail,
                onValueChange = onLoginEmailChange,
                placeholder = stringResource(R.string.field_email),
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(WakepointSpacing.fieldGap))
            WakepointTextField(
                value = uiState.loginPassword,
                onValueChange = onLoginPasswordChange,
                placeholder = stringResource(R.string.auth_password),
                enabled = !uiState.isSubmitting,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            AuthInlineMessage(uiState = uiState, topGap = WakepointSpacing.fieldGap)
            Spacer(modifier = Modifier.height(WakepointSpacing.xl))
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
            Spacer(modifier = Modifier.height(WakepointSpacing.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
            ) {
                WakepointTextButton(
                    text = stringResource(R.string.auth_forgot_password),
                    enabled = !uiState.isSubmitting,
                    onClick = onForgotPassword
                )
                Box(
                    modifier = Modifier
                        .size(width = WakepointSize.border, height = WakepointSpacing.md)
                        .background(colors.line)
                )
                WakepointTextButton(
                    text = stringResource(R.string.auth_signup),
                    enabled = !uiState.isSubmitting,
                    onClick = onSignUp
                )
            }
            Spacer(modifier = Modifier.height(WakepointSpacing.lg))
            OrDivider()
            Spacer(modifier = Modifier.height(WakepointSpacing.lg))
            KakaoLoginButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onClick = {
                    val url = onKakaoLogin() ?: return@KakaoLoginButton
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
            Spacer(modifier = Modifier.height(WakepointSpacing.xl))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WakepointSpacing.formHorizontal)
                .padding(bottom = WakepointSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.auth_terms),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(WakepointSpacing.sm))
            Text(
                text = stringResource(R.string.auth_copyright),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 회원가입 — design-handoff `Signup-default` · `Signup-submitting` · `Signup-error` · `Signup-info`.
 *
 * 비밀번호 확인 불일치는 필드 자체의 오류(3dp `negativeLineStrong` + 오류 아이콘 + 보조 문구)로,
 * 그 밖의 실패·안내는 가입 버튼 위 메시지 영역으로 보여 준다.
 */
@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onDismissMessage: () -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    val passwordMismatch = uiState.signUpPasswordConfirm.isNotEmpty() &&
        uiState.signUpPassword != uiState.signUpPasswordConfirm

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        WakepointPageHeader(
            title = stringResource(R.string.signup_title),
            navigation = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(WakepointSize.icon)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.formHorizontal)
                .padding(vertical = WakepointSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap)
        ) {
            WakepointTextField(
                value = uiState.signUpName,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.field_name),
                enabled = !uiState.isSubmitting
            )
            WakepointTextField(
                value = uiState.signUpEmail,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.field_email),
                enabled = !uiState.isSubmitting,
                supportingText = stringResource(R.string.signup_email_support),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            WakepointTextField(
                value = uiState.signUpPassword,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.field_password),
                enabled = !uiState.isSubmitting,
                supportingText = stringResource(R.string.signup_password_support),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            WakepointTextField(
                value = uiState.signUpPasswordConfirm,
                onValueChange = onPasswordConfirmChange,
                placeholder = stringResource(R.string.field_password_confirm),
                enabled = !uiState.isSubmitting,
                isError = passwordMismatch,
                supportingText = if (passwordMismatch) {
                    stringResource(R.string.signup_password_confirm_error)
                } else {
                    null
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            AuthInlineMessage(uiState = uiState)
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

/** 오류 / 안내 메시지 영역 — 오류가 있으면 오류를, 없으면 안내를 보여 준다. */
@Composable
private fun AuthInlineMessage(
    uiState: AuthUiState,
    topGap: Dp? = null
) {
    val message = uiState.errorMessage ?: uiState.infoMessage ?: return
    if (topGap != null) {
        Spacer(modifier = Modifier.height(topGap))
    }
    WakepointMessage(
        text = message,
        kind = if (uiState.errorMessage != null) {
            WakepointMessageKind.Error
        } else {
            WakepointMessageKind.Info
        }
    )
}

/** "또는" 구분선 — 1dp `line` + 12/16 · 500 라벨. */
@Composable
private fun OrDivider() {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = WakepointSize.border,
            color = colors.line
        )
        Text(
            text = stringResource(R.string.auth_or),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = WakepointSize.border,
            color = colors.line
        )
    }
}

/** 카카오 로그인 — 공식 이미지 에셋을 그대로 쓴다 (DESIGN.md 7절). */
@Composable
private fun KakaoLoginButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(KAKAO_BUTTON_HEIGHT)
            .alpha(if (enabled) 1f else KAKAO_DISABLED_ALPHA)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.kakao_login_medium_narrow),
            contentDescription = stringResource(R.string.auth_kakao_login),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = KAKAO_BUTTON_WIDTH, height = KAKAO_BUTTON_HEIGHT)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// @Preview — design-handoff 의 상태별 화면
// ─────────────────────────────────────────────────────────────────────

private const val PREVIEW_EMAIL = "minji@example.com"
private const val PREVIEW_PASSWORD = "password12"
private const val PREVIEW_NAME = "김민지"

@Composable
private fun PreviewAuthScreen(uiState: AuthUiState) {
    AuthScreen(
        uiState = uiState,
        onLoginEmailChange = {},
        onLoginPasswordChange = {},
        onSignIn = {},
        onKakaoLogin = { null },
        onSignUp = {}
    )
}

@Composable
private fun PreviewSignUpScreen(uiState: AuthUiState) {
    SignUpScreen(
        uiState = uiState,
        onEmailChange = {},
        onPasswordChange = {},
        onPasswordConfirmChange = {},
        onNameChange = {},
        onSubmit = {},
        onBack = {}
    )
}

@WakepointScreenPreviews
@Composable
private fun SplashPreview() {
    WakepointTheme {
        SplashScreen()
    }
}

@WakepointScreenPreviews
@Composable
private fun LoginDefaultPreview() {
    WakepointTheme {
        PreviewAuthScreen(AuthUiState(isCheckingSession = false))
    }
}

@WakepointScreenPreviews
@Composable
private fun LoginSubmittingPreview() {
    WakepointTheme {
        PreviewAuthScreen(
            AuthUiState(
                isCheckingSession = false,
                loginEmail = PREVIEW_EMAIL,
                loginPassword = PREVIEW_PASSWORD,
                isSubmitting = true
            )
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun LoginErrorPreview() {
    WakepointTheme {
        PreviewAuthScreen(
            AuthUiState(
                isCheckingSession = false,
                loginEmail = PREVIEW_EMAIL,
                loginPassword = PREVIEW_PASSWORD,
                errorMessage = "이메일과 비밀번호를 다시 확인해 주세요."
            )
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun LoginInfoPreview() {
    WakepointTheme {
        PreviewAuthScreen(
            AuthUiState(
                isCheckingSession = false,
                loginEmail = PREVIEW_EMAIL,
                loginPassword = PREVIEW_PASSWORD,
                infoMessage = "확인 메일을 보냈습니다. 메일함에서 인증을 마친 뒤 로그인해 주세요."
            )
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun SignupDefaultPreview() {
    WakepointTheme {
        PreviewSignUpScreen(AuthUiState(isCheckingSession = false))
    }
}

@WakepointScreenPreviews
@Composable
private fun SignupSubmittingPreview() {
    WakepointTheme {
        PreviewSignUpScreen(
            AuthUiState(
                isCheckingSession = false,
                signUpName = PREVIEW_NAME,
                signUpEmail = PREVIEW_EMAIL,
                signUpPassword = PREVIEW_PASSWORD,
                signUpPasswordConfirm = PREVIEW_PASSWORD,
                isSubmitting = true
            )
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun SignupErrorPreview() {
    WakepointTheme {
        PreviewSignUpScreen(
            AuthUiState(
                isCheckingSession = false,
                signUpName = PREVIEW_NAME,
                signUpEmail = PREVIEW_EMAIL,
                signUpPassword = PREVIEW_PASSWORD,
                signUpPasswordConfirm = "password"
            )
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun SignupInfoPreview() {
    WakepointTheme {
        PreviewSignUpScreen(
            AuthUiState(
                isCheckingSession = false,
                signUpName = PREVIEW_NAME,
                signUpEmail = PREVIEW_EMAIL,
                signUpPassword = PREVIEW_PASSWORD,
                signUpPasswordConfirm = PREVIEW_PASSWORD,
                infoMessage = "확인 메일을 보냈습니다. 메일함에서 인증을 마치면 가입이 끝납니다."
            )
        )
    }
}
