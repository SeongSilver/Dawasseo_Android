package com.wakepoint.app.feature.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.tasks.CancellationTokenSource
import com.wakepoint.app.R
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointMessage
import com.wakepoint.app.core.design.WakepointMessageKind
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointComponentPreviews
import com.wakepoint.app.core.design.WakepointScreenPreviews
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.data.location.PlaceSearchResult
import java.util.Locale

private val DefaultMapTarget = LatLng(37.5665, 126.9780)

/** 하단 탭 바가 차지하는 높이(시스템 내비게이션 바 제외). 하단 컨트롤을 탭 바 위로 올리는 데 사용. */
private val BOTTOM_TAB_CLEARANCE = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAlarmCreated: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val locationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var showAlarmSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(context.hasForegroundLocationPermission())
    }
    var hasNotificationPermission by remember {
        mutableStateOf(context.hasPostNotificationPermission())
    }
    var selectedTarget by remember { mutableStateOf(DefaultMapTarget) }
    var selectedTargetAddress by remember {
        mutableStateOf(context.getString(R.string.home_default_target))
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DefaultMapTarget, 15f)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted || !context.requiresPostNotificationPermission()
    }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenQuery = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()
            .trim()
        if (spokenQuery.isNotBlank()) {
            showSearchSheet = true
            viewModel.updateSearchQuery(spokenQuery)
            viewModel.searchPlaces()
        }
    }
    val startVoiceSearch: () -> Unit = {
        runCatching {
            speechLauncher.launch(context.createSpeechSearchIntent())
        }.onFailure { error ->
            if (error is ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    context.getString(R.string.place_voice_search_unavailable),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        Unit
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(FOREGROUND_LOCATION_PERMISSIONS)
        }
    }

    LaunchedEffect(uiState.saveSucceeded) {
        if (uiState.saveSucceeded) {
            showAlarmSheet = false
            viewModel.consumeSaveSuccess()
            onAlarmCreated()
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.syncLocationTracking()
            locationClient.fetchCurrentLocation(
                onSuccess = { currentLocation ->
                    selectedTarget = currentLocation
                    selectedTargetAddress = context.getString(R.string.home_current_location)
                    cameraPositionState.centerOn(currentLocation)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalWakepointColors.current.backgroundSecondary)
    ) {
        WakepointGoogleMap(
            cameraPositionState = cameraPositionState,
            selectedTarget = selectedTarget,
            isMyLocationEnabled = hasLocationPermission,
            onMapClick = { target ->
                selectedTarget = target
                val fallbackAddress = context.getString(R.string.home_map_selected_location)
                selectedTargetAddress = fallbackAddress
                cameraPositionState.centerOn(target)
                viewModel.resolveTargetAddress(
                    target = target,
                    fallback = fallbackAddress
                ) { address ->
                    if (selectedTarget == target) {
                        selectedTargetAddress = address
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    horizontal = WakepointSpacing.screenHorizontal,
                    vertical = WakepointSpacing.sm
                )
                .fillMaxWidth()
        ) {
            HomeSearchBar(
                placeholder = stringResource(R.string.home_search_placeholder),
                micContentDescription = stringResource(R.string.place_voice_search),
                onClick = { showSearchSheet = true },
                onMicClick = startVoiceSearch
            )
        }
        FloatingActionButton(
            onClick = {
                if (hasLocationPermission) {
                    locationClient.fetchCurrentLocation(
                        onSuccess = { currentLocation ->
                            selectedTarget = currentLocation
                            selectedTargetAddress = context.getString(R.string.home_current_location)
                            cameraPositionState.centerOn(currentLocation)
                        },
                        onFallback = {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(DefaultMapTarget, 15f)
                        }
                    )
                } else {
                    permissionLauncher.launch(FOREGROUND_LOCATION_PERMISSIONS)
                }
            },
            shape = WakepointShape.card,
            containerColor = LocalWakepointColors.current.surfaceElevated,
            contentColor = LocalWakepointColors.current.text,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                // FAB 는 CTA 바로 위 16 간격 (CTA bottom + 버튼높이 + 16)
                .padding(
                    end = WakepointSpacing.screenHorizontal,
                    bottom = BOTTOM_TAB_CLEARANCE + WakepointSpacing.lg + WakepointSize.buttonHeight + WakepointSpacing.lg
                )
                .size(WakepointSize.fab)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
        WakepointButton(
            text = stringResource(R.string.home_create_alarm),
            onClick = {
                if (!hasNotificationPermission && context.requiresPostNotificationPermission()) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                context.ensureFullScreenAlarmPermission()
                showAlarmSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = WakepointSpacing.screenHorizontal,
                    end = WakepointSpacing.screenHorizontal,
                    top = WakepointSpacing.lg,
                    bottom = WakepointSpacing.lg + BOTTOM_TAB_CLEARANCE
                )
                .fillMaxWidth()
        )
    }

    if (showAlarmSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlarmSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = LocalWakepointColors.current.surfaceElevated
        ) {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_setup_title),
                primaryButton = stringResource(R.string.alarm_add),
                target = selectedTarget,
                targetAddress = selectedTargetAddress,
                label = uiState.alarmLabel,
                radius = uiState.radiusOption,
                isSaving = uiState.isSavingAlarm,
                errorMessage = uiState.saveErrorMessage,
                soundTitle = uiState.selectedSoundTitle,
                soundEnabled = uiState.soundEnabled,
                vibrate = uiState.vibrate,
                noticeMessage = if (!hasNotificationPermission) {
                    stringResource(R.string.alarm_notification_permission_hint)
                } else {
                    null
                },
                onLabelChange = viewModel::updateAlarmLabel,
                onRadiusChange = viewModel::updateRadiusOption,
                onSoundEnabledChange = viewModel::updateSoundEnabled,
                onVibrateChange = viewModel::updateVibrate,
                onPrimaryClick = {
                    viewModel.saveAlarm(
                        target = selectedTarget,
                        targetAddress = selectedTargetAddress,
                        fallbackLabel = context.getString(R.string.alarm_default_label)
                    )
                },
                onDismiss = { showAlarmSheet = false }
            )
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = LocalWakepointColors.current.surfaceElevated
        ) {
            PlaceSearchSheet(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                errorMessage = uiState.searchErrorMessage,
                onQueryChange = viewModel::updateSearchQuery,
                onSearch = viewModel::searchPlaces,
                onVoiceSearch = startVoiceSearch,
                onSelectPlace = { place ->
                    val target = LatLng(place.lat, place.lng)
                    selectedTarget = target
                    selectedTargetAddress =
                        place.displayName(context.getString(R.string.place_search_result_fallback))
                    cameraPositionState.centerOn(target)
                    showSearchSheet = false
                    viewModel.clearSearch()
                }
            )
        }
    }
}

private fun CameraPositionState.centerOn(target: LatLng) {
    position = CameraPosition.fromLatLngZoom(
        target,
        maxOf(position.zoom, SELECTED_TARGET_ZOOM)
    )
}

@Composable
private fun WakepointGoogleMap(
    cameraPositionState: CameraPositionState,
    selectedTarget: LatLng,
    isMyLocationEnabled: Boolean,
    onMapClick: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // 커스텀 마커 아이콘은 Maps SDK 초기화가 끝난 뒤(onMapLoaded)에 생성한다.
    // 초기화 전에 만들면 BitmapDescriptorFactory 가 실패해 기본(빨간) 핀으로 폴백되기 때문.
    var markerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val markerState = remember { MarkerState(position = selectedTarget) }
    LaunchedEffect(selectedTarget) { markerState.position = selectedTarget }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick,
        onMapLoaded = {
            if (markerIcon == null) {
                val sizePx = (48 * context.resources.displayMetrics.density).toInt()
                markerIcon = context.vectorToBitmapDescriptor(R.drawable.logo_point, sizePx)
            }
        },
        properties = MapProperties(
            isBuildingEnabled = true,
            isTrafficEnabled = false,
            isMyLocationEnabled = isMyLocationEnabled
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    ) {
        // 우리 심볼 아이콘이 준비된 뒤에만 마커를 그린다 → 기본 빨간 핀이 절대 노출되지 않음.
        markerIcon?.let { icon ->
            Marker(
                state = markerState,
                title = stringResource(R.string.home_map_default_marker),
                icon = icon,
                anchor = androidx.compose.ui.geometry.Offset(0.5f, 1f)
            )
        }
    }
}

/** Renders a (vector) drawable resource into a marker BitmapDescriptor. */
private fun Context.vectorToBitmapDescriptor(resId: Int, sizePx: Int): BitmapDescriptor? = runCatching {
    // 혹시 초기화 전이라도 안전하도록 명시적으로 초기화.
    com.google.android.gms.maps.MapsInitializer.initialize(this)
    val drawable = ContextCompat.getDrawable(this, resId) ?: return@runCatching null
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)
    BitmapDescriptorFactory.fromBitmap(bitmap)
}.getOrNull()

@Composable
fun AlarmSetupSheet(
    title: String,
    primaryButton: String,
    target: LatLng = DefaultMapTarget,
    targetAddress: String = "",
    modifier: Modifier = Modifier,
    label: String = "",
    radius: String = "500m",
    isSaving: Boolean = false,
    errorMessage: String? = null,
    noticeMessage: String? = null,
    soundTitle: String = stringResource(R.string.alarm_default_sound),
    soundEnabled: Boolean? = null,
    vibrate: Boolean? = null,
    showAddressSearch: Boolean = false,
    addressQuery: String = "",
    addressResults: List<PlaceSearchResult> = emptyList(),
    isSearchingAddress: Boolean = false,
    addressMessage: String? = null,
    onAddressQueryChange: (String) -> Unit = {},
    onAddressSearch: () -> Unit = {},
    onSelectAddress: (PlaceSearchResult) -> Unit = {},
    onLabelChange: (String) -> Unit = {},
    onRadiusChange: (String) -> Unit = {},
    onSoundEnabledChange: (Boolean) -> Unit = {},
    onVibrateChange: (Boolean) -> Unit = {},
    onPrimaryClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        // 본문만 스크롤한다. 하단 버튼 줄은 스크롤 밖 고정 (DESIGN.md 7절).
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = WakepointSpacing.formHorizontal,
                    end = WakepointSpacing.formHorizontal,
                    bottom = WakepointSpacing.xl
                ),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
        ) {
            BottomSheetTitle(title = title)

            if (showAddressSearch) {
                WakepointTextField(
                    value = addressQuery,
                    onValueChange = onAddressQueryChange,
                    placeholder = stringResource(R.string.alarm_address_search),
                    leadingIcon = Icons.Rounded.Search,
                    filled = true
                )
                WakepointButton(
                    text = if (isSearchingAddress) {
                        stringResource(R.string.place_searching)
                    } else {
                        stringResource(R.string.place_search)
                    },
                    enabled = !isSearchingAddress && addressQuery.isNotBlank(),
                    onClick = onAddressSearch,
                    modifier = Modifier.fillMaxWidth()
                )
                if (addressMessage != null) {
                    WakepointMessage(
                        text = addressMessage,
                        kind = WakepointMessageKind.InfoOnSheet
                    )
                }
                if (addressResults.isNotEmpty()) {
                    SheetRowGroup {
                        addressResults.forEachIndexed { index, place ->
                            if (index > 0) WakepointDivider()
                            PlaceResultRow(place = place, onClick = { onSelectAddress(place) })
                        }
                    }
                }
            }

            WakepointTextField(
                value = label,
                onValueChange = onLabelChange,
                placeholder = stringResource(R.string.alarm_location_alias),
                supportingText = stringResource(R.string.alarm_location_alias_hint),
                filled = true
            )

            SheetSection(label = stringResource(R.string.alarm_selected_target)) {
                SheetRowGroup {
                    SheetReadOnlyRow(
                        icon = Icons.Rounded.LocationOn,
                        title = targetAddress.ifBlank {
                            stringResource(R.string.alarm_selected_target_unknown)
                        },
                        subtitle = stringResource(R.string.home_map_selected_location)
                    )
                }
            }

            RadiusSelector(
                options = ALARM_RADIUS_OPTIONS,
                selectedOption = radius,
                onSelected = onRadiusChange,
                label = stringResource(R.string.alarm_radius_setting),
                filled = true
            )

            if (soundEnabled != null) {
                SheetToggleRow(
                    title = stringResource(R.string.alarm_sound_toggle),
                    hint = stringResource(R.string.alarm_sound_toggle_hint),
                    checked = soundEnabled,
                    onCheckedChange = onSoundEnabledChange
                )
            }

            if (soundEnabled != false) {
                SheetSection(label = stringResource(R.string.alarm_sound_setting)) {
                    SheetRowGroup {
                        SheetReadOnlyRow(
                            icon = Icons.AutoMirrored.Rounded.VolumeUp,
                            title = soundTitle,
                            subtitle = stringResource(R.string.alarm_sound_hint),
                            trailingIcon = Icons.Rounded.ChevronRight
                        )
                    }
                }
            }

            if (vibrate != null) {
                SheetToggleRow(
                    title = stringResource(R.string.alarm_vibrate_setting),
                    hint = stringResource(R.string.alarm_vibrate_hint),
                    checked = vibrate,
                    onCheckedChange = onVibrateChange
                )
            }

            // 알림 권한 안내만 cautionary 를 쓴다 (DESIGN.md 5절).
            if (noticeMessage != null) {
                WakepointMessage(
                    text = noticeMessage,
                    kind = WakepointMessageKind.Caution
                )
            }

            if (errorMessage != null) {
                WakepointMessage(
                    text = errorMessage,
                    kind = WakepointMessageKind.Error
                )
            }
        }

        WakepointDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = WakepointSpacing.formHorizontal,
                    end = WakepointSpacing.formHorizontal,
                    top = WakepointSpacing.md,
                    bottom = WakepointSpacing.md
                ),
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
        ) {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.weight(SHEET_CANCEL_WEIGHT)
            )
            WakepointButton(
                text = if (isSaving) stringResource(R.string.alarm_saving) else primaryButton,
                onClick = onPrimaryClick,
                enabled = !isSaving,
                modifier = Modifier.weight(SHEET_CONFIRM_WEIGHT)
            )
        }
    }
}

/** 시트 하단 버튼 비율 — 취소 1 : 실행 1.4 (design-handoff `Sheet-default`). */
private const val SHEET_CANCEL_WEIGHT = 1f
private const val SHEET_CONFIRM_WEIGHT = 1.4f

/** 시트 안 묶음 행 컨테이너 — 모서리 16, 배경 `surfaceElevatedSecondary`. */
@Composable
private fun SheetRowGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .background(LocalWakepointColors.current.surfaceElevatedSecondary),
        content = content
    )
}

/** 시트 안 읽기 전용 행 — 최소 높이 64, 좌우 16, 아이콘 24 + 제목/부제. */
@Composable
private fun SheetReadOnlyRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = WakepointSize.soundRowHeight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = WakepointSpacing.lg, vertical = WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(WakepointSize.icon)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
    }
}

/** 시트 섹션: 섹션 타이틀(T2) + 콘텐츠를 라벨↔콘텐츠 8 간격으로 묶는다. */
@Composable
private fun SheetSection(
    label: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        SheetSectionLabel(text = label)
        content()
    }
}

/** 토글 행: 제목(15/SemiBold) + 보조설명(T5) + 스위치, 높이 56. */
@Composable
private fun SheetToggleRow(
    title: String,
    hint: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = LocalWakepointColors.current.text,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = hint,
                color = LocalWakepointColors.current.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        WakepointSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * 장소 검색 시트 (design-handoff `Place-idle` · `-searching` · `-results` · `-none` · `-error`, 명세 4.2).
 * 시트 안이므로 채움 필드와 `surfaceElevatedSecondary` 안내를 쓴다.
 */
@Composable
private fun PlaceSearchSheet(
    query: String,
    results: List<PlaceSearchResult>,
    isSearching: Boolean,
    errorMessage: String?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVoiceSearch: () -> Unit,
    onSelectPlace: (PlaceSearchResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = WakepointSpacing.formHorizontal,
                end = WakepointSpacing.formHorizontal,
                bottom = WakepointSpacing.xxxl
            ),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        BottomSheetTitle(title = stringResource(R.string.place_search_title))
        WakepointTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.place_search_field_label),
            leadingIcon = Icons.Rounded.Search,
            trailingIcon = Icons.Rounded.Mic,
            trailingIconContentDescription = stringResource(R.string.place_voice_search),
            onTrailingIconClick = onVoiceSearch,
            filled = true
        )
        WakepointButton(
            text = if (isSearching) {
                stringResource(R.string.place_searching)
            } else {
                stringResource(R.string.place_search)
            },
            enabled = !isSearching,
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            errorMessage != null -> WakepointMessage(
                text = errorMessage,
                kind = WakepointMessageKind.Error
            )
            !isSearching && query.isNotBlank() && results.isEmpty() -> WakepointMessage(
                text = stringResource(R.string.place_no_result),
                kind = WakepointMessageKind.InfoOnSheet
            )
        }

        if (results.isNotEmpty()) {
            SheetRowGroup {
                results.forEachIndexed { index, place ->
                    if (index > 0) WakepointDivider()
                    PlaceResultRow(place = place, onClick = { onSelectPlace(place) })
                }
            }
        }
    }
}

/** 장소 검색 결과 행 — 아이콘 location_on `textSecondary`, 제목 bodyLarge, 주소 bodySmall. */
@Composable
private fun PlaceResultRow(
    place: PlaceSearchResult,
    onClick: () -> Unit
) {
    SheetReadOnlyRow(
        icon = Icons.Rounded.LocationOn,
        title = place.name,
        subtitle = place.address,
        onClick = onClick
    )
}

/** 지도 위 검색바 (design-handoff `Home`) — 높이 56, 완전 원형, `surfaceElevated`. 그림자 없음. */
@Composable
private fun HomeSearchBar(
    placeholder: String,
    micContentDescription: String,
    onClick: () -> Unit,
    onMicClick: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.searchBarHeight),
        shape = WakepointShape.full,
        color = colors.surfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(horizontal = WakepointSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
            Text(
                text = placeholder,
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = micContentDescription,
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(WakepointSize.icon)
                    .clickable(onClick = onMicClick)
            )
        }
    }
}

/** 시트 섹션 라벨 — labelLarge(14/20 · 500), `textSecondary`. */
@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        color = LocalWakepointColors.current.textSecondary,
        style = MaterialTheme.typography.labelLarge
    )
}

private val FOREGROUND_LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private const val SELECTED_TARGET_ZOOM = 16f

private fun Context.hasForegroundLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.requiresPostNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}

private fun Context.hasPostNotificationPermission(): Boolean {
    return !requiresPostNotificationPermission() ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
}

/**
 * 실제 알람처럼 도착 시 알람 화면이 즉시 뜨도록 필요한 특수 권한을 확인/요청한다.
 * 두 권한은 시스템 설정 화면으로 각각 이동해야 해서, 한 번에 하나씩만 안내한다.
 *
 * 1) 전체화면 알림 권한(Android 14+): 없으면 도착 알림이 헤드업으로만 표시됨.
 * 2) 다른 앱 위에 표시(SYSTEM_ALERT_WINDOW): 백그라운드/다른 앱 사용 중에도
 *    알람 화면을 곧바로 실행하기 위한 백그라운드 액티비티 시작 예외.
 */
private fun Context.ensureFullScreenAlarmPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager != null && !notificationManager.canUseFullScreenIntent()) {
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(Uri.fromParts("package", packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            return
        }
    }

    if (!Settings.canDrawOverlays(this)) {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    .setData(Uri.fromParts("package", packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

private fun FusedLocationProviderClient.fetchCurrentLocation(
    onSuccess: (LatLng) -> Unit,
    onFallback: () -> Unit = {}
) {
    runCatching {
        getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        )
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(LatLng(location.latitude, location.longitude))
                } else {
                    onFallback()
                }
            }
            .addOnFailureListener {
                onFallback()
            }
    }.onFailure {
        onFallback()
    }
}

private fun PlaceSearchResult.displayName(fallback: String): String {
    return name.ifBlank { address.ifBlank { fallback } }
}

private fun Context.createSpeechSearchIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.place_voice_search_prompt))
    }
}

// ─────────────────────────────────────────────────────────────────────
// @Preview — design-handoff 의 상태별 화면
// ─────────────────────────────────────────────────────────────────────

@WakepointComponentPreviews
@Composable
private fun SheetDefaultPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_setup_title),
                primaryButton = stringResource(R.string.alarm_add),
                targetAddress = "서울 강남구 테헤란로 152"
            )
        }
    }
}

@WakepointComponentPreviews
@Composable
private fun SheetNoPermPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_setup_title),
                primaryButton = stringResource(R.string.alarm_add),
                targetAddress = "서울 강남구 테헤란로 152",
                noticeMessage = stringResource(R.string.alarm_notification_permission_hint)
            )
        }
    }
}

@WakepointComponentPreviews
@Composable
private fun SheetSavingPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_setup_title),
                primaryButton = stringResource(R.string.alarm_add),
                targetAddress = "서울 강남구 테헤란로 152",
                label = "회사",
                isSaving = true
            )
        }
    }
}

@WakepointComponentPreviews
@Composable
private fun SheetErrorPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_setup_title),
                primaryButton = stringResource(R.string.alarm_add),
                targetAddress = "서울 강남구 테헤란로 152",
                label = "회사",
                errorMessage = "알람을 저장하지 못했습니다. 잠시 뒤 다시 눌러 주세요."
            )
        }
    }
}

@WakepointComponentPreviews
@Composable
private fun SheetFriendPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_send_title),
                primaryButton = stringResource(R.string.alarm_send),
                targetAddress = "서울 강남구 테헤란로 152",
                showAddressSearch = true,
                addressQuery = "테헤란로"
            )
        }
    }
}

@WakepointComponentPreviews
@Composable
private fun PlaceIdlePreview() {
    WakepointTheme { PreviewSheetSurface { PreviewPlaceSheet() } }
}

@WakepointComponentPreviews
@Composable
private fun PlaceSearchingPreview() {
    WakepointTheme { PreviewSheetSurface { PreviewPlaceSheet(query = "서울역", isSearching = true) } }
}

@WakepointComponentPreviews
@Composable
private fun PlaceResultsPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            PreviewPlaceSheet(query = "서울역", results = PREVIEW_PLACES)
        }
    }
}

@WakepointComponentPreviews
@Composable
private fun PlaceNonePreview() {
    WakepointTheme { PreviewSheetSurface { PreviewPlaceSheet(query = "서울역") } }
}

@WakepointComponentPreviews
@Composable
private fun PlaceErrorPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            PreviewPlaceSheet(
                query = "서울역",
                errorMessage = "장소를 찾지 못했습니다. 네트워크를 확인하고 다시 검색해 주세요."
            )
        }
    }
}

@WakepointScreenPreviews
@Composable
private fun HomeChromePreview() {
    WakepointTheme { PreviewHomeChrome() }
}

/** 시트 컨테이너(모서리 28 · `surfaceElevated` · 드래그 핸들)를 미리보기에서 재현한다. */
@Composable
private fun PreviewSheetSurface(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.sheet)
            .background(LocalWakepointColors.current.surfaceElevated)
    ) {
        BottomSheetHandle()
        content()
    }
}

@Composable
private fun PreviewPlaceSheet(
    query: String = "",
    results: List<PlaceSearchResult> = emptyList(),
    isSearching: Boolean = false,
    errorMessage: String? = null
) {
    PlaceSearchSheet(
        query = query,
        results = results,
        isSearching = isSearching,
        errorMessage = errorMessage,
        onQueryChange = {},
        onSearch = {},
        onVoiceSearch = {},
        onSelectPlace = {}
    )
}

/** 홈 · 홈(권한 없음) 의 지도 위 UI. 지도 자체는 미리보기에서 그려지지 않아 바탕색으로 둔다. */
@Composable
private fun PreviewHomeChrome() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalWakepointColors.current.backgroundSecondary)
    ) {
        Box(
            modifier = Modifier
                .padding(
                    horizontal = WakepointSpacing.screenHorizontal,
                    vertical = WakepointSpacing.sm
                )
                .fillMaxWidth()
        ) {
            HomeSearchBar(
                placeholder = stringResource(R.string.home_search_placeholder),
                micContentDescription = stringResource(R.string.place_voice_search),
                onClick = {},
                onMicClick = {}
            )
        }
        WakepointButton(
            text = stringResource(R.string.home_create_alarm),
            icon = Icons.Rounded.Add,
            onClick = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = WakepointSpacing.screenHorizontal,
                    end = WakepointSpacing.screenHorizontal,
                    bottom = WakepointSpacing.lg + BOTTOM_TAB_CLEARANCE
                )
                .fillMaxWidth()
        )
    }
}

private val PREVIEW_PLACES = listOf(
    PlaceSearchResult("1", "서울역", "서울 용산구 한강대로 405", 37.5547, 126.9707),
    PlaceSearchResult("2", "서울역 버스환승센터", "서울 중구 봉래동2가 122", 37.5553, 126.9723),
    PlaceSearchResult("3", "서울로7017", "서울 중구 청파로 432", 37.5560, 126.9713)
)
