package com.gusanitolabs.robia.core.model

object DefaultTags {
    val categories = listOf(
        TagCategory(id = "style", name = "Style", sortOrder = 10, isSystem = true),
        TagCategory(id = "season", name = "Season", sortOrder = 20, isSystem = true),
        TagCategory(id = "occasion", name = "Occasion", sortOrder = 30, isSystem = true),
        TagCategory(id = "care", name = "Care", sortOrder = 40, isSystem = true),
        TagCategory(id = "custom", name = "Custom", sortOrder = 90, isSystem = true),
    )

    val tags = listOf(
        GarmentTag(id = "style-casual", categoryId = "style", name = "Casual", sortOrder = 10, isSystem = true),
        GarmentTag(id = "style-formal", categoryId = "style", name = "Formal", sortOrder = 20, isSystem = true),
        GarmentTag(id = "season-spring", categoryId = "season", name = "Spring", sortOrder = 10, isSystem = true),
        GarmentTag(id = "season-summer", categoryId = "season", name = "Summer", sortOrder = 20, isSystem = true),
        GarmentTag(id = "season-autumn", categoryId = "season", name = "Autumn", sortOrder = 30, isSystem = true),
        GarmentTag(id = "season-winter", categoryId = "season", name = "Winter", sortOrder = 40, isSystem = true),
        GarmentTag(id = "occasion-work", categoryId = "occasion", name = "Work", sortOrder = 10, isSystem = true),
        GarmentTag(id = "occasion-travel", categoryId = "occasion", name = "Travel", sortOrder = 20, isSystem = true),
        GarmentTag(id = "care-dry-clean", categoryId = "care", name = "Dry clean", sortOrder = 10, isSystem = true),
    )
}
