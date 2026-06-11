package com.gusanitolabs.robia.core.color

import com.gusanitolabs.robia.core.model.DisplayColorLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorLabelResolverTest {
    @Test
    fun resolvesHexColorsToFixedLabels() {
        assertEquals(DisplayColorLabel.Red, ColorLabelResolver.fromRawValue("#cc2020"))
        assertEquals(DisplayColorLabel.Blue, ColorLabelResolver.fromRawValue("#3366cc"))
        assertEquals(DisplayColorLabel.Gray, ColorLabelResolver.fromRawValue("#777777"))
    }

    @Test
    fun preservesKnownTextLabels() {
        assertEquals(DisplayColorLabel.Brown, ColorLabelResolver.fromRawValue("brown"))
        assertEquals(DisplayColorLabel.Multicolor, ColorLabelResolver.fromRawValue("multi-color"))
    }

    @Test
    fun returnsUnknownForBlankOrUnparsedValues() {
        assertEquals(DisplayColorLabel.Unknown, ColorLabelResolver.fromRawValue(""))
        assertEquals(DisplayColorLabel.Unknown, ColorLabelResolver.fromRawValue("not-a-color"))
    }
}
