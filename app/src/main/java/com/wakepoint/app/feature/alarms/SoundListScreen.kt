package com.wakepoint.app.feature.alarms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.ButtonRow
import com.wakepoint.app.core.design.MessageText
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointSecondaryButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundListScreen(
    alarmId: String,
    onBack: () -> Unit,
    viewModel: SoundListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showRecording by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importSound(uri)
        }
    }
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showRecording = true
            viewModel.startRecording()
        }
    }
    val hasRecordPermission = {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.alarm_sound_list_title),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.errorMessage?.let { MessageText(text = it, isError = true) }
            uiState.message?.let { MessageText(text = it) }
            if (uiState.alarm == null) {
                MessageText(text = stringResource(R.string.sound_alarm_not_found), isError = true)
            }
            WakepointCard {
                uiState.options.forEachIndexed { index, option ->
                    SoundOptionRowItem(
                        option = option,
                        selected = option.id == uiState.selectedId,
                        enabled = !uiState.isSaving && uiState.alarm != null,
                        onClick = { viewModel.selectOption(option) }
                    )
                    if (index != uiState.options.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
            if (uiState.isSaving) {
                MessageText(text = stringResource(R.string.sound_saving))
            }
        }
        ButtonRow(modifier = Modifier.padding(16.dp)) {
            WakepointSecondaryButton(
                text = stringResource(R.string.sound_import),
                icon = Icons.Rounded.FileDownload,
                enabled = uiState.alarm != null && !uiState.isSaving,
                onClick = { importLauncher.launch(arrayOf("audio/*")) },
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.sound_record),
                icon = Icons.Rounded.Mic,
                enabled = uiState.alarm != null && !uiState.isSaving,
                onClick = {
                    if (hasRecordPermission()) {
                        showRecording = true
                        viewModel.startRecording()
                    } else {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showRecording) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.cancelRecording()
                showRecording = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() }
        ) {
            RecordingSheet(
                isRecording = uiState.isRecording,
                onStop = { viewModel.stopRecording() },
                onCancel = {
                    viewModel.cancelRecording()
                    showRecording = false
                },
                onSave = {
                    viewModel.saveRecording()
                    showRecording = false
                }
            )
        }
    }
}

@Composable
private fun SoundOptionRowItem(
    option: SoundOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val icon = when (option.kind) {
        SoundOptionKind.Default -> Icons.Rounded.Notifications
        SoundOptionKind.Recording -> Icons.Rounded.Mic
        SoundOptionKind.Imported -> Icons.Rounded.MusicNote
    }
    val subtitle = when (option.kind) {
        SoundOptionKind.Default -> stringResource(R.string.sound_option_default_subtitle)
        SoundOptionKind.Recording -> stringResource(R.string.sound_option_recording_subtitle)
        SoundOptionKind.Imported -> stringResource(R.string.sound_option_imported_subtitle)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = option.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        if (selected) {
            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null)
        }
    }
}

@Composable
private fun RecordingSheet(
    isRecording: Boolean,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var stopped by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (true) {
                delay(1_000L)
                elapsedSeconds += 1
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.record_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(
                R.string.sound_record_elapsed,
                elapsedSeconds / 60,
                elapsedSeconds % 60
            ),
            style = MaterialTheme.typography.headlineLarge
        )
        if (isRecording) {
            IconButton(
                onClick = {
                    stopped = true
                    onStop()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = stringResource(R.string.sound_record_stop)
                )
            }
            Text(
                text = stringResource(R.string.sound_recording_hint),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        HorizontalDivider()
        ButtonRow {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.alarm_save),
                enabled = stopped && !isRecording,
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
