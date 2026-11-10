package com.example

import com.example.data.entity.BuildingEntity
import com.example.data.entity.LevelCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BuildingEntityDistributionTest {

    @Test
    fun testParseAndSerializeDistributions() {
        val original = listOf(
            LevelCount(level = 10, count = 2),
            LevelCount(level = 11, count = 3)
        )
        val json = BuildingEntity.serializeLevelDistributions(original)
        assertTrue(json.contains("\"level\":10"))
        assertTrue(json.contains("\"count\":2"))
        assertTrue(json.contains("\"level\":11"))
        assertTrue(json.contains("\"count\":3"))

        val building = BuildingEntity(
            typeId = 1L,
            name = "Cannon",
            totalCount = 5,
            currentLevel = 11,
            maxLevel = 15,
            levelDistributionJson = json
        )

        val parsed = building.parseLevelDistributions()
        assertEquals(2, parsed.size)
        assertEquals(10, parsed[0].level)
        assertEquals(2, parsed[0].count)
        assertEquals(11, parsed[1].level)
        assertEquals(3, parsed[1].count)
    }

    @Test
    fun testFallbackWhenJsonEmpty() {
        val building = BuildingEntity(
            typeId = 1L,
            name = "Archer Tower",
            totalCount = 4,
            currentLevel = 9,
            maxLevel = 14,
            levelDistributionJson = "[]"
        )

        val parsed = building.parseLevelDistributions()
        assertEquals(1, parsed.size)
        assertEquals(9, parsed[0].level)
        assertEquals(4, parsed[0].count)
    }
}
