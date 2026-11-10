package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClashRepository
import com.example.data.entity.ActiveUpgradeEntity
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.data.entity.LevelCount
import com.example.data.entity.SyncRecordEntity
import com.example.data.entity.TownHallEntity
import com.example.network.BuildingDiff
import com.example.network.GroundingSource
import com.example.network.GroundingSyncResult
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    NAME,
    CURRENT_LEVEL_DESC,
    MAX_LEVEL_DESC,
    PROGRESS_ASC,
    PROGRESS_DESC
}

sealed class SyncUiState {
    object Idle : SyncUiState()
    data class Searching(val townHallLevel: Int, val village: String) : SyncUiState()
    data class DiffPreview(
        val targetTownHall: TownHallEntity,
        val diffs: List<BuildingDiff>,
        val sources: List<GroundingSource>,
        val searchQueries: List<String>,
        val summary: String
    ) : SyncUiState()
    data class Success(val message: String) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

data class ClashUiState(
    val village: String = "HOME",
    val activeTownHall: TownHallEntity? = null,
    val townHalls: List<TownHallEntity> = emptyList(),
    val buildingTypes: List<BuildingTypeEntity> = emptyList(),
    val buildings: List<BuildingEntity> = emptyList(),
    val filteredBuildings: List<BuildingEntity> = emptyList(),
    val activeUpgrades: List<ActiveUpgradeEntity> = emptyList(),
    val selectedTypeId: Long? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NAME,
    val latestSyncRecord: SyncRecordEntity? = null,
    val syncUiState: SyncUiState = SyncUiState.Idle,
    val userNotice: String? = null
)

class ClashViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ClashRepository

    private val _village = MutableStateFlow("HOME")
    private val _selectedTypeId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.NAME)
    private val _syncUiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    private val _userNotice = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ClashRepository(database)
    }

    // Reactive streams driven by current village
    private val buildingTypesFlow = _village.flatMapLatest { v -> repository.getBuildingTypes(v) }
    private val townHallsFlow = _village.flatMapLatest { v -> repository.getTownHalls(v) }
    private val activeTownHallFlow = _village.flatMapLatest { v -> repository.getActiveTownHall(v) }
    private val buildingsFlow = _village.flatMapLatest { v -> repository.getBuildings(v) }
    private val latestSyncFlow = _village.flatMapLatest { v -> repository.getLatestSync(v) }
    private val activeUpgradesFlow = repository.getActiveUpgrades()

    val uiState: StateFlow<ClashUiState> = combine(
        combine(
            _village,
            activeTownHallFlow,
            townHallsFlow,
            buildingTypesFlow,
            buildingsFlow
        ) { village, activeTh, thList, types, buildings ->
            Tuple5(village, activeTh, thList, types, buildings)
        },
        combine(
            activeUpgradesFlow,
            _selectedTypeId,
            _searchQuery,
            _sortOption,
            latestSyncFlow
        ) { upgrades, typeFilter, query, sort, latestSync ->
            Tuple5(upgrades, typeFilter, query, sort, latestSync)
        },
        _syncUiState,
        _userNotice
    ) { part1, part2, syncState, notice ->
        val village = part1.a
        val activeTh = part1.b
        val thList = part1.c
        val types = part1.d
        val allBuildings = part1.e

        val upgrades = part2.a
        val typeFilter = part2.b
        val query = part2.c
        val sort = part2.d
        val latestSync = part2.e

        val filtered = allBuildings
            .filter { b ->
                (typeFilter == null || b.typeId == typeFilter) &&
                (query.isBlank() || b.name.contains(query, ignoreCase = true) || b.notes.contains(query, ignoreCase = true))
            }
            .let { list ->
                when (sort) {
                    SortOption.NAME -> list.sortedBy { it.name }
                    SortOption.CURRENT_LEVEL_DESC -> list.sortedByDescending { it.currentLevel }
                    SortOption.MAX_LEVEL_DESC -> list.sortedByDescending { it.maxLevel }
                    SortOption.PROGRESS_ASC -> list.sortedBy { it.currentLevel.toFloat() / maxOf(1, it.maxLevel) }
                    SortOption.PROGRESS_DESC -> list.sortedByDescending { it.currentLevel.toFloat() / maxOf(1, it.maxLevel) }
                }
            }

        ClashUiState(
            village = village,
            activeTownHall = activeTh,
            townHalls = thList,
            buildingTypes = types,
            buildings = allBuildings,
            filteredBuildings = filtered,
            activeUpgrades = upgrades,
            selectedTypeId = typeFilter,
            searchQuery = query,
            sortOption = sort,
            latestSyncRecord = latestSync,
            syncUiState = syncState,
            userNotice = notice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClashUiState()
    )

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    fun switchVillage(village: String) {
        _village.value = village
        _selectedTypeId.value = null
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(typeId: Long?) {
        _selectedTypeId.value = typeId
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun clearNotice() {
        _userNotice.value = null
    }

    fun dismissSyncState() {
        _syncUiState.value = SyncUiState.Idle
    }

    // Town Hall operations
    fun setActiveTownHall(townHall: TownHallEntity) {
        viewModelScope.launch {
            repository.setActiveTownHall(townHall.id, _village.value)
            _userNotice.value = "Active level set to ${townHall.name}"
        }
    }

    fun triggerSearchGroundingSync(targetTownHall: TownHallEntity? = null) {
        val currentTh = targetTownHall ?: uiState.value.activeTownHall
        if (currentTh == null) {
            _userNotice.value = "Please select or create an active Town Hall first."
            return
        }

        viewModelScope.launch {
            _syncUiState.value = SyncUiState.Searching(currentTh.level, _village.value)
            val result = repository.querySearchGrounding(currentTh.level, _village.value)
            when (result) {
                is GroundingSyncResult.Success -> {
                    _syncUiState.value = SyncUiState.DiffPreview(
                        targetTownHall = currentTh,
                        diffs = result.diffs,
                        sources = result.sources,
                        searchQueries = result.searchQueries,
                        summary = result.rawSummary
                    )
                }
                is GroundingSyncResult.Error -> {
                    _syncUiState.value = SyncUiState.Error(result.message)
                }
            }
        }
    }

    fun applyConfirmedDiffs(
        targetTownHall: TownHallEntity,
        diffs: List<BuildingDiff>,
        sources: List<GroundingSource>
    ) {
        viewModelScope.launch {
            val updatedCount = repository.applyConfirmedSyncDiffs(
                diffs = diffs,
                townHallLevel = targetTownHall.level,
                village = _village.value,
                sources = sources
            )
            repository.setActiveTownHall(targetTownHall.id, _village.value)
            _syncUiState.value = SyncUiState.Success(
                "Synchronized maximum levels for ${targetTownHall.name}! ($updatedCount building caps updated)"
            )
        }
    }

    // Building CRUD & Distributions
    fun addBuilding(
        typeId: Long,
        name: String,
        currentLevel: Int = 1,
        maxLevel: Int = 1,
        notes: String = "",
        totalCount: Int = 1
    ) {
        viewModelScope.launch {
            val initialDistribution = listOf(LevelCount(level = maxOf(1, currentLevel), count = maxOf(1, totalCount)))
            val entity = BuildingEntity(
                typeId = typeId,
                name = name.trim(),
                totalCount = maxOf(1, totalCount),
                currentLevel = maxOf(1, currentLevel),
                maxLevel = maxOf(1, maxLevel),
                levelDistributionJson = BuildingEntity.serializeLevelDistributions(initialDistribution),
                village = _village.value,
                notes = notes.trim()
            )
            repository.insertBuilding(entity)
            _userNotice.value = "Added building '${name.trim()}'"
        }
    }

    fun updateBuilding(building: BuildingEntity) {
        viewModelScope.launch {
            repository.updateBuilding(building)
            _userNotice.value = "Updated building '${building.name}'"
        }
    }

    fun deleteBuilding(building: BuildingEntity) {
        viewModelScope.launch {
            repository.deleteBuilding(building)
            _userNotice.value = "Deleted building '${building.name}'"
        }
    }

    fun updateBuildingDistribution(
        buildingId: Long,
        totalCount: Int,
        maxLevel: Int,
        distributions: List<LevelCount>
    ) {
        viewModelScope.launch {
            repository.updateBuildingDistribution(buildingId, totalCount, maxLevel, distributions)
            _userNotice.value = "Updated level distribution for building"
        }
    }

    // Upgrade Scheduling & Timer Notifications
    fun scheduleUpgrade(
        building: BuildingEntity,
        fromLevel: Int,
        toLevel: Int,
        durationMillis: Long
    ) {
        viewModelScope.launch {
            val finishTime = System.currentTimeMillis() + durationMillis
            val upgrade = ActiveUpgradeEntity(
                buildingId = building.id,
                buildingName = building.name,
                fromLevel = fromLevel,
                toLevel = toLevel,
                startTimestamp = System.currentTimeMillis(),
                finishTimestamp = finishTime
            )
            val newId = repository.insertActiveUpgrade(upgrade)
            val scheduledUpgrade = upgrade.copy(id = newId)

            // Schedule system alarm and notification
            NotificationHelper.scheduleUpgradeAlarm(getApplication(), scheduledUpgrade)

            _userNotice.value = "Started upgrade for ${building.name} (Lv $fromLevel ➔ Lv $toLevel)"
        }
    }

    fun finishActiveUpgrade(upgrade: ActiveUpgradeEntity) {
        viewModelScope.launch {
            NotificationHelper.cancelUpgradeAlarm(getApplication(), upgrade.id)
            repository.finishActiveUpgrade(upgrade)
            _userNotice.value = "Completed upgrade for ${upgrade.buildingName} to Level ${upgrade.toLevel}!"
        }
    }

    fun cancelActiveUpgrade(upgrade: ActiveUpgradeEntity) {
        viewModelScope.launch {
            NotificationHelper.cancelUpgradeAlarm(getApplication(), upgrade.id)
            repository.deleteActiveUpgrade(upgrade.id)
            _userNotice.value = "Cancelled upgrade for ${upgrade.buildingName}"
        }
    }

    fun incrementBuildingLevel(building: BuildingEntity) {
        if (building.currentLevel < building.maxLevel) {
            viewModelScope.launch {
                repository.updateBuilding(building.copy(currentLevel = building.currentLevel + 1))
            }
        }
    }

    fun decrementBuildingLevel(building: BuildingEntity) {
        if (building.currentLevel > 1) {
            viewModelScope.launch {
                repository.updateBuilding(building.copy(currentLevel = building.currentLevel - 1))
            }
        }
    }

    // Town Hall CRUD
    fun addTownHall(level: Int, name: String, makeActive: Boolean) {
        viewModelScope.launch {
            val title = if (name.isBlank()) {
                if (_village.value == "HOME") "Town Hall $level" else "Builder Hall $level"
            } else name.trim()

            val entity = TownHallEntity(
                level = level,
                name = title,
                village = _village.value,
                isActive = false
            )
            val newId = repository.insertTownHall(entity)
            if (makeActive) {
                val created = entity.copy(id = newId)
                setActiveTownHall(created)
            } else {
                _userNotice.value = "Added $title"
            }
        }
    }

    fun updateTownHall(townHall: TownHallEntity) {
        viewModelScope.launch {
            repository.updateTownHall(townHall)
            _userNotice.value = "Updated ${townHall.name}"
        }
    }

    fun deleteTownHall(townHall: TownHallEntity) {
        viewModelScope.launch {
            repository.deleteTownHall(townHall)
            _userNotice.value = "Deleted ${townHall.name}"
        }
    }

    // Building Type CRUD
    fun addBuildingType(name: String, iconKey: String, priority: Int = 1) {
        viewModelScope.launch {
            val entity = BuildingTypeEntity(
                name = name.trim(),
                village = _village.value,
                iconKey = iconKey,
                priority = maxOf(1, priority)
            )
            repository.insertBuildingType(entity)
            _userNotice.value = "Added category '${name.trim()}' (Priority $priority)"
        }
    }

    fun updateBuildingType(type: BuildingTypeEntity) {
        viewModelScope.launch {
            repository.updateBuildingType(type)
            _userNotice.value = "Updated category '${type.name}'"
        }
    }

    fun deleteBuildingType(type: BuildingTypeEntity) {
        viewModelScope.launch {
            repository.deleteBuildingType(type)
            _userNotice.value = "Deleted category '${type.name}'"
        }
    }
}
