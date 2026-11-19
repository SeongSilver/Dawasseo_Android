package com.wakepoint.app.feature.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.ButtonRow
import com.wakepoint.app.core.design.MessageText
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SectionLabel
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.feature.home.ALARM_RADIUS_OPTIONS

/**
 * 권한 있는 친구에게 목적지 알람 초대를 보내는 시트.
 * 요청자는 초대 설정값만 입력한다. 상대의 위치나 도착 정보는 어디에도 표시하지 않는다.
 */
@Composable
fun SendAlarmSheet(
    uiState: FriendsUiState,
    onLabelChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectPlace: (PlaceSearchResult) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    val target = uiState.sendTarget ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.alarm_send_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.invite_to, target.nickname),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        SectionLabel(text = stringResource(R.string.invite_destination_search))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WakepointTextField(
                value = uiState.sendSearchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = stringResource(R.string.invite_destination_hint),
                leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = if (uiState.isSendSearching) {
                    stringResource(R.string.place_searching)
                } else {
                    stringResource(R.string.place_search)
                },
                enabled = !uiState.isSendSearching,
                onClick = onSearch
            )
        }

        uiState.sendSearchResults.forEach { place ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPlace(place) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = place.name, style = MaterialTheme.typography.bodyLarge)
                    Text(text = place.address, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        SectionLabel(text = stringResource(R.string.invite_destination_selected))
        WakepointTextField(
            value = uiState.sendDestination?.let { place ->
                place.name.ifBlank { place.address }
            } ?: stringResource(R.string.invite_destination_none),
            onValueChange = {},
            placeholder = stringResource(R.string.invite_destination_selected),
            readOnly = true
        )

        SectionLabel(text = stringResource(R.string.alarm_location_alias))
        WakepointTextField(
            value = uiState.sendLabel,
            onValueChange = onLabelChange,
            placeholder = stringResource(R.string.alarm_default_label)
        )

        SectionLabel(text = stringResource(R.string.alarm_radius_setting))
        RadiusSelector(
            options = ALARM_RADIUS_OPTIONS,
            selectedOption = uiState.sendRadiusOption,
            onSelected = onRadiusChange
        )

        uiState.sendMessage?.let { message ->
            MessageText(text = message.resolve())
        }

        ButtonRow {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                enabled = !uiState.isSendingInvitation,
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = if (uiState.isSendingInvitation) {
                    stringResource(R.string.invite_sending)
                } else {
                    stringResource(R.string.alarm_send)
                },
                enabled = !uiState.isSendingInvitation && uiState.sendDestination != null,
                onClick = onSend,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FriendsMessage.resolve(): String {
    return text ?: resId?.let { stringResource(it) }.orEmpty()
}
