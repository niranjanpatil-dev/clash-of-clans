package com.example

import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PriorityRecommendationTest {

    @Test
    fun testRecommendationSortingByCategoryPriority() {
        val typeDefence = BuildingTypeEntity(id = 1L, name = "Defence", priority = 2)
        val typeArmy = BuildingTypeEntity(id = 2L, name = "Army", priority = 1)
        val typeResource = BuildingTypeEntity(id = 3L, name = "Resource", priority = 3)

        val typeMap = mapOf(
            1L to typeDefence,
            2L to typeArmy,
            3L to typeResource
        )

        val buildingDefence = BuildingEntity(
            id = 101L,
            typeId = 1L,
            name = "Cannon",
            currentLevel = 5,
            maxLevel = 10
        )

        val buildingArmy = BuildingEntity(
            id = 102L,
            typeId = 2L,
            name = "Barracks",
            currentLevel = 6,
            maxLevel = 10
        )

        val buildingResource = BuildingEntity(
            id = 103L,
            typeId = 3L,
            name = "Gold Mine",
            currentLevel = 3,
            maxLevel = 10
        )

        val list = listOf(buildingResource, buildingDefence, buildingArmy)

        // Sorting by category priority where less number = more priority (1 before 2 before 3)
        val sorted = list.sortedWith(
            compareBy<BuildingEntity> { building ->
                typeMap[building.typeId]?.priority ?: 999
            }.thenBy { it.name }
        )

        // Priority 1 (Army) must be first, Priority 2 (Defence) second, Priority 3 (Resource) third
        assertEquals("Barracks", sorted[0].name)
        assertEquals(1, typeMap[sorted[0].typeId]?.priority)

        assertEquals("Cannon", sorted[1].name)
        assertEquals(2, typeMap[sorted[1].typeId]?.priority)

        assertEquals("Gold Mine", sorted[2].name)
        assertEquals(3, typeMap[sorted[2].typeId]?.priority)
    }
}
