package com.gusanitolabs.robia.ui

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gusanitolabs.robia.R
import com.gusanitolabs.robia.core.designsystem.RobiaTheme
import com.gusanitolabs.robia.core.model.ClothingItem
import com.gusanitolabs.robia.core.model.DisplayColorLabel
import com.gusanitolabs.robia.core.model.GarmentTag
import com.gusanitolabs.robia.core.model.LanguagePreference
import com.gusanitolabs.robia.core.model.RobiaSettings
import com.gusanitolabs.robia.core.model.TagCategory
import com.gusanitolabs.robia.data.SettingsRepository
import com.gusanitolabs.robia.data.TagRepository
import com.gusanitolabs.robia.data.WardrobeRepository
import kotlinx.coroutines.launch

private sealed interface RobiaRoute {
    @get:StringRes
    val titleRes: Int

    data object Browse : RobiaRoute {
        override val titleRes = R.string.browse
    }

    data object ManageTags : RobiaRoute {
        override val titleRes = R.string.manage
    }

    data object AddEditClothing : RobiaRoute {
        override val titleRes = R.string.add_clothing
    }

    data object ItemDetail : RobiaRoute {
        override val titleRes = R.string.item_detail
    }

    data object LanguageSettings : RobiaRoute {
        override val titleRes = R.string.language
    }
}

private data class BottomNavDestination(
    val route: RobiaRoute,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

private data class UiWardrobeItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val notes: String,
    val photoUri: String?,
    val tags: List<UiTag>,
    val primaryColor: DisplayColorLabel,
    val primaryRawValue: String?,
    val secondaryColor: DisplayColorLabel,
    val secondaryRawValue: String?,
    val isFavorite: Boolean,
    val isSample: Boolean = false,
)

private data class UiTag(
    val id: String,
    val label: String,
)

private val bottomDestinations = listOf(
    BottomNavDestination(RobiaRoute.Browse, R.string.browse, Icons.Rounded.GridView),
    BottomNavDestination(RobiaRoute.AddEditClothing, R.string.add_clothing, Icons.Rounded.Add),
    BottomNavDestination(RobiaRoute.ManageTags, R.string.manage, Icons.Rounded.Style),
)

@Composable
fun RobiaApp(
    settingsRepository: SettingsRepository,
    wardrobeRepository: WardrobeRepository,
    tagRepository: TagRepository,
) {
    val settings by settingsRepository.settings.collectAsState(initial = RobiaSettings())
    val clothingItems by wardrobeRepository.observeActiveItems().collectAsState(initial = emptyList())
    val tagCategories by tagRepository.observeCategories().collectAsState(initial = emptyList())
    val availableTags by tagRepository.observeTags().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(tagRepository) {
        tagRepository.seedDefaultsIfNeeded()
    }

    RobiaTheme {
        RobiaShell(
            settings = settings,
            clothingItems = clothingItems,
            tagCategories = tagCategories,
            availableTags = availableTags,
            onLanguageSelected = { language ->
                scope.launch { settingsRepository.setLanguagePreference(language) }
            },
            onSaveItem = { item ->
                scope.launch { wardrobeRepository.upsertItem(item) }
            },
            onSaveTag = { tag ->
                scope.launch { tagRepository.upsertTag(tag) }
            },
            onDeleteCustomTag = { tag ->
                scope.launch { tagRepository.deleteCustomTag(tag.id) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RobiaShell(
    settings: RobiaSettings,
    clothingItems: List<ClothingItem>,
    tagCategories: List<TagCategory>,
    availableTags: List<GarmentTag>,
    onLanguageSelected: (LanguagePreference) -> Unit,
    onSaveItem: (ClothingItem) -> Unit,
    onSaveTag: (GarmentTag) -> Unit,
    onDeleteCustomTag: (GarmentTag) -> Unit,
) {
    var currentRoute: RobiaRoute by remember { mutableStateOf(RobiaRoute.Browse) }
    var settingsExpanded by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    val items = clothingItems.toUiWardrobeItems()
    val sampleItems = rememberSampleWardrobeItems()
    val displayedItems = items.ifEmpty { sampleItems }
    val selectedItem = displayedItems.firstOrNull { it.id == selectedItemId } ?: displayedItems.firstOrNull()
    val selectedDomainItem = clothingItems.firstOrNull { it.id == selectedItemId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    if (currentRoute == RobiaRoute.ItemDetail) {
                        IconButton(onClick = { currentRoute = RobiaRoute.Browse }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.content_go_back),
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = if (currentRoute == RobiaRoute.ItemDetail) {
                            stringResource(R.string.item_detail)
                        } else {
                            stringResource(R.string.app_name)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    if (currentRoute == RobiaRoute.ItemDetail && selectedItem != null) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = if (selectedItem.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = stringResource(R.string.content_favorite),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    } else {
                        Box {
                            val settingsDescription = stringResource(R.string.content_settings_menu)
                            IconButton(
                                modifier = Modifier.semantics { contentDescription = settingsDescription },
                                onClick = { settingsExpanded = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                )
                            }
                            SettingsMenu(
                                expanded = settingsExpanded,
                                currentLanguage = settings.languagePreference,
                                onLanguageSelected = { language ->
                                    onLanguageSelected(language)
                                    settingsExpanded = false
                                },
                                onDismiss = { settingsExpanded = false },
                                onLanguageClick = {
                                    currentRoute = RobiaRoute.LanguageSettings
                                    settingsExpanded = false
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        bottomBar = {
            if (currentRoute != RobiaRoute.ItemDetail) {
                RobiaBottomBar(
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        if (route == RobiaRoute.AddEditClothing) selectedItemId = null
                        currentRoute = route
                    },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        RobiaNavHost(
            currentRoute = currentRoute,
            innerPadding = innerPadding,
            items = displayedItems,
            isShowingSamples = items.isEmpty(),
            selectedItem = selectedItem,
            selectedDomainItem = selectedDomainItem,
            tagCategories = tagCategories,
            availableTags = availableTags,
            onRouteSelected = { route ->
                if (route == RobiaRoute.AddEditClothing && currentRoute != RobiaRoute.ItemDetail) selectedItemId = null
                currentRoute = route
            },
            onItemSelected = { item ->
                selectedItemId = item.id
                currentRoute = RobiaRoute.ItemDetail
            },
            onSaveItem = { item ->
                onSaveItem(item)
                selectedItemId = item.id
                currentRoute = RobiaRoute.ItemDetail
            },
            onSaveTag = onSaveTag,
            onDeleteCustomTag = onDeleteCustomTag,
        )
    }
}

@Composable
private fun SettingsMenu(
    expanded: Boolean,
    currentLanguage: LanguagePreference,
    onLanguageSelected: (LanguagePreference) -> Unit,
    onDismiss: () -> Unit,
    onLanguageClick: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.language)) },
            leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null) },
            onClick = onLanguageClick,
        )
        LanguagePreference.entries.forEach { language ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(language.labelRes),
                        fontWeight = if (language == currentLanguage) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                onClick = { onLanguageSelected(language) },
            )
        }
        Divider()
        DropdownMenuItem(
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.data_sync))
                    Text(
                        text = stringResource(R.string.data_sync_coming_soon),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            },
            leadingIcon = { Icon(Icons.Rounded.CloudOff, contentDescription = null) },
            onClick = { },
            enabled = false,
        )
    }
}

private val LanguagePreference.labelRes: Int
    get() = when (this) {
        LanguagePreference.System -> R.string.language_system
        LanguagePreference.English -> R.string.language_english
        LanguagePreference.Spanish -> R.string.language_spanish
        LanguagePreference.German -> R.string.language_german
    }

@Composable
private fun RobiaBottomBar(
    currentRoute: RobiaRoute,
    onRouteSelected: (RobiaRoute) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onRouteSelected(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}

@Composable
private fun RobiaNavHost(
    currentRoute: RobiaRoute,
    innerPadding: PaddingValues,
    items: List<UiWardrobeItem>,
    isShowingSamples: Boolean,
    selectedItem: UiWardrobeItem?,
    selectedDomainItem: ClothingItem?,
    tagCategories: List<TagCategory>,
    availableTags: List<GarmentTag>,
    onRouteSelected: (RobiaRoute) -> Unit,
    onItemSelected: (UiWardrobeItem) -> Unit,
    onSaveItem: (ClothingItem) -> Unit,
    onSaveTag: (GarmentTag) -> Unit,
    onDeleteCustomTag: (GarmentTag) -> Unit,
) {
    when (currentRoute) {
        RobiaRoute.Browse -> BrowseWardrobeScreen(
            innerPadding = innerPadding,
            items = items,
            isShowingSamples = isShowingSamples,
            onItemSelected = onItemSelected,
            onAddClick = { onRouteSelected(RobiaRoute.AddEditClothing) },
        )
        RobiaRoute.ManageTags -> ManageTagsScreen(
            innerPadding = innerPadding,
            categories = tagCategories,
            tags = availableTags,
            onSaveTag = onSaveTag,
            onDeleteCustomTag = onDeleteCustomTag,
        )
        RobiaRoute.AddEditClothing -> AddEditClothingScreen(
            innerPadding = innerPadding,
            availableTags = availableTags,
            existingItem = selectedDomainItem,
            onCancel = { onRouteSelected(RobiaRoute.Browse) },
            onSave = onSaveItem,
        )
        RobiaRoute.ItemDetail -> selectedItem?.let { item ->
            ItemDetailScreen(
                innerPadding = innerPadding,
                item = item,
                onEditClick = { onRouteSelected(RobiaRoute.AddEditClothing) },
            )
        } ?: EmptyStateCard(onAddClick = { onRouteSelected(RobiaRoute.AddEditClothing) })
        RobiaRoute.LanguageSettings -> LanguageSettingsScreen(innerPadding)
    }
}

@Composable
private fun BrowseWardrobeScreen(
    innerPadding: PaddingValues,
    items: List<UiWardrobeItem>,
    isShowingSamples: Boolean,
    onItemSelected: (UiWardrobeItem) -> Unit,
    onAddClick: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { FilterBar() }
        if (isShowingSamples) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyStateCard(onAddClick = onAddClick)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SampleWardrobeNotice(onAddClick = onAddClick)
            }
        }
        items(items, key = { it.id }) { item ->
            GarmentGridCard(
                item = item,
                onClick = { onItemSelected(item) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp, vertical = 12.dp),
            ) {
                Text(stringResource(R.string.load_more_items))
            }
        }
        if (!isShowingSamples && items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyStateCard(onAddClick = onAddClick) }
        }
    }
}

@Composable
private fun FilterBar() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected = false,
            onClick = { },
            label = { Text(stringResource(R.string.filters)) },
            leadingIcon = { Icon(Icons.Rounded.Tune, contentDescription = null) },
        )
        AssistChip(
            onClick = { },
            label = { Text(stringResource(R.string.all_filters)) },
        )
        Spacer(
            modifier = Modifier
                .height(24.dp)
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun SampleWardrobeNotice(onAddClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sample_wardrobe_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.sample_wardrobe_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = onAddClick,
                label = { Text(stringResource(R.string.add_clothing)) },
                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun GarmentGridCard(
    item: UiWardrobeItem,
    onClick: () -> Unit,
) {
    val itemDescription = stringResource(R.string.content_open_item_detail)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = itemDescription }
            .clickable(onClick = onClick),
    ) {
        Box {
            GarmentPhotoPlaceholder(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(R.string.content_favorite),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(6.dp),
                )
            }
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.tags.take(2).forEach { tag -> TonalTag(text = tag.label) }
            }
        }
    }
}

@Composable
private fun GarmentPhotoPlaceholder(
    item: UiWardrobeItem,
    modifier: Modifier = Modifier,
) {
    val swatchColor = item.primaryColor.swatchColor()
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        swatchColor.copy(alpha = 0.26f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        val photoUri = item.photoUri?.takeIf { it.isNotBlank() }
        if (photoUri != null) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { imageView -> imageView.setImageURI(Uri.parse(photoUri)) },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Style,
                    contentDescription = null,
                    tint = swatchColor,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Composable
private fun TonalTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun ItemDetailScreen(
    innerPadding: PaddingValues,
    item: UiWardrobeItem,
    onEditClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            GarmentPhotoPlaceholder(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.notes.ifBlank { item.subtitle },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { ColorMetricsCard(item) }
        item { TagBentoGrid(item.tags) }
        item {
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.edit))
            }
        }
    }
}

@Composable
private fun ColorMetricsCard(item: UiWardrobeItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.extracted_colors),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ColorSwatch(
                    role = stringResource(R.string.primary_color),
                    color = item.primaryColor,
                    rawValue = item.primaryRawValue,
                )
                ColorSwatch(
                    role = stringResource(R.string.secondary_color),
                    color = item.secondaryColor,
                    rawValue = item.secondaryRawValue,
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    role: String,
    color: DisplayColorLabel,
    rawValue: String?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.swatchColor())
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        )
        Text(
            text = role.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = color.localizedLabel(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        rawValue?.takeIf { it.isNotBlank() }?.let { value ->
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TagBentoGrid(tags: List<UiTag>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.tags),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (tags.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_tags),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                tags.chunked(2).forEach { rowTags ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowTags.forEach { tag ->
                            Box(modifier = Modifier.weight(1f)) {
                                TonalTag(text = tag.label)
                            }
                        }
                        if (rowTags.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Style,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.empty_wardrobe_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.empty_wardrobe_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            AssistChip(
                onClick = onAddClick,
                label = { Text(stringResource(R.string.add_clothing)) },
                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    innerPadding: PaddingValues,
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    icon: ImageVector,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSettingsScreen(innerPadding: PaddingValues) {
    val languages = stringArrayResource(R.array.language_choices)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.language_settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.language_settings_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                languages.forEachIndexed { index, language ->
                    ListItem(
                        headlineContent = { Text(language) },
                        leadingContent = { Icon(Icons.Rounded.Language, contentDescription = null) },
                    )
                    if (index != languages.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun List<ClothingItem>.toUiWardrobeItems(): List<UiWardrobeItem> = map { item ->
    UiWardrobeItem(
        id = item.id,
        name = item.name,
        subtitle = item.notes.ifBlank { stringResource(R.string.wardrobe_item_saved_locally) },
        notes = item.notes,
        photoUri = item.photoUri,
        tags = item.tags.map { tag -> UiTag(tag.id, tag.localizedLabel()) },
        primaryColor = item.colorMetrics.primaryDisplayLabel ?: DisplayColorLabel.Unknown,
        primaryRawValue = item.colorMetrics.primaryRawValue,
        secondaryColor = item.colorMetrics.secondaryDisplayLabel ?: DisplayColorLabel.Unknown,
        secondaryRawValue = item.colorMetrics.secondaryRawValue,
        isFavorite = item.isFavorite,
    )
}

@Composable
private fun rememberSampleWardrobeItems(): List<UiWardrobeItem> = listOf(
    UiWardrobeItem(
        id = "sample-linen-shirt",
        name = stringResource(R.string.sample_linen_shirt_name),
        subtitle = stringResource(R.string.sample_linen_shirt_brand),
        notes = stringResource(R.string.sample_linen_shirt_notes),
        photoUri = null,
        tags = listOf(
            UiTag("season-summer", stringResource(R.string.tag_summer)),
            UiTag("style-casual", stringResource(R.string.tag_casual)),
        ),
        primaryColor = DisplayColorLabel.White,
        primaryRawValue = "#F8F9FA",
        secondaryColor = DisplayColorLabel.Brown,
        secondaryRawValue = "#C8A57D",
        isFavorite = false,
        isSample = true,
    ),
    UiWardrobeItem(
        id = "sample-jeans",
        name = stringResource(R.string.sample_jeans_name),
        subtitle = stringResource(R.string.sample_jeans_brand),
        notes = stringResource(R.string.sample_jeans_notes),
        photoUri = null,
        tags = listOf(
            UiTag("season-all", stringResource(R.string.tag_all_season)),
            UiTag("style-everyday", stringResource(R.string.tag_everyday)),
        ),
        primaryColor = DisplayColorLabel.Blue,
        primaryRawValue = "#315F8E",
        secondaryColor = DisplayColorLabel.Gray,
        secondaryRawValue = "#AEB4BA",
        isFavorite = true,
        isSample = true,
    ),
    UiWardrobeItem(
        id = "sample-field-jacket",
        name = stringResource(R.string.sample_field_jacket_name),
        subtitle = stringResource(R.string.sample_field_jacket_brand),
        notes = stringResource(R.string.sample_field_jacket_notes),
        photoUri = null,
        tags = listOf(
            UiTag("season-autumn", stringResource(R.string.tag_autumn)),
            UiTag("style-outerwear", stringResource(R.string.tag_outerwear)),
        ),
        primaryColor = DisplayColorLabel.Green,
        primaryRawValue = "#5F6F48",
        secondaryColor = DisplayColorLabel.Brown,
        secondaryRawValue = "#8B6848",
        isFavorite = false,
        isSample = true,
    ),
    UiWardrobeItem(
        id = "sample-sneakers",
        name = stringResource(R.string.sample_sneakers_name),
        subtitle = stringResource(R.string.sample_sneakers_brand),
        notes = stringResource(R.string.sample_sneakers_notes),
        photoUri = null,
        tags = listOf(
            UiTag("season-all", stringResource(R.string.tag_all_season)),
            UiTag("style-shoes", stringResource(R.string.tag_shoes)),
        ),
        primaryColor = DisplayColorLabel.White,
        primaryRawValue = "#F8F9FA",
        secondaryColor = DisplayColorLabel.Gray,
        secondaryRawValue = "#DADADA",
        isFavorite = false,
        isSample = true,
    ),
    UiWardrobeItem(
        id = "sample-tee",
        name = stringResource(R.string.sample_tee_name),
        subtitle = stringResource(R.string.sample_tee_brand),
        notes = stringResource(R.string.sample_tee_notes),
        photoUri = null,
        tags = listOf(
            UiTag("style-casual", stringResource(R.string.tag_casual)),
            UiTag("category-tops", stringResource(R.string.tag_tops)),
        ),
        primaryColor = DisplayColorLabel.Black,
        primaryRawValue = "#1F1F1F",
        secondaryColor = DisplayColorLabel.Gray,
        secondaryRawValue = "#8E8E8E",
        isFavorite = true,
        isSample = true,
    ),
)

@Composable
private fun GarmentTag.localizedLabel(): String = when (id) {
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
private fun DisplayColorLabel.localizedLabel(): String = stringResource(
    when (this) {
        DisplayColorLabel.Black -> R.string.color_black
        DisplayColorLabel.Blue -> R.string.color_blue
        DisplayColorLabel.Brown -> R.string.color_brown
        DisplayColorLabel.Gray -> R.string.color_gray
        DisplayColorLabel.Green -> R.string.color_green
        DisplayColorLabel.Orange -> R.string.color_orange
        DisplayColorLabel.Pink -> R.string.color_pink
        DisplayColorLabel.Purple -> R.string.color_purple
        DisplayColorLabel.Red -> R.string.color_red
        DisplayColorLabel.White -> R.string.color_white
        DisplayColorLabel.Yellow -> R.string.color_yellow
        DisplayColorLabel.Multicolor -> R.string.color_multicolor
        DisplayColorLabel.Unknown -> R.string.color_unknown
    },
)

private fun DisplayColorLabel.swatchColor(): Color = when (this) {
    DisplayColorLabel.Black -> Color(0xFF1F1F1F)
    DisplayColorLabel.Blue -> Color(0xFF315F8E)
    DisplayColorLabel.Brown -> Color(0xFF8B6848)
    DisplayColorLabel.Gray -> Color(0xFF8E8E8E)
    DisplayColorLabel.Green -> Color(0xFF5F6F48)
    DisplayColorLabel.Orange -> Color(0xFFC56F33)
    DisplayColorLabel.Pink -> Color(0xFFD4879A)
    DisplayColorLabel.Purple -> Color(0xFF765A91)
    DisplayColorLabel.Red -> Color(0xFF9E3D35)
    DisplayColorLabel.White -> Color(0xFFF8F9FA)
    DisplayColorLabel.Yellow -> Color(0xFFD6B84C)
    DisplayColorLabel.Multicolor -> Color(0xFFA56639)
    DisplayColorLabel.Unknown -> Color(0xFFDADADA)
}

@Preview(showBackground = true)
@Composable
private fun RobiaAppPreview() {
    RobiaTheme {
        RobiaShell(
            settings = RobiaSettings(),
            clothingItems = emptyList(),
            tagCategories = emptyList(),
            availableTags = emptyList(),
            onLanguageSelected = {},
            onSaveItem = {},
            onSaveTag = {},
            onDeleteCustomTag = {},
        )
    }
}
