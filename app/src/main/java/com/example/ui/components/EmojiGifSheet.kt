package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class CuratedGif(val title: String, val url: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiGifSheet(
    onEmojiSelected: (String) -> Unit,
    onGifSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val emojis = remember {
        listOf(
            "❤️", "💖", "✨", "🔥", "😂", "🥰", "🥳", "🥺",
            "🙌", "🎉", "💯", "😍", "🤝", "🌟", "🌸", "🍕",
            "🌮", "🧋", "☕", "🍻", "🚀", "🐼", "🦊", "🐶",
            "🐱", "🌈", "🍦", "🏕️", "🎧", "🎬", "💌", "😴"
        )
    }

    val curatedGifs = remember {
        listOf(
            CuratedGif("Best Friends Hug", "https://media.giphy.com/media/l41YkxvU8c77T8dUc/giphy.gif"),
            CuratedGif("Happy Dance", "https://media.giphy.com/media/blSTtZehjAZ8I/giphy.gif"),
            CuratedGif("High Five", "https://media.giphy.com/media/3oEjHV0z8S7WM4MwnK/giphy.gif"),
            CuratedGif("Laughing Out Loud", "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif"),
            CuratedGif("Heart Sparkles", "https://media.giphy.com/media/26FLdm964upIslUZ2/giphy.gif"),
            CuratedGif("Excited Jump", "https://media.giphy.com/media/5GoVLqeAOo6PK/giphy.gif"),
            CuratedGif("Mind Blown", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif"),
            CuratedGif("Sending Love", "https://media.giphy.com/media/M90mJvfWfd5mbUuULX/giphy.gif"),
            CuratedGif("Popcorn Chill", "https://media.giphy.com/media/gl0mkIZOW6Nwc/giphy.gif"),
            CuratedGif("Celebration Party", "https://media.giphy.com/media/artj92V8o75VPL7AeQ/giphy.gif")
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Emojis 😊") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("GIFs 🎬") }
                )
            }

            if (selectedTab == 0) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis) { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEmojiSelected(emoji) }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(curatedGifs) { gif ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onGifSelected(gif.url) }
                                .height(100.dp)
                        ) {
                            AsyncImage(
                                model = gif.url,
                                contentDescription = gif.title,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
