package com.example

import com.example.network.BuildingDiff
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClashLogicTest {
    @Test
    fun testBuildingDiffCalculation() {
        val changedDiff = BuildingDiff(
            buildingId = 1L,
            buildingName = "Archer Tower",
            categoryName = "Defence",
            currentLevel = 20,
            oldMaxLevel = 21,
            newMaxLevel = 22
        )

        assertTrue(changedDiff.hasChanged)
        assertEquals(1, changedDiff.levelDelta)
        assertEquals(20, changedDiff.currentLevel)

        val unchangedDiff = BuildingDiff(
            buildingId = 2L,
            buildingName = "Cannon",
            categoryName = "Defence",
            currentLevel = 21,
            oldMaxLevel = 21,
            newMaxLevel = 21
        )

        assertFalse(unchangedDiff.hasChanged)
        assertEquals(0, unchangedDiff.levelDelta)
    }
}
