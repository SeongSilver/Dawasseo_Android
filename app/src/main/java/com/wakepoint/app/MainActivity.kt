package com.wakepoint.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.navigation.WakepointApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleOAuthCallback(intent)
        enableEdgeToEdge()
        setContent {
            WakepointTheme {
                WakepointApp()
            }
        }
        observeSessionForPushToken()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        val callbackUri = intent?.data?.toString() ?: return
        if (!callbackUri.startsWith(OAUTH_REDIRECT_URI)) return

        lifecycleScope.launch {
            authRepository.completeOAuthSignIn(callbackUri).onFailure { error ->
                Toast.makeText(
                    this@MainActivity,
                    error.message ?: "카카오 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 로그인 세션이 생길 때마다 FCM 토큰을 서버 프로필에 반영한다.
     * 저장된 토큰이 없으면 Firebase에서 한 번 가져온다. Firebase가 초기화되지 않은
     * 빌드(google-services.json 없음)에서는 조용히 건너뛴다.
     */
    private fun observeSessionForPushToken() {
        lifecycleScope.launch {
            authRepository.authSession
                .map { session -> session?.userId }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { syncPushToken() }
        }
    }

    private suspend fun syncPushToken() {
        val storedToken = userPreferences.pushToken.first()
        if (storedToken.isNullOrBlank()) {
            fetchFirebaseToken { token ->
                lifecycleScope.launch {
                    userPreferences.savePushToken(token)
                    uploadPushToken()
                }
            }
        } else {
            uploadPushToken()
        }
    }

    private suspend fun uploadPushToken() {
        authRepository.syncPushToken().onFailure { error ->
            Log.w(TAG, "Push token sync failed: ${error.message}")
        }
    }

    private fun fetchFirebaseToken(onToken: (String) -> Unit) {
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) onToken(token)
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Firebase token unavailable: ${error.message}")
                }
        }.onFailure { error ->
            Log.w(TAG, "Firebase not initialized: ${error.message}")
        }
    }

    companion object {
        private const val TAG = "WakepointMain"
        private const val OAUTH_REDIRECT_URI = "com.wakepoint.app://auth-callback"
    }
}
