package org.infinite.solution.generalhealthtools.presentation.common.view.component.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import org.infinite.solution.generalhealthtools.presentation.common.extension.Background
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun VerticalPagerComponent(
    modifier: Modifier = Modifier,
    totalPages: Int,
    selectedPage: Int = 0,
    content: @Composable (PagerScope.(Int) -> Unit)
) {
    val state = rememberPagerState(
        pageCount = { totalPages },
    )
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(selectedPage) {
        coroutineScope.launch {
            state.animateScrollToPage(selectedPage)
        }
    }
    VerticalPager(
        modifier = modifier,
        state = state,
        pageContent = content
    )
}

@Preview
@Composable
private fun VerticalPagerComponentPreview() {
    MaterialTheme {
        VerticalPagerComponent(
            totalPages = 10,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Background),
        ) { page ->
            Text("Page: $page")
        }
    }
}