package com.gusanitolabs.robia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gusanitolabs.robia.R
import com.gusanitolabs.robia.core.model.GarmentTag
import com.gusanitolabs.robia.core.model.TagCategory
import java.util.Locale
import java.util.UUID

private data class TagEditorState(
    val categoryId: String,
    val existingTag: GarmentTag? = null,
)

@Composable
fun ManageTagsScreen(
    innerPadding: PaddingValues,
    categories: List<TagCategory>,
    tags: List<GarmentTag>,
    onSaveTag: (GarmentTag) -> Unit,
    onDeleteCustomTag: (GarmentTag) -> Unit,
) {
    var editorState by remember { mutableStateOf<TagEditorState?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.manage_tags_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.manage_tags_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        categories.forEach { category ->
            item(key = category.id) {
                val categoryTags = tags.filter { tag -> tag.categoryId == category.id }
                TagCategoryCard(
                    category = category,
                    tags = categoryTags,
                    onAddTag = { editorState = TagEditorState(categoryId = category.id) },
                    onEditTag = { tag -> editorState = TagEditorState(categoryId = category.id, existingTag = tag) },
                    onDeleteTag = onDeleteCustomTag,
                )
            }
        }
    }

    editorState?.let { state ->
        TagEditorDialog(
            state = state,
            categoryTags = tags.filter { tag -> tag.categoryId == state.categoryId },
            onDismiss = { editorState = null },
            onSave = { tag ->
                onSaveTag(tag)
                editorState = null
            },
        )
    }
}

@Composable
private fun TagCategoryCard(
    category: TagCategory,
    tags: List<GarmentTag>,
    onAddTag: () -> Unit,
    onEditTag: (GarmentTag) -> Unit,
    onDeleteTag: (GarmentTag) -> Unit,
) {
    val addDescription = stringResource(R.string.content_add_tag)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalOffer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.localizedName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.tag_count, tags.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    modifier = Modifier.semantics { contentDescription = addDescription },
                    onClick = onAddTag,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }

            if (tags.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_tags_in_category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        TagListRow(
                            tag = tag,
                            onEdit = { onEditTag(tag) },
                            onDelete = { onDeleteTag(tag) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagListRow(
    tag: GarmentTag,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val editDescription = stringResource(R.string.content_edit_tag)
    val deleteDescription = stringResource(R.string.content_delete_tag)

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tag.dotColor()),
            )
            Text(
                text = tag.localizedName(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (tag.isSystem) {
                Text(
                    text = stringResource(R.string.system_tag_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                IconButton(
                    modifier = Modifier
                        .size(36.dp)
                        .semantics { contentDescription = editDescription },
                    onClick = onEdit,
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    modifier = Modifier
                        .size(36.dp)
                        .semantics { contentDescription = deleteDescription },
                    onClick = onDelete,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TagEditorDialog(
    state: TagEditorState,
    categoryTags: List<GarmentTag>,
    onDismiss: () -> Unit,
    onSave: (GarmentTag) -> Unit,
) {
    var name by remember(state) { mutableStateOf(state.existingTag?.name.orEmpty()) }
    val trimmedName = name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (state.existingTag == null) R.string.add_tag_title else R.string.edit_tag_title,
                ),
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.tag_name_label)) },
            )
        },
        confirmButton = {
            Button(
                enabled = trimmedName.isNotEmpty(),
                onClick = {
                    val existing = state.existingTag
                    onSave(
                        GarmentTag(
                            id = existing?.id ?: customTagId(trimmedName),
                            categoryId = state.categoryId,
                            name = trimmedName,
                            sortOrder = existing?.sortOrder ?: categoryTags.nextSortOrder(),
                            isSystem = false,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun TagCategory.localizedName(): String = when (id) {
    "style" -> stringResource(R.string.category_style)
    "season" -> stringResource(R.string.category_season)
    "occasion" -> stringResource(R.string.category_occasion)
    "care" -> stringResource(R.string.category_care)
    "custom" -> stringResource(R.string.category_custom)
    else -> name
}

@Composable
private fun GarmentTag.localizedName(): String = when (id) {
    "style-casual" -> stringResource(R.string.tag_casual)
    "style-formal" -> stringResource(R.string.tag_formal)
    "season-spring" -> stringResource(R.string.tag_spring)
    "season-summer" -> stringResource(R.string.tag_summer)
    "season-autumn" -> stringResource(R.string.tag_autumn)
    "season-winter" -> stringResource(R.string.tag_winter)
    "occasion-work" -> stringResource(R.string.tag_work)
    "occasion-travel" -> stringResource(R.string.tag_travel)
    "care-dry-clean" -> stringResource(R.string.tag_dry_clean)
    else -> name
}

@Composable
private fun GarmentTag.dotColor(): Color = when (categoryId) {
    "season" -> MaterialTheme.colorScheme.secondaryContainer
    "occasion" -> MaterialTheme.colorScheme.outline
    "care" -> MaterialTheme.colorScheme.tertiaryContainer
    "custom" -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}

private fun List<GarmentTag>.nextSortOrder(): Int =
    (maxOfOrNull(GarmentTag::sortOrder) ?: 0) + 10

private fun customTagId(name: String): String {
    val slug = name
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifEmpty { "tag" }
    return "custom-$slug-${UUID.randomUUID()}"
}
