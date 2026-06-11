package com.gusanitolabs.robia.core.model

import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultTagsTest {
    @Test
    fun defaultTagsReferenceExistingCategories() {
        val categoryIds = DefaultTags.categories.map(TagCategory::id).toSet()
        assertTrue(DefaultTags.tags.all { tag -> tag.categoryId in categoryIds })
    }

    @Test
    fun customCategoryExistsForUserManagedTags() {
        assertTrue(DefaultTags.categories.any { category -> category.id == "custom" })
    }
}
