package com.wakepoint.app.feature.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.ButtonRow
import com.wakepoint.app.core.design.MessageText
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SectionLabel
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.feature.home.ALARM_RADIUS_OPTIONS

@Composable
fun AlarmsScreen(
    onOpenSoundList: () -> Unit,
    onCreateAlarm: () -> Unit,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedAlarmId by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<Alarm?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        WakepointHeader(
            action = {
                IconButton(onClick = onCreateAlarm) {
                    Text(text = "+", style = MaterialTheme.typography.titleLarge)
                }
            }
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                MessageText(text = errorMessage, isError = true)
            }

            if (uiState.alarms.isEmpty()) {
                EmptyAlarmMessage()
            } else {
                if (uiState.activeAlarms.isNotEmpty()) {
                    SectionLabel(text = stringResource(R.string.alarm_active))
                    uiState.activeAlarms.forEach { alarm ->
                        key(alarm.id) {
                            AlarmListCard(
                                alarm = alarm,
                                expanded = expandedAlarmId == alarm.id,
                                isUpdating = alarm.id in uiState.updatingAlarmIds,
                                onToggleExpanded = {
                                    expandedAlarmId = if (expandedAlarmId == alarm.id) null else alarm.id
                                },
                                onOpenSoundList = onOpenSoundList,
                                onActiveChange = { isActive -> viewModel.setAlarmActive(alarm.id, isActive) },
                                onSave = { updatedAlarm, label, radius ->
                                    viewModel.updateAlarmSettings(updatedAlarm, label, radius)
                                },
                                onDeleteRequest = { deleteTarget = alarm }
                            )
                        }
                    }
                }

                if (uiState.inactiveAlarms.isNotEmpty()) {
                    SectionLabel(text = stringResource(R.string.alarm_inactive))
                    uiState.inactiveAlarms.forEach { alarm ->
                        key(alarm.id) {
                            AlarmListCard(
                                alarm = alarm,
                                expanded = expandedAlarmId == alarm.id,
                                isUpdating = alarm.id in uiState.updatingAlarmIds,
                                onToggleExpanded = {
                                    expandedAlarmId = if (expandedAlarmId == alarm.id) null else alarm.id
                                },
                                onOpenSoundList = onOpenSoundList,
                                onActiveChange = { isActive -> viewModel.setAlarmActive(alarm.id, isActive) },
                                onSave = { updatedAlarm, label, radius ->
                                    viewModel.updateAlarmSettings(updatedAlarm, label, radius)
                                },
                                onDeleteRequest = { deleteTarget = alarm }
                            )
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { alarm ->
        WakepointConfirmDialog(
            title = stringResource(R.string.alarm_delete_confirm_title),
            message = stringResource(R.string.alarm_delete_confirm_message),
            cancelText = stringResource(R.string.alarm_cancel),
            confirmText = stringResource(R.string.alarm_delete),
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteAlarm(alarm.id)
                if (expandedAlarmId == alarm.id) {
                    expandedAlarmId = null
                }
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun AlarmListCard(
    alarm: Alarm,
    expanded: Boolean,
    isUpdating: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenSoundList: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: (Alarm, String, String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    if (expanded) {
        EditableAlarmCard(
            alarm = alarm,
            isUpdating = isUpdating,
            onToggleExpanded = onToggleExpanded,
            onOpenSoundList = onOpenSoundList,
            onActiveChange = onActiveChange,
            onSave = onSave,
            onDelete = onDeleteRequest
        )
    } else {
        CompactAlarmCard(
            alarm = alarm,
            isUpdating = isUpdating,
            onToggleExpanded = onToggleExpanded,
            onActiveChange = onActiveChange
        )
    }
}

@Composable
private fun EditableAlarmCard(
    alarm: Alarm,
    isUpdating: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenSoundList: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: (Alarm, String, String) -> Unit,
    onDelete: () -> Unit
) {
    var radius by remember(alarm.radiusKm) { mutableStateOf(alarm.radiusKm.toRadiusOption()) }
    var label by remember(alarm.id, alarm.label) { mutableStateOf(alarm.label) }
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.alarm_list_location_prefix,
                        alarm.label
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null)
                Switch(
                    checked = alarm.isActive,
                    onCheckedChange = onActiveChange,
                    enabled = !isUpdating
                )
            }
            SectionLabel(text = stringResource(R.string.alarm_location_alias))
            WakepointTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = stringResource(R.string.alarm_location_alias),
                readOnly = isUpdating
            )
            SectionLabel(text = stringResource(R.string.alarm_radius_setting))
            RadiusSelector(
                options = ALARM_RADIUS_OPTIONS,
                selectedOption = radius,
                onSelected = { radius = it }
            )
            SectionLabel(text = stringResource(R.string.alarm_sound_setting))
            SoundOptionRow(
                title = stringResource(R.string.alarm_default_sound),
                icon = Icons.Rounded.Notifications,
                trailing = {
                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null)
                },
                onClick = onOpenSoundList
            )
            ButtonRow {
                WakepointButton(
                    text = stringResource(R.string.alarm_save),
                    enabled = !isUpdating,
                    onClick = { onSave(alarm, label, radius) },
                    modifier = Modifier.weight(1f)
                )
                WakepointSecondaryButton(
                    text = stringResource(R.string.alarm_delete),
                    enabled = !isUpdating,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactAlarmCard(
    alarm: Alarm,
    isUpdating: Boolean,
    onToggleExpanded: () -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    WakepointCard(modifier = Modifier.clickable(onClick = onToggleExpanded)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.alarm_list_location_prefix, alarm.label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = alarm.targetAddress,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(
                        R.string.alarm_radius_line,
                        if (alarm.radiusKm < 1.0) "${(alarm.radiusKm * 1000).toInt()}m" else "${alarm.radiusKm.toInt()}km"
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = alarm.isActive,
                onCheckedChange = onActiveChange,
                enabled = !isUpdating
            )
        }
    }
}

@Composable
private fun EmptyAlarmMessage() {
    WakepointCard {
        Text(
            text = stringResource(R.string.alarm_empty),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundListScreen(
    onBack: () -> Unit
) {
    var showRecording by remember { mutableStateOf(false) }
    val sounds = listOf(
        SoundItem(stringResource(R.string.sound_default_1), stringResource(R.string.sound_default_subtitle), Icons.Rounded.Notifications, true),
        SoundItem(stringResource(R.string.sound_default_2), stringResource(R.string.sound_default_subtitle), Icons.Rounded.Notifications, false),
        SoundItem(stringResource(R.string.sound_nature), stringResource(R.string.sound_nature_subtitle), Icons.Rounded.Opacity, false),
        SoundItem(stringResource(R.string.sound_rhythm), stringResource(R.string.sound_rhythm_subtitle), Icons.Rounded.MusicNote, false)
    )

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
        WakepointCard(
            modifier = Modifier.padding(16.dp)
        ) {
            sounds.forEachIndexed { index, sound ->
                SoundRow(sound = sound)
                if (index != sounds.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        ButtonRow(modifier = Modifier.padding(16.dp)) {
            WakepointSecondaryButton(
                text = stringResource(R.string.sound_import),
                icon = Icons.Rounded.FileDownload,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.sound_record),
                icon = Icons.Rounded.Mic,
                onClick = { showRecording = true },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showRecording) {
        ModalBottomSheet(
            onDismissRequest = { showRecording = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() }
        ) {
            RecordingSheet(onDismiss = { showRecording = false })
        }
    }
}

@Composable
private fun SoundRow(sound: SoundItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = sound.icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = sound.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = sound.subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        if (sound.selected) {
            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null)
        }
    }
}

@Composable
private fun RecordingSheet(onDismiss: () -> Unit) {
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
            text = stringResource(R.string.record_time),
            style = MaterialTheme.typography.headlineLarge
        )
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Rounded.Stop, contentDescription = null)
        }
        HorizontalDivider()
        ButtonRow {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.alarm_save),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun Double.toRadiusOption(): String {
    return if (this < 1.0) {
        "${(this * 1000).toInt()}m"
    } else {
        "${this.toInt()}km"
    }
}

private data class SoundItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val selected: Boolean
)
