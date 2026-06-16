package com.wakepoint.app.feature.alarms

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = WakepointMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.alarms.isEmpty()) {
                EmptyAlarmMessage()
            } else {
                if (uiState.activeAlarms.isNotEmpty()) {
                    SectionTitle(text = stringResource(R.string.alarm_active))
                    EditableAlarmCard(
                        alarm = uiState.activeAlarms.first(),
                        isUpdating = uiState.isUpdating,
                        onOpenSoundList = onOpenSoundList,
                        onActiveChange = { isActive ->
                            viewModel.setAlarmActive(uiState.activeAlarms.first().id, isActive)
                        },
                        onDelete = {
                            viewModel.deleteAlarm(uiState.activeAlarms.first().id)
                        }
                    )
                    uiState.activeAlarms.drop(1).forEach { alarm ->
                        CompactAlarmCard(
                            alarm = alarm,
                            isUpdating = uiState.isUpdating,
                            onActiveChange = { isActive -> viewModel.setAlarmActive(alarm.id, isActive) },
                            onDelete = { viewModel.deleteAlarm(alarm.id) }
                        )
                    }
                }

                if (uiState.inactiveAlarms.isNotEmpty()) {
                    SectionTitle(text = stringResource(R.string.alarm_inactive))
                    uiState.inactiveAlarms.forEach { alarm ->
                        CompactAlarmCard(
                            alarm = alarm,
                            isUpdating = uiState.isUpdating,
                            onActiveChange = { isActive -> viewModel.setAlarmActive(alarm.id, isActive) },
                            onDelete = { viewModel.deleteAlarm(alarm.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableAlarmCard(
    alarm: Alarm,
    isUpdating: Boolean,
    onOpenSoundList: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var radius by remember(alarm.radiusKm) { mutableStateOf(alarm.radiusKm.toRadiusOption()) }
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.alarm_list_location_prefix,
                        stringResource(R.string.alarm_transfer_center)
                    ),
                    color = WakepointPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = alarm.isActive,
                    onCheckedChange = onActiveChange,
                    enabled = !isUpdating,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WakepointPrimary
                    )
                )
            }
            Text(
                text = alarm.label,
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
            SectionLabel(text = stringResource(R.string.alarm_location_alias))
            WakepointTextField(
                value = alarm.targetAddress,
                onValueChange = {},
                placeholder = stringResource(R.string.alarm_location_alias),
                readOnly = true
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
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = WakepointMuted
                    )
                },
                onClick = onOpenSoundList
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WakepointButton(
                    text = stringResource(R.string.alarm_save),
                    enabled = !isUpdating,
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
    onActiveChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    WakepointCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.alarm_list_location_prefix, alarm.label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (alarm.isActive) WakepointInk else WakepointMuted
                )
                Text(
                    text = alarm.targetAddress,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (alarm.isActive) WakepointInk else WakepointMuted
                )
                Text(
                    text = stringResource(
                        R.string.alarm_radius_line,
                        if (alarm.radiusKm < 1.0) "${(alarm.radiusKm * 1000).toInt()}m" else "${alarm.radiusKm.toInt()}km"
                    ),
                    color = WakepointMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = alarm.isActive,
                onCheckedChange = onActiveChange,
                enabled = !isUpdating,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = WakepointPrimary
                )
            )
            IconButton(
                onClick = onDelete,
                enabled = !isUpdating
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.alarm_delete),
                    tint = WakepointMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyAlarmMessage() {
    WakepointCard {
        Text(
            text = stringResource(R.string.alarm_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = WakepointMuted
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = WakepointInk
    )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(WakepointCanvas)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.alarm_sound_list_title),
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
        }
        WakepointCard(
            modifier = Modifier.padding(20.dp)
        ) {
            sounds.forEachIndexed { index, sound ->
                SoundRow(sound = sound)
                if (index != sounds.lastIndex) {
                    HorizontalDivider(color = WakepointParchment)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WakepointCanvas)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = 10.dp)) },
            containerColor = WakepointCanvas
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
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = WakepointParchment
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = sound.icon, contentDescription = null)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = sound.title, style = MaterialTheme.typography.bodyLarge, color = WakepointInk)
            Text(text = sound.subtitle, style = MaterialTheme.typography.bodyMedium, color = WakepointMuted)
        }
        if (sound.selected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun RecordingSheet(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text(
            text = stringResource(R.string.record_title),
            color = WakepointPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.record_time),
            color = WakepointPrimary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, WakepointPrimary),
            color = WakepointCanvas
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = null,
                    tint = WakepointPrimary
                )
            }
        }
        HorizontalDivider(color = WakepointParchment)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = WakepointInk)
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
