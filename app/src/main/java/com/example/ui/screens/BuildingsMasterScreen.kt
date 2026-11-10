package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.ui.ClashUiState
import com.example.ui.SortOption
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BlueOnPrimaryContainer
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RedDangerContainer
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SlateSecondaryContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BuildingsMasterScreen(
    uiState: ClashUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Long?) -> Unit,
    onSortSelect: (SortOption) -> Unit,
    onAddBuildingClick: () -> Unit,
    onEditBuildingClick: (BuildingEntity) -> Unit,
    onDeleteBuildingClick: (BuildingEntity) -> Unit,
    onManageCountsClick: (BuildingEntity) -> Unit,
    onScheduleUpgradeClick: (BuildingEntity) -> Unit,
    onIncrementLevel: (BuildingEntity) -> Unit,
    onDecrementLevel: (BuildingEntity) -> Unit
) {
    val buildings = uiState.filteredBuildings
    val types = uiState.buildingTypes
    val typeMap = types.associateBy { it.id }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LightBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBuildingClick,
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_building_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Building")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Search Bar & Sort Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search buildings...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_buildings_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Dropdown Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightSurface)
                            .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                            .clickable { sortMenuExpanded = true }
                            .testTag("sort_buildings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = BluePrimary
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(LightSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name (A to Z)", color = TextPrimary) },
                            onClick = {
                                onSortSelect(SortOption.NAME)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Current Level (Highest)", color = TextPrimary) },
                            onClick = {
                                onSortSelect(SortOption.CURRENT_LEVEL_DESC)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Max Level (Highest)", color = TextPrimary) },
                            onClick = {
                                onSortSelect(SortOption.MAX_LEVEL_DESC)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upgrade Progress (Lowest first)", color = TextPrimary) },
                            onClick = {
                                onSortSelect(SortOption.PROGRESS_ASC)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upgrade Progress (Highest first)", color = TextPrimary) },
                            onClick = {
                                onSortSelect(SortOption.PROGRESS_DESC)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            if (types.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val isAll = uiState.selectedTypeId == null
                        CategoryFilterChip(
                            name = "All (${uiState.buildings.size})",
                            isSelected = isAll,
                            onClick = { onCategorySelect(null) }
                        )
                    }

                    items(types, key = { it.id }) { type ->
                        val isSelected = uiState.selectedTypeId == type.id
                        val count = uiState.buildings.count { it.typeId == type.id }
                        CategoryFilterChip(
                            name = "${type.name} ($count)",
                            isSelected = isSelected,
                            onClick = { onCategorySelect(type.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Count summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${buildings.size} of ${uiState.buildings.size} buildings",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = if (uiState.village == "HOME") "Home Village" else "Builder Base",
                    color = BluePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Building List / Empty State
            if (buildings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BluePrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No buildings found",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotBlank() || uiState.selectedTypeId != null)
                                    "No buildings match the current search or category filter."
                                else
                                    "Your building list is currently empty. Tap the button below to add your first building.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onAddBuildingClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Building", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(buildings, key = { it.id }) { building ->
                        val category = typeMap[building.typeId]
                        BuildingMasterItemCard(
                            building = building,
                            category = category,
                            onEdit = { onEditBuildingClick(building) },
                            onDelete = { onDeleteBuildingClick(building) },
                            onManageCounts = { onManageCountsClick(building) },
                            onScheduleUpgrade = { onScheduleUpgradeClick(building) },
                            onIncrement = { onIncrementLevel(building) },
                            onDecrement = { onDecrementLevel(building) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BluePrimaryContainer else LightSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) BluePrimary else LightBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("filter_chip_$name")
    ) {
        Text(
            text = name,
            color = if (isSelected) BluePrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BuildingMasterItemCard(
    building: BuildingEntity,
    category: BuildingTypeEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageCounts: () -> Unit,
    onScheduleUpgrade: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val distributions = remember(building) { building.parseLevelDistributions() }
    val isMaxed = building.currentLevel >= building.maxLevel && building.maxLevel > 0
    val progress = if (building.maxLevel > 0) {
        (building.currentLevel.toFloat() / building.maxLevel.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val (categoryColor, containerColor) = when (category?.iconKey?.uppercase()) {
        "DEFENCE" -> BluePrimary to BluePrimaryContainer
        "RESOURCE" -> EmeraldSuccess to EmeraldContainer
        "ARMY" -> AmberPending to AmberContainer
        "TRAPS" -> RedDanger to RedDangerContainer
        else -> SlateSecondary to SlateSecondaryContainer
    }

    val categoryIcon = when (category?.iconKey?.uppercase()) {
        "DEFENCE" -> Icons.Default.Shield
        "RESOURCE" -> Icons.Default.Storage
        "ARMY" -> Icons.Default.LocalFireDepartment
        "TRAPS" -> Icons.Default.Dangerous
        else -> Icons.Default.Build
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("building_card_${building.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Name + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = building.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Total count badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BluePrimaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Total: ${building.totalCount}",
                                    color = BluePrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isMaxed) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MAXED",
                                        color = EmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${category?.name ?: "Building"} • Max Lv ${building.maxLevel}",
                            color = categoryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Edit and Delete Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_building_${building.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_building_${building.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = RedDanger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (building.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = building.notes,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Breakdown Chips (e.g., 3x Lv 10, 2x Lv 11)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instances:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(distributions) { item ->
                        val isLevelMaxed = item.level >= building.maxLevel && building.maxLevel > 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLevelMaxed) EmeraldContainer else SlateSecondaryContainer)
                                .border(
                                    1.dp,
                                    if (isLevelMaxed) EmeraldSuccess.copy(alpha = 0.3f) else LightBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${item.count}x Lv ${item.level}",
                                color = if (isLevelMaxed) EmeraldSuccess else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Progress and Quick Steppers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level ${building.currentLevel} of ${building.maxLevel}",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = if (isMaxed) EmeraldSuccess else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isMaxed) EmeraldSuccess else categoryColor,
                        trackColor = SlateSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Quick - / + Stepper for base Current Level
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSecondaryContainer)
                        .border(1.dp, LightBorder, RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = building.currentLevel > 1,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("quick_dec_${building.id}")
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrement",
                            tint = if (building.currentLevel > 1) TextPrimary else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${building.currentLevel}",
                        color = BluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        enabled = building.currentLevel < building.maxLevel,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("quick_inc_${building.id}")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increment",
                            tint = if (building.currentLevel < building.maxLevel) TextPrimary else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: "Manage Counts" & "Upgrade"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onManageCounts,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("manage_counts_${building.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manage Counts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onScheduleUpgrade,
                    enabled = !isMaxed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = SlateSecondaryContainer,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("schedule_upgrade_${building.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMaxed) "Maxed Out" else "Upgrade",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
