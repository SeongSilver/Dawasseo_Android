package com.wakepoint.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.navigation.WakepointApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleOAuthCallback(intent)
        enableEdgeToEdge()
        setContent {
            WakepointTheme {
                WakepointApp()
            }
        }
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

    companion object {
        private const val OAUTH_REDIRECT_URI = "com.wakepoint.app://auth-callback"
    }
}
