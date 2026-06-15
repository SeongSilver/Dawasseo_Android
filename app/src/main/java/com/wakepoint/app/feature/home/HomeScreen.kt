package com.wakepoint.app.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.MapMarkerPreview
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var showAlarmSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        StylizedMap(modifier = Modifier.fillMaxSize())
        WakepointTextField(
            value = "",
            onValueChange = {},
            placeholder = stringResource(R.string.home_search_placeholder),
            leadingIcon = Icons.Rounded.Search,
            trailingIcon = Icons.Rounded.Mic,
            readOnly = true,
            modifier = Modifier
                .padding(horizontal = 28.dp, vertical = 28.dp)
                .fillMaxWidth()
        )
        MapMarkerPreview(
            modifier = Modifier.align(Alignment.Center)
        )
        FloatingActionButton(
            onClick = {},
            shape = RoundedCornerShape(14.dp),
            containerColor = WakepointCanvas,
            contentColor = WakepointInk,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .size(54.dp)
        ) {
            Icon(imageVector = Icons.Rounded.GpsFixed, contentDescription = null)
        }
        WakepointButton(
            text = stringResource(R.string.home_create_alarm),
            onClick = { showAlarmSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth()
        )
    }

    if (showAlarmSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlarmSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = 10.dp)) },
            containerColor = WakepointCanvas
        ) {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_setup_title),
                primaryButton = stringResource(R.string.alarm_add),
                onDismiss = { showAlarmSheet = false }
            )
        }
    }
}

@Composable
fun AlarmSetupSheet(
    title: String,
    primaryButton: String,
    modifier: Modifier = Modifier,
    showAddressSearch: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    var radius by remember { mutableStateOf("500m") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = title,
            color = WakepointPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (showAddressSearch) {
            SheetSectionLabel(text = stringResource(R.string.alarm_address_search))
            WakepointTextField(
                value = "",
                onValueChange = {},
                placeholder = stringResource(R.string.alarm_address_hint),
                leadingIcon = Icons.Rounded.Search
            )
        }

        SheetSectionLabel(text = stringResource(R.string.alarm_location_alias))
        WakepointTextField(
            value = stringResource(R.string.alarm_place_office),
            onValueChange = {},
            placeholder = stringResource(R.string.alarm_location_alias)
        )

        SheetSectionLabel(text = stringResource(R.string.alarm_radius_setting))
        RadiusSelector(
            options = listOf("300m", "500m", "1km"),
            selectedOption = radius,
            onSelected = { radius = it }
        )

        SheetSectionLabel(text = stringResource(R.string.alarm_sound_setting))
        SoundOptionRow(
            title = stringResource(R.string.alarm_default_sound),
            icon = Icons.Rounded.Notifications,
            trailing = {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = WakepointMuted
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = primaryButton,
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        color = WakepointInk,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun StylizedMap(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color(0xFFEAF3F7))
        drawRect(
            color = Color(0xFFB8ECF1),
            topLeft = Offset(0f, 0f),
            size = size.copy(width = size.width, height = size.height * 0.32f)
        )
        drawRect(
            color = Color(0xFFD6F3DF),
            topLeft = Offset(0f, size.height * 0.43f),
            size = size.copy(width = size.width, height = size.height * 0.16f)
        )

        val road = Path().apply {
            moveTo(size.width * 0.04f, size.height * 0.78f)
            cubicTo(
                size.width * 0.28f,
                size.height * 0.62f,
                size.width * 0.52f,
                size.height * 0.50f,
                size.width * 0.96f,
                size.height * 0.38f
            )
        }
        drawPath(road, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 28f))
        drawPath(road, Color(0xFFD2DCE7), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))

        repeat(7) { index ->
            val y = size.height * (0.2f + index * 0.1f)
            drawLine(
                color = Color(0xFFC6D2DC),
                start = Offset(0f, y),
                end = Offset(size.width, y - size.height * 0.14f),
                strokeWidth = 2f
            )
        }
        repeat(5) { index ->
            val x = size.width * (0.12f + index * 0.18f)
            drawLine(
                color = Color(0xFFC6D2DC),
                start = Offset(x, 0f),
                end = Offset(x + size.width * 0.18f, size.height),
                strokeWidth = 2f
            )
        }
    }
}
