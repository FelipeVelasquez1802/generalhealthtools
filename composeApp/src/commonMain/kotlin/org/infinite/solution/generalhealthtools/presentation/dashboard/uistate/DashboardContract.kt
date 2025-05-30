package org.infinite.solution.generalhealthtools.presentation.dashboard.uistate

import org.infinite.solution.generalhealthtools.presentation.common.extension.Empty
import org.infinite.solution.generalhealthtools.presentation.common.uimodel.ErrorUiModel
import org.infinite.solution.generalhealthtools.presentation.dashboard.view.component.FileHandle
import org.infinite.solution.generalhealthtools.presentation.dashboard.view.component.FileManager
import org.infinite.solution.generalhealthtools.presentation.dashboard.viewmodel.MenuDashboard

internal sealed interface DashboardContract {
    data class State(
        val isLoading: Boolean = false,
        val error: ErrorUiModel? = null,

        val totalPages: Int = 0,
        val selectedPage: Int = 1,
        val menu: List<MenuDashboard> = emptyList(),

        val fileHandle: FileHandle? = null,
        val newFileExtension: String = String.Empty,
        val pubicKey: ByteArray = ByteArray(0),
        val fileManager: FileManager? = null,
    )

    sealed interface Event {
        data object GoToEncryptFile : Event
    }
}