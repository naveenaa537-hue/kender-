package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.KindredViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)

@Composable
fun MainNavigationContainer(
    viewModel: KindredViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()

    if (isLocked) {
        PinLockScreen(
            onUnlock = { pin -> viewModel.unlockApp(pin) }
        )
        return
    }

    // Handle back button: if on sub-tab, return to Chat (tab 0)
    BackHandler(enabled = selectedTab != 0) {
        viewModel.selectedTab.value = 0
    }

    val navItems = listOf(
        NavItem("Chat", Icons.AutoMirrored.Filled.Chat, 0),
        NavItem("Memories", Icons.Default.PhotoAlbum, 1),
        NavItem("Bucket", Icons.Default.Checklist, 2),
        NavItem("Notes", Icons.Default.Note, 3),
        NavItem("Duo Hub", Icons.Default.Favorite, 4),
        NavItem("Settings", Icons.Default.Settings, 5)
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth >= 720.dp

        if (isWideScreen) {
            // Tablet / Desktop Layout: NavigationRail + Dual-Pane Content
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = selectedTab == item.index,
                            onClick = { viewModel.selectedTab.value = item.index },
                            icon = {
                                if (item.index == 4 && (activeUser?.streakDays ?: 0) > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text("${activeUser?.streakDays}") }
                                        }
                                    ) {
                                        Icon(item.icon, contentDescription = item.title)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.title)
                                }
                            },
                            label = { Text(item.title) },
                            modifier = Modifier.testTag("nav_rail_${item.title.lowercase()}")
                        )
                    }
                }

                VerticalDivider()

                // Dual Pane or Wide Single Pane
                if (screenWidth >= 1000.dp && selectedTab != 0) {
                    // Split mode: Left side Chat, Right side Selected Feature!
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(0.52f)) {
                            ChatScreen(
                                viewModel = viewModel,
                                onOpenDuoHub = { viewModel.selectedTab.value = 4 }
                            )
                        }
                        VerticalDivider()
                        Box(modifier = Modifier.weight(0.48f)) {
                            when (selectedTab) {
                                1 -> MemoriesScreen(viewModel = viewModel)
                                2 -> BucketListScreen(viewModel = viewModel)
                                3 -> NotesScreen(viewModel = viewModel)
                                4 -> FriendshipHubScreen(viewModel = viewModel)
                                5 -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTab) {
                            0 -> ChatScreen(
                                viewModel = viewModel,
                                onOpenDuoHub = { viewModel.selectedTab.value = 4 }
                            )
                            1 -> MemoriesScreen(viewModel = viewModel)
                            2 -> BucketListScreen(viewModel = viewModel)
                            3 -> NotesScreen(viewModel = viewModel)
                            4 -> FriendshipHubScreen(viewModel = viewModel)
                            5 -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            // Mobile Phone Layout: Screen with Bottom Navigation Bar
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                selected = selectedTab == item.index,
                                onClick = { viewModel.selectedTab.value = item.index },
                                icon = {
                                    if (item.index == 4 && (activeUser?.streakDays ?: 0) > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge { Text("${activeUser?.streakDays}") }
                                            }
                                        ) {
                                            Icon(item.icon, contentDescription = item.title)
                                        }
                                    } else {
                                        Icon(item.icon, contentDescription = item.title)
                                    }
                                },
                                label = { Text(item.title) },
                                modifier = Modifier.testTag("nav_item_${item.title.lowercase()}")
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> ChatScreen(
                            viewModel = viewModel,
                            onOpenDuoHub = { viewModel.selectedTab.value = 4 }
                        )
                        1 -> MemoriesScreen(viewModel = viewModel)
                        2 -> BucketListScreen(viewModel = viewModel)
                        3 -> NotesScreen(viewModel = viewModel)
                        4 -> FriendshipHubScreen(viewModel = viewModel)
                        5 -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
