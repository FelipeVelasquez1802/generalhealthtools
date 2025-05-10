package org.infinite.solution.generalhealthtools.presentation.dashboard.viewmodel

import generalhealthtools.composeapp.generated.resources.Res.string
import generalhealthtools.composeapp.generated.resources.error_general_message
import kotlinx.coroutines.flow.update
import org.infinite.solution.generalhealthtools.presentation.common.uimodel.ErrorUiModel
import org.infinite.solution.generalhealthtools.presentation.common.util.readResource
import org.infinite.solution.generalhealthtools.presentation.common.viewmodel.CommonViewModel
import org.infinite.solution.generalhealthtools.presentation.dashboard.uistate.DashboardContract
import org.infinite.solution.generalhealthtools.presentation.dashboard.view.component.FileHandle
import org.infinite.solution.generalhealthtools.presentation.dashboard.view.component.FileManager

internal class DashboardViewModel :
    CommonViewModel<DashboardContract.State, DashboardContract.Event>(
        initialState = DashboardContract.State()
    ) {

    override suspend fun onStart() {
        initialize()
    }

    override suspend fun onLaunchError() {
        mutableUiState.update { state ->
            state.copy(
                isLoading = false,
                error = ErrorUiModel(
                    messageRes = string.error_general_message,
                    isShowing = true
                )
            )
        }
    }

    fun setSelectedPageChange(selectedPage: Int) {
        mutableUiState.update { state -> state.copy(selectedPage = selectedPage) }
    }

    fun setFileHandleChange(fileHandle: FileHandle) {
        mutableUiState.update { state -> state.copy(fileHandle = fileHandle) }
    }

    fun setNewFileExtensionChange(newFileExtension: String) {
        mutableUiState.update { state ->
            state.copy(newFileExtension = newFileExtension)
        }
    }

    private suspend fun initialize() {
        mutableUiState.update { state ->
            state.copy(
                totalPages = MenuDashboard.entries.size,
                selectedPage = 0,
                menu = MenuDashboard.entries.toList(),
                pubicKey = readResource("files/rsa_public_mlls.pem")
            )
        }
        mutableUiState.update { state ->
            state.copy(fileManager = FileManager.getInstance(state.pubicKey).generatePublicKey())
        }
    }
}

internal enum class MenuDashboard(val title: String) {
    ENCRYPT(title = "Encrypt"),
    CHANGE_EXTENSION(title = "Change extension"),
    ;

    companion object {
        fun fromOrdinal(ordinal: Int): MenuDashboard = entries.firstOrNull {
            it.ordinal == ordinal
        } ?: ENCRYPT

    }
}