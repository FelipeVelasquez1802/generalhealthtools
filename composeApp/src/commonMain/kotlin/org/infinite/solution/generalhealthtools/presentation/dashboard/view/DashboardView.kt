package org.infinite.solution.generalhealthtools.presentation.dashboard.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.infinite.solution.generalhealthtools.presentation.common.extension.Background
import org.infinite.solution.generalhealthtools.presentation.common.extension.SecondaryBackground
import org.infinite.solution.generalhealthtools.presentation.common.util.multiplierX8
import org.infinite.solution.generalhealthtools.presentation.common.view.component.button.ButtonComponent
import org.infinite.solution.generalhealthtools.presentation.common.view.component.container.ScrollableScaffoldComponent
import org.infinite.solution.generalhealthtools.presentation.common.view.component.pager.VerticalPagerComponent
import org.infinite.solution.generalhealthtools.presentation.dashboard.uistate.DashboardContract
import org.infinite.solution.generalhealthtools.presentation.dashboard.view.component.FileManager
import org.infinite.solution.generalhealthtools.presentation.dashboard.viewmodel.DashboardViewModel
import org.infinite.solution.generalhealthtools.presentation.dashboard.viewmodel.MenuDashboard

@Composable
internal fun DashboardView() {
    val viewModel = DashboardViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScrollableScaffoldComponent {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.2f)
                    .background(Color.SecondaryBackground)
                    .padding(horizontal = multiplierX8),
            ) {
                Text(
                    text = "Welcome to General Health Tools!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = multiplierX8)
                )
                Spacer(modifier = Modifier.height(multiplierX8))
                state.menu.forEach { menu ->
                    ButtonComponent(
                        text = menu.title,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        viewModel.setSelectedPageChange(menu.ordinal)
                    }
                }
            }
            VerticalPagerComponent(
                totalPages = state.totalPages,
                selectedPage = state.selectedPage,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.8f)
                    .background(Color.Background),
            ) { page ->
                Pages(
                    menu = MenuDashboard.fromOrdinal(page),
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun Pages(
    menu: MenuDashboard,
    state: DashboardContract.State,
    viewModel: DashboardViewModel
) {
    when (menu) {
        MenuDashboard.ENCRYPT -> EncryptPage(state, viewModel)
        MenuDashboard.CHANGE_EXTENSION -> ChangeExtensionPage(state, viewModel)
    }
}

@Composable
private fun EncryptPage(state: DashboardContract.State, viewModel: DashboardViewModel) {
    val scope = rememberCoroutineScope()
//    val fileSelector by remember { mutableStateOf(FileManager.getInstance(state.pubicKey)) }
    Text(text = "Encrypt page")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = multiplierX8),
        horizontalArrangement = Arrangement.spacedBy(multiplierX8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            ButtonComponent(text = "Choose file...") {
                scope.launch {
                    val selectedFile = state.fileManager?.selectFile()
                    selectedFile?.let {
                        viewModel.setFileHandleChange(it)
                    }
                }
            }
        }
        item {
            Text(
                text = state.fileHandle?.name
                    .orEmpty()
                    .ifBlank { "No file selected" }
            )
        }
    }
    LazyRow {
        item {
            ButtonComponent(
                text = "Encrypt",
                enabled = state.fileHandle?.name
                    .orEmpty()
                    .isNotEmpty()
            ) {
                scope.launch {
                    val encryptedData = state.fileManager
                        ?.encryptFile(state.fileHandle?.bytes ?: ByteArray(0))
                    encryptedData?.let {
                        state.fileManager.saveEncryptedFile(
                            it, state.fileHandle?.name
                                .orEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeExtensionPage(
    state: DashboardContract.State,
    viewModel: DashboardViewModel
) {
    val scope = rememberCoroutineScope()
    val fileManager = remember { FileManager.getInstance(state.pubicKey) }
    Text(text = "Change extension")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = multiplierX8),
        horizontalArrangement = Arrangement.spacedBy(multiplierX8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            ButtonComponent(text = "Choose file...") {
                scope.launch {
                    val selectedFile = fileManager.selectFile()
                    selectedFile?.let {
                        viewModel.setFileHandleChange(it)
                    }
                }
            }
        }
        item {
            Text(
                text = state.fileHandle?.name
                    .orEmpty()
                    .ifBlank { "No file selected" }
            )
        }
    }
    Spacer(modifier = Modifier.height(multiplierX8))
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = multiplierX8),
        horizontalArrangement = Arrangement.spacedBy(multiplierX8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            OutlinedTextField(
                value = state.newFileExtension,
                onValueChange = viewModel::setNewFileExtensionChange,
                label = { Text("New file extension") }

            )
        }
        item {
            ButtonComponent(
                text = "Download",
                enabled = state.fileHandle?.name
                    .orEmpty()
                    .isNotEmpty()
                    .and(state.newFileExtension.isNotEmpty())
            ) {
                scope.launch {
                    fileManager.downloadFile(
                        state.fileHandle?.bytes
                            ?: ByteArray(0),
                        state.fileHandle?.name
                            .orEmpty(),
                        state.newFileExtension
                    )
                }
            }
        }
    }
}