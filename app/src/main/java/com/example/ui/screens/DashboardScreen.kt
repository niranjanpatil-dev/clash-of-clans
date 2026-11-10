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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.ui.ClashUiState
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
    onAddBuildingClick: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {}
) {
    val buildings = uiState.buildings
    val types = uiState.buildingTypes
    val activeTh = uiState.activeTownHall

    val totalBuildings = buildings.size
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
