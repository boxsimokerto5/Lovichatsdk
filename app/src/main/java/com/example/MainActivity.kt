package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Chat
import com.example.data.repository.LovyChatRepository
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.GroupListScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NearbyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.LovyPrimary
import com.example.ui.theme.MyApplicationTheme

enum class LovyTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CHATS("Obrolan", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "tab_chats"),
    NEARBY("Teman Sekitar", Icons.Filled.NearMe, Icons.Outlined.NearMe, "tab_nearby"),
    GROUPS("Komunitas", Icons.Filled.Groups, Icons.Outlined.Groups, "tab_groups"),
    PROFILE("Saya", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

class MainActivity : ComponentActivity() {
    private val repository = LovyChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository.supabaseManager.init(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LovyChatApp(repository = repository)
            }
        }
    }
}

@Composable
fun LovyChatApp(repository: LovyChatRepository) {
    val currentUser by repository.currentUser.collectAsState()

    if (currentUser == null) {
        LoginScreen(repository = repository)
    } else {
        LovyChatMainContent(repository = repository)
    }
}

@Composable
fun LovyChatMainContent(repository: LovyChatRepository) {
    var selectedTab by remember { mutableStateOf(LovyTab.CHATS) }
    var activeChat by remember { mutableStateOf<Chat?>(null) }
    val chats by repository.chats.collectAsState()
    val totalUnread = chats.sumOf { it.unreadCount }

    if (activeChat != null) {
        ChatDetailScreen(
            chat = activeChat!!,
            repository = repository,
            onBackClick = { activeChat = null }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    LovyTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.testTag(tab.testTag),
                            icon = {
                                if (tab == LovyTab.CHATS && totalUnread > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = LovyPrimary) {
                                                Text(text = "$totalUnread", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LovyPrimary,
                                selectedTextColor = LovyPrimary,
                                indicatorColor = LovyPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { tab ->
                when (tab) {
                    LovyTab.CHATS -> {
                        ChatListScreen(
                            repository = repository,
                            onChatClick = { chat -> activeChat = chat },
                            onNavigateToNearby = { selectedTab = LovyTab.NEARBY }
                        )
                    }
                    LovyTab.NEARBY -> {
                        NearbyScreen(
                            repository = repository,
                            onOpenChat = { chat -> activeChat = chat }
                        )
                    }
                    LovyTab.GROUPS -> {
                        GroupListScreen(
                            repository = repository,
                            onOpenGroupChat = { chat -> activeChat = chat }
                        )
                    }
                    LovyTab.PROFILE -> {
                        ProfileScreen(
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}

