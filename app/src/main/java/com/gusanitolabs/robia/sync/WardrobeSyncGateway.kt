package com.gusanitolabs.robia.sync

import com.gusanitolabs.robia.core.model.ClothingItem
import com.gusanitolabs.robia.core.model.GarmentTag

/** Future seam for Drive or another backend; MVP deliberately stays local-only. */
interface WardrobeSyncGateway {
    suspend fun enqueueItemChanged(item: ClothingItem)
    suspend fun enqueueTagChanged(tag: GarmentTag)
}

object NoOpWardrobeSyncGateway : WardrobeSyncGateway {
    override suspend fun enqueueItemChanged(item: ClothingItem) = Unit
    override suspend fun enqueueTagChanged(tag: GarmentTag) = Unit
}
