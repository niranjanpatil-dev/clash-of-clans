package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fort
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.data.entity.TownHallEntity
import com.example.ui.ClashUiState
import com.example.ui.ClashViewModel
import com.example.ui.SyncUiState
import com.example.ui.dialogs.AddEditBuildingDialog
import com.example.ui.dialogs.AddEditBuildingTypeDialog
import com.example.ui.dialogs.AddEditTownHallDialog
import com.example.ui.dialogs.DeleteConfirmDialog
import com.example.ui.dialogs.SyncDiffDialog
import com.example.ui.screens.BuildingTypeMasterScreen
import com.example.ui.screens.BuildingsMasterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TownHallMasterScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.ClashTrackerTheme
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ClashDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    BUILDINGS("Buildings", Icons.Default.Apartment),
    TOWN_HALLS("Town Hall", Icons.Default.Fort),
    BUILDING_TYPES("Categories", Icons.Default.Category)
}

sealed class DeleteTarget {
    data class Building(val entity: BuildingEntity) : DeleteTarget()
    data class TownHall(val entity: TownHallEntity) : DeleteTarget()
    data class BuildingType(val entity: BuildingTypeEntity) : DeleteTarget()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClashTrackerTheme {
                ClashApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClashApp(viewModel: ClashViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by remember { mutableStateOf(ClashDestination.DASHBOARD) }

    // Dialog States
    var showAddBuilding by remember { mutableStateOf(false) }
    var buildingToEdit by remember { mutableStateOf<BuildingEntity?>(null) }

    var showAddTownHall by remember { mutableStateOf(false) }
    var townHallToEdit by remember { mutableStateOf<TownHallEntity?>(null) }

    var showAddType by remember { mutableStateOf(false) }
    var typeToEdit by remember { mutableStateOf<BuildingTypeEntity?>(null) }

    var itemToDelete by remember { mutableStateOf<DeleteTarget?>(null) }

    // Handle user feedback notices
    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    // Handle back button when not on Dashboard
    BackHandler(enabled = currentDestination != ClashDestination.DASHBOARD) {
        currentDestination = ClashDestination.DASHBOARD
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(LightBackground)) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = LightBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(BluePrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Clash Tracker",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = if (uiState.village == "HOME") "Home Village • ${uiState.activeTownHall?.name ?: "No Level Selected"}"
                                               else "Builder Base • ${uiState.activeTownHall?.name ?: "No Level Selected"}",
                                        color = BluePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LightSurface
                        )
                    )
                    HorizontalDivider(color = LightBorder)
                }
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    Column {
                        HorizontalDivider(color = LightBorder)
                        NavigationBar(
                            containerColor = LightSurface,
                            contentColor = TextPrimary
                        ) {
                            ClashDestination.values().forEach { destination ->
                                val isSelected = currentDestination == destination
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = destination.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BluePrimary,
                                        selectedTextColor = BluePrimary,
                                        indicatorColor = BluePrimaryContainer,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Expanded Screen Side Navigation Rail
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = LightSurface,
                        contentColor = TextPrimary,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ClashDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = BluePrimary,
                                    selectedTextColor = BluePrimary,
                                    indicatorColor = BluePrimaryContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
                            )
                        }
                    }
                }

                // Main Screen Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentDestination) {
                        ClashDestination.DASHBOARD -> {
                            DashboardScreen(
                                uiState = uiState,
                                onSwitchVillage = { viewModel.switchVillage(it) },
                                onTriggerSync = { viewModel.triggerSearchGroundingSync() },
                                onNavigateToTownHalls = { currentDestination = ClashDestination.TOWN_HALLS },
                                onNavigateToBuildings = { typeId ->
                                    viewModel.setSelectedCategory(typeId)
                                    currentDestination = ClashDestination.BUILDINGS
                                },
                                onAddBuildingClick = { showAddBuilding = true },
                                onAddCategoryClick = { showAddType = true }
                            )
                        }
                        ClashDestination.BUILDINGS -> {
                            BuildingsMasterScreen(
                                uiState = uiState,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onCategorySelect = { viewModel.setSelectedCategory(it) },
                                onSortSelect = { viewModel.setSortOption(it) },
                                onAddBuildingClick = { showAddBuilding = true },
                                onEditBuildingClick = { buildingToEdit = it },
                                onDeleteBuildingClick = { itemToDelete = DeleteTarget.Building(it) },
                                onIncrementLevel = { viewModel.incrementBuildingLevel(it) },
                                onDecrementLevel = { viewModel.decrementBuildingLevel(it) }
                            )
                        }
                        ClashDestination.TOWN_HALLS -> {
                            TownHallMasterScreen(
                                uiState = uiState,
                                onSetActive = { th -> viewModel.setActiveTownHall(th) },
                                onAddClick = { showAddTownHall = true },
                                onEditClick = { townHallToEdit = it },
                                onDeleteClick = { itemToDelete = DeleteTarget.TownHall(it) }
                            )
                        }
                        ClashDestination.BUILDING_TYPES -> {
                            BuildingTypeMasterScreen(
                                uiState = uiState,
                                onAddClick = { showAddType = true },
                                onEditClick = { typeToEdit = it },
                                onDeleteClick = { itemToDelete = DeleteTarget.BuildingType(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Search Grounding Diff Preview Dialog (Available on manual sync)
    val syncState = uiState.syncUiState
    if (syncState is SyncUiState.DiffPreview) {
        SyncDiffDialog(
            targetTownHall = syncState.targetTownHall,
            diffs = syncState.diffs,
            sources = syncState.sources,
            searchQueries = syncState.searchQueries,
            summary = syncState.summary,
            onApply = {
                viewModel.applyConfirmedDiffs(
                    targetTownHall = syncState.targetTownHall,
                    diffs = syncState.diffs,
                    sources = syncState.sources
                )
            },
            onDismiss = { viewModel.dismissSyncState() }
        )
    }

    // Search Grounding Error Dialog
    if (syncState is SyncUiState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncState() },
            title = {
                Text(
                    text = "Search Grounding Notice",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = syncState.message,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSyncState() },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = Color.White)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = LightSurface
        )
    }

    // Search Grounding Success Dialog
    if (syncState is SyncUiState.Success) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncState() },
            title = {
                Text(
                    text = "Synchronization Complete",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = syncState.message,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSyncState() },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = Color.White)
                ) {
                    Text("Great", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = LightSurface
        )
    }

    // Add / Edit Building Dialog
    if (showAddBuilding || buildingToEdit != null) {
        AddEditBuildingDialog(
            buildingToEdit = buildingToEdit,
            availableTypes = uiState.buildingTypes,
            onSave = { typeId, name, notes ->
                if (buildingToEdit == null) {
                    viewModel.addBuilding(typeId, name, 1, 1, notes)
                } else {
                    viewModel.updateBuilding(
                        buildingToEdit!!.copy(
                            typeId = typeId,
                            name = name,
                            notes = notes
                        )
                    )
                }
                showAddBuilding = false
                buildingToEdit = null
            },
            onDismiss = {
                showAddBuilding = false
                buildingToEdit = null
            }
        )
    }

    // Add / Edit Town Hall Dialog
    if (showAddTownHall || townHallToEdit != null) {
        AddEditTownHallDialog(
            townHallToEdit = townHallToEdit,
            village = uiState.village,
            onSave = { level, name, makeActive ->
                if (townHallToEdit == null) {
                    viewModel.addTownHall(level, name, makeActive)
                } else {
                    val updated = townHallToEdit!!.copy(level = level, name = name)
                    viewModel.updateTownHall(updated)
                    if (makeActive) {
                        viewModel.setActiveTownHall(updated)
                    }
                }
                showAddTownHall = false
                townHallToEdit = null
            },
            onDismiss = {
                showAddTownHall = false
                townHallToEdit = null
            }
        )
    }

    // Add / Edit Building Type Dialog
    if (showAddType || typeToEdit != null) {
        AddEditBuildingTypeDialog(
            typeToEdit = typeToEdit,
            onSave = { name, iconKey ->
                if (typeToEdit == null) {
                    viewModel.addBuildingType(name, iconKey)
                } else {
                    viewModel.updateBuildingType(typeToEdit!!.copy(name = name, iconKey = iconKey))
                }
                showAddType = false
                typeToEdit = null
            },
            onDismiss = {
                showAddType = false
                typeToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { target ->
        val (title, message) = when (target) {
            is DeleteTarget.Building -> "Delete Building" to "Are you sure you want to delete '${target.entity.name}'?"
            is DeleteTarget.TownHall -> "Delete Town Hall" to "Are you sure you want to delete '${target.entity.name}'?"
            is DeleteTarget.BuildingType -> "Delete Category" to "Are you sure you want to delete '${target.entity.name}'? Any buildings in this category will also be removed."
        }

        DeleteConfirmDialog(
            title = title,
            message = message,
            onConfirm = {
                when (target) {
                    is DeleteTarget.Building -> viewModel.deleteBuilding(target.entity)
                    is DeleteTarget.TownHall -> viewModel.deleteTownHall(target.entity)
                    is DeleteTarget.BuildingType -> viewModel.deleteBuildingType(target.entity)
                }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}
