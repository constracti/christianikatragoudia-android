package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import gr.christianikatragoudia.app.data.SongTitle

object SearchResult {

    @Composable
    fun ResultList(
        resultList: List<SongTitle>,
        navigateToSong: (Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(resultList) {
                ResultItem(
                    songTitle = it,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            onClickLabel = it.title,
                        ) {
                            navigateToSong(it.id)
                        },
                )
            }
        }
    }

    @Composable
    private fun ResultItem(
        songTitle: SongTitle,
        modifier: Modifier,
    ) {
        ListItem(
            modifier = modifier,
            headlineContent = {
                Text(text = songTitle.title)
            },
            supportingContent = {
                if (songTitle.excerpt != songTitle.title)
                    Text(text = songTitle.excerpt)
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .5F),
            ),
        )
    }
}
