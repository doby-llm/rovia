package com.gusanitolabs.robia.data.local

import androidx.room.TypeConverter
import com.gusanitolabs.robia.core.model.DisplayColorLabel

class RobiaConverters {
    @TypeConverter
    fun colorLabelToString(value: DisplayColorLabel?): String? = value?.name

    @TypeConverter
    fun stringToColorLabel(value: String?): DisplayColorLabel? =
        value?.let { DisplayColorLabel.valueOf(it) }
}
