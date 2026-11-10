package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.entity.ActiveUpgradeEntity
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.ui.ClashUiState
import kotlinx.coroutines.delay
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BlueOnPrimaryContainer
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.BluePrimaryLight
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
fun DashboardScreen(
    uiState: ClashUiState,
    onSwitchVillage: (String) -> Unit,
    onTriggerSync: () -> Unit,
    onNavigateToTownHalls: () -> Unit,
    onNavigateToBuildings: (typeId: Long?) -> Unit,
    onScheduleUpgrade: (BuildingEntity) -> Unit = {},
    onFinishUpgrade: (ActiveUpgradeEntity) -> Unit = {},
    onCancelUpgrade: (ActiveUpgradeEntity) -> Unit = {},
    onAddBuildingClick: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {}
) {
    val buildings = uiState.buildings
    val types = uiState.buildingTypes
    val activeTh = uiState.activeTownHall
    val activeUpgrades = uiState.activeUpgrades

    val typeMap = remember(types) { types.associateBy { it.id } }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    // Recommended Upgrades sorted primarily by Category Priority (1 before 2)
    val recommendedBuildings = remember(buildings, typeMap, activeUpgrades) {
        val activeBuildingIds = activeUpgrades.map { it.buildingId }.toSet()
        buildings
            .filter { building ->
                val isMaxed = building.maxLevel > 0 && building.currentLevel >= building.maxLevel &&
                    building.parseLevelDistributions().all { it.level >= building.maxLevel }
                !isMaxed && building.maxLevel > 0
            }
            .sortedWith(
                compareBy<BuildingEntity> { building ->
                    // 1. Category Priority: Lower number = Higher priority (1 before 2)
                    typeMap[building.typeId]?.priority ?: 999
                }
                .thenBy { building ->
                    // 2. Prioritize non-active upgrades
                    if (building.id in activeBuildingIds) 1 else 0
                }
                .thenBy { building ->
                    // 3. Lowest progress first
                    if (building.maxLevel > 0) building.currentLevel.toFloat() / building.maxLevel.toFloat() else 0f
                }
                .thenBy { building -> building.name }
            )
    }

    val totalBuildings = buildings.size
    val totalInstances = buildings.sumOf { it.totalCount }
    val completedBuildings = buildings.count { it.currentLevel >= it.maxLevel && it.maxLevel > 0 }
    val pendingBuildings = totalBuildings - completedBuildings

    val totalCurrentLevels = buildings.sumOf { it.currentLevel }
    val totalMaxLevels = buildings.sumOf { it.maxLevel }
    val overallProgress = if (totalMaxLevels > 0) {
        (totalCurrentLevels.toFloat() / totalMaxLevels.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedOverallProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "overallProgress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Village Segmented Switcher
        item {
            Spacer(modifier = Modifier.height(6.dp))
            VillageSwitchHeader(
                currentVillage = uiState.village,
                onSwitchVillage = onSwitchVillage
            )
        }

        // Active Town Hall Banner
        item {
            TownHallHeroBanner(
                activeTownHall = activeTh,
                village = uiState.village,
                onChangeTownHall = onNavigateToTownHalls
            )
        }

        // Active Upgrades Ticking Widget (If any upgrades are running)
        if (activeUpgrades.isNotEmpty()) {
            item {
                ActiveUpgradesSection(
                    upgrades = activeUpgrades,
                    currentTime = currentTime,
                    onFinish = onFinishUpgrade,
                    onCancel = onCancelUpgrade
                )
            }
        }

        // Recommended Upgrades Section (Ranked by Category Priority: 1 > 2 > 3)
        if (buildings.isNotEmpty()) {
            item {
                RecommendedUpgradesSection(
                    recommendedBuildings = recommendedBuildings,
                    typeMap = typeMap,
                    activeUpgrades = activeUpgrades,
                    onScheduleUpgrade = onScheduleUpgrade
                )
            }
        }

        // Overall Progress Metric Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERALL UPGRADE COMPLETION",
                                color = BluePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalBuildings > 0) "${(animatedOverallProgress * 100).toInt()}% Complete" else "0% Complete",
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BluePrimaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$totalCurrentLevels / $totalMaxLevels Lvls",
                                color = BlueOnPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { animatedOverallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = BluePrimary,
                        trackColor = SlateSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Key Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(
                            label = "Categories",
                            value = "${types.size}",
                            icon = Icons.Default.Shield,
                            accentColor = BluePrimary,
                            bgColor = BluePrimaryContainer
                        )
                        StatPill(
                            label = "Buildings",
                            value = "$totalBuildings",
                            icon = Icons.Default.Apartment,
                            accentColor = SlateSecondary,
                            bgColor = SlateSecondaryContainer
                        )
                        StatPill(
                            label = "Completed",
                            value = "$completedBuildings",
                            icon = Icons.Default.CheckCircle,
                            accentColor = EmeraldSuccess,
                            bgColor = EmeraldContainer
                        )
                        StatPill(
                            label = "Pending",
                            value = "$pendingBuildings",
                            icon = Icons.Default.HourglassTop,
                            accentColor = AmberPending,
                            bgColor = AmberContainer
                        )
                    }
                }
            }
        }

        // Category Breakdown Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Building Categories",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                if (types.isNotEmpty()) {
                    Text(
                        text = "Tap category to view",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Empty state if no categories exist
        if (types.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(BluePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No categories yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Add building categories (such as Defence, Resource, Army) to organize structures.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(types, key = { it.id }) { type ->
                val typeBuildings = buildings.filter { it.typeId == type.id }
                CategoryProgressCard(
                    type = type,
                    buildings = typeBuildings,
                    onClick = { onNavigateToBuildings(type.id) }
                )
            }
        }

        // Buildings Empty State Card if no buildings yet
        if (buildings.isEmpty() && types.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your village currently has no buildings",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Use the Buildings tab to record your village defenses, storages, and troops.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VillageSwitchHeader(
    currentVillage: String,
    onSwitchVillage: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSecondaryContainer)
            .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        val isHome = currentVillage == "HOME"
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isHome) LightSurface else Color.Transparent)
                .clickable { onSwitchVillage("HOME") }
                .padding(vertical = 9.dp)
                .testTag("switch_home_village"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = if (isHome) BluePrimary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Home Village",
                    color = if (isHome) BluePrimary else TextSecondary,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }

        val isBuilder = currentVillage == "BUILDER"
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isBuilder) LightSurface else Color.Transparent)
                .clickable { onSwitchVillage("BUILDER") }
                .padding(vertical = 9.dp)
                .testTag("switch_builder_base"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = if (isBuilder) BluePrimary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Builder Base",
                    color = if (isBuilder) BluePrimary else TextSecondary,
                    fontWeight = if (isBuilder) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun TownHallHeroBanner(
    activeTownHall: com.example.data.entity.TownHallEntity?,
    village: String,
    onChangeTownHall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BluePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeTownHall != null) "${activeTownHall.level}" else "-",
                        color = BluePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (village == "HOME") "ACTIVE TOWN HALL" else "ACTIVE BUILDER HALL",
                        color = BluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = activeTownHall?.name ?: "None Selected",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = onChangeTownHall,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("change_th_button")
            ) {
                Text(
                    text = if (activeTownHall != null) "Change" else "Select",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
fun CategoryProgressCard(
    type: BuildingTypeEntity,
    buildings: List<BuildingEntity>,
    onClick: () -> Unit
) {
    val total = buildings.size
    val completed = buildings.count { it.currentLevel >= it.maxLevel && it.maxLevel > 0 }
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    val (categoryColor, containerColor) = when (type.iconKey.uppercase()) {
        "DEFENCE" -> BluePrimary to BluePrimaryContainer
        "RESOURCE" -> EmeraldSuccess to EmeraldContainer
        "ARMY" -> AmberPending to AmberContainer
        "TRAPS" -> RedDanger to RedDangerContainer
        else -> SlateSecondary to SlateSecondaryContainer
    }

    val categoryIcon = when (type.iconKey.uppercase()) {
        "DEFENCE" -> Icons.Default.Shield
        "RESOURCE" -> Icons.Default.Storage
        "ARMY" -> Icons.Default.LocalFireDepartment
        "TRAPS" -> Icons.Default.Dangerous
        else -> Icons.Default.Build
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("category_card_${type.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = type.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "$total buildings total",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (completed == total && total > 0) EmeraldContainer else SlateSecondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$completed / $total Maxed",
                        color = if (completed == total && total > 0) EmeraldSuccess else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = categoryColor,
                trackColor = SlateSecondaryContainer
            )
        }
    }
}

@Composable
fun ActiveUpgradesSection(
    upgrades: List<ActiveUpgradeEntity>,
    currentTime: Long,
    onFinish: (ActiveUpgradeEntity) -> Unit,
    onCancel: (ActiveUpgradeEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_upgrades_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BluePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ACTIVE UPGRADES",
                            color = BluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${upgrades.size} in progress",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BluePrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Alerts ON",
                            color = BluePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                upgrades.forEach { upgrade ->
                    ActiveUpgradeItem(
                        upgrade = upgrade,
                        currentTime = currentTime,
                        onFinish = { onFinish(upgrade) },
                        onCancel = { onCancel(upgrade) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveUpgradeItem(
    upgrade: ActiveUpgradeEntity,
    currentTime: Long,
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {
    val totalDuration = (upgrade.finishTimestamp - upgrade.startTimestamp).coerceAtLeast(1L)
    val elapsed = (currentTime - upgrade.startTimestamp).coerceIn(0L, totalDuration)
    val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    val remainingMillis = (upgrade.finishTimestamp - currentTime).coerceAtLeast(0L)
    val isReady = remainingMillis <= 0L

    val days = remainingMillis / (24 * 60 * 60 * 1000)
    val hours = (remainingMillis / (60 * 60 * 1000)) % 24
    val minutes = (remainingMillis / (60 * 1000)) % 60
    val seconds = (remainingMillis / 1000) % 60

    val countdownText = when {
        isReady -> "Ready to Complete!"
        days > 0 -> "${days}d ${hours}h ${minutes}m ${seconds}s"
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        else -> "${minutes}m ${seconds}s"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSecondaryContainer)
            .border(1.dp, if (isReady) EmeraldSuccess.copy(alpha = 0.5f) else LightBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("active_upgrade_item_${upgrade.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = upgrade.buildingName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lv ${upgrade.fromLevel}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Lv ${upgrade.toLevel}",
                        color = BluePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isReady) EmeraldContainer else BluePrimaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = countdownText,
                    color = if (isReady) EmeraldSuccess else BluePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isReady) EmeraldSuccess else BluePrimary,
            trackColor = LightSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("cancel_upgrade_${upgrade.id}")
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) EmeraldSuccess else BluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("finish_upgrade_${upgrade.id}")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isReady) "Finish Now" else "Complete Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecommendedUpgradesSection(
    recommendedBuildings: List<BuildingEntity>,
    typeMap: Map<Long, BuildingTypeEntity>,
    activeUpgrades: List<ActiveUpgradeEntity>,
    onScheduleUpgrade: (BuildingEntity) -> Unit
) {
    val activeBuildingIds = remember(activeUpgrades) { activeUpgrades.map { it.buildingId }.toSet() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recommended_upgrades_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = AmberPending,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RECOMMENDED UPDATES",
                            color = BluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Prioritized by Category (P1 > P2)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (recommendedBuildings.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BluePrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${recommendedBuildings.size} available",
                            color = BluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recommendedBuildings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldContainer)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "All Village Upgrades Complete!",
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Every structure across your prioritized categories has achieved maximum level.",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recommendedBuildings.take(5).forEach { building ->
                        val category = typeMap[building.typeId]
                        val isUpgrading = building.id in activeBuildingIds
                        RecommendedUpgradeCard(
                            building = building,
                            category = category,
                            isUpgrading = isUpgrading,
                            onUpgradeClick = { onScheduleUpgrade(building) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedUpgradeCard(
    building: BuildingEntity,
    category: BuildingTypeEntity?,
    isUpgrading: Boolean,
    onUpgradeClick: () -> Unit
) {
    val priority = category?.priority ?: 999
    val isTopPriority = priority == 1
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSecondaryContainer)
            .border(
                1.dp,
                if (isTopPriority) AmberPending.copy(alpha = 0.5f) else LightBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
            .testTag("recommended_upgrade_${building.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = building.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTopPriority) AmberContainer else BluePrimaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isTopPriority) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberPending,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    text = if (isTopPriority) "Top Priority (P1)" else "Priority $priority",
                                    color = if (isTopPriority) AmberPending else BluePrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Text(
                        text = "${category?.name ?: "Building"} • Level ${building.currentLevel} of ${building.maxLevel}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (isUpgrading) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightSurface)
                        .border(1.dp, LightBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Upgrading",
                            color = BluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("upgrade_recommended_${building.id}")
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upgrade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isTopPriority) AmberPending else categoryColor,
            trackColor = LightSurface
        )
    }
}


