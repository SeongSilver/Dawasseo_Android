package com.wakepoint.app.feature.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.speech.RecognizerIntent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.wakepoint.app.data.location.PlaceSearchResult
import java.util.Locale

private val DefaultMapTarget = LatLng(37.5665, 126.9780)

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
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 16f)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        WakepointGoogleMap(
            cameraPositionState = cameraPositionState,
            selectedTarget = selectedTarget,
            isMyLocationEnabled = hasLocationPermission,
            onMapClick = { target ->
                selectedTarget = target
                val fallbackAddress = context.getString(R.string.home_map_selected_location)
                selectedTargetAddress = fallbackAddress
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
                .padding(horizontal = 28.dp, vertical = 28.dp)
                .fillMaxWidth()
        ) {
            HomeSearchBar(
                placeholder = stringResource(R.string.home_search_placeholder),
                micContentDescription = stringResource(R.string.place_voice_search),
                onClick = { showSearchSheet = true },
                onMicClick = startVoiceSearch
            )
        }
        MapMarkerPreview(
            modifier = Modifier.align(Alignment.Center)
        )
        FloatingActionButton(
            onClick = {
                if (hasLocationPermission) {
                    locationClient.fetchCurrentLocation(
                        onSuccess = { currentLocation ->
                            selectedTarget = currentLocation
                            selectedTargetAddress = context.getString(R.string.home_current_location)
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 16f)
                        },
                        onFallback = {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(DefaultMapTarget, 15f)
                        }
                    )
                } else {
                    permissionLauncher.launch(FOREGROUND_LOCATION_PERMISSIONS)
                }
            },
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
            onClick = {
                if (!hasNotificationPermission && context.requiresPostNotificationPermission()) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                showAlarmSheet = true
            },
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
                target = selectedTarget,
                targetAddress = selectedTargetAddress,
                label = uiState.alarmLabel,
                radius = uiState.radiusOption,
                isSaving = uiState.isSavingAlarm,
                errorMessage = uiState.saveErrorMessage,
                noticeMessage = if (!hasNotificationPermission) {
                    stringResource(R.string.alarm_notification_permission_hint)
                } else {
                    null
                },
                onLabelChange = viewModel::updateAlarmLabel,
                onRadiusChange = viewModel::updateRadiusOption,
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
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = 10.dp)) },
            containerColor = WakepointCanvas
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
                    selectedTargetAddress = place.displayName()
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 16f)
                    showSearchSheet = false
                    viewModel.clearSearch()
                }
            )
        }
    }
}

@Composable
private fun WakepointGoogleMap(
    cameraPositionState: CameraPositionState,
    selectedTarget: LatLng,
    isMyLocationEnabled: Boolean,
    onMapClick: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick,
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
        Marker(
            state = MarkerState(position = selectedTarget),
            title = stringResource(R.string.home_map_default_marker)
        )
    }
}

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
    showAddressSearch: Boolean = false,
    onLabelChange: (String) -> Unit = {},
    onRadiusChange: (String) -> Unit = {},
    onPrimaryClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
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
            value = label,
            onValueChange = onLabelChange,
            placeholder = stringResource(R.string.alarm_default_label)
        )

        SheetSectionLabel(text = stringResource(R.string.alarm_selected_target))
        WakepointTextField(
            value = targetAddress.ifBlank {
                stringResource(R.string.alarm_selected_target_unknown)
            },
            onValueChange = {},
            placeholder = stringResource(R.string.alarm_selected_target),
            readOnly = true
        )

        SheetSectionLabel(text = stringResource(R.string.alarm_radius_setting))
        RadiusSelector(
            options = ALARM_RADIUS_OPTIONS,
            selectedOption = radius,
            onSelected = onRadiusChange
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

        if (noticeMessage != null) {
            Text(
                text = noticeMessage,
                color = WakepointMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = if (isSaving) stringResource(R.string.alarm_saving) else primaryButton,
                onClick = onPrimaryClick,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = WakepointMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

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
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.place_search_title),
            color = WakepointPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        WakepointTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.home_search_placeholder),
            leadingIcon = Icons.Rounded.Search,
            trailingIcon = Icons.Rounded.Mic,
            trailingIconContentDescription = stringResource(R.string.place_voice_search),
            onTrailingIconClick = onVoiceSearch
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

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted
            )
        }

        results.forEach { place ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPlace(place) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = WakepointPrimary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = WakepointInk
                    )
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WakepointMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSearchBar(
    placeholder: String,
    micContentDescription: String,
    onClick: () -> Unit,
    onMicClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = WakepointMuted
            )
            Text(
                text = placeholder,
                color = WakepointMuted,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = micContentDescription,
                    tint = WakepointMuted
                )
            }
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

private val FOREGROUND_LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

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

private fun PlaceSearchResult.displayName(): String {
    return name.ifBlank { address.ifBlank { "검색 결과" } }
}

private fun Context.createSpeechSearchIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.place_voice_search_prompt))
    }
}


