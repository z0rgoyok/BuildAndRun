package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.entities.KanbanColumnType
import app.tich.buildandrun.domain.entities.KanbanTask
import app.tich.buildandrun.domain.entities.Repository
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.failures.DomainFailure
import app.tich.buildandrun.domain.failures.DomainFailureMapper
import app.tich.buildandrun.domain.usecases.AddRepositoryUseCase
import app.tich.buildandrun.domain.usecases.CreateWorktreeUseCase
import app.tich.buildandrun.domain.usecases.UseCaseResult
import app.tich.buildandrun.presentation.errors.DomainFailureToUiErrorMapper
import app.tich.buildandrun.presentation.errors.UiError
import app.tich.buildandrun.presentation.i18n.UiText
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MacOSAppStore {
    private val graph = MacOSAppGraph()
    private val failureToUiErrorMapper = DomainFailureToUiErrorMapper()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableValue(State())

    private var repositories: List<Repository> = emptyList()
    private val worktreesByRepositoryPath = mutableMapOf<String, List<Worktree>>()
    private val tasksByScope = mutableMapOf<String, MutableList<KanbanTask>>()

    private var isLoading: Boolean = false
    private var addRepositoryPathInput: String = ""
    private var createWorktreeState = CreateWorktreeState()
    private var selectedRepositoryId: String? = null
    private var selectedWorktreePath: String? = null
    private var error: ErrorState? = null
    private var success: SuccessState? = null

    val state: Value<State> = mutableState

    init {
        loadInitial()
    }

    fun onAddRepositoryPathChanged(value: String) {
        addRepositoryPathInput = value
        clearMessages()
        publishState()
    }

    fun onAddRepository() {
        if (isLoading) {
            return
        }
        scope.launch {
            isLoading = true
            clearMessages()
            publishState()
            when (
                val result =
                    graph.addRepositoryUseCase.execute(
                        input = AddRepositoryUseCase.Input(path = addRepositoryPathInput),
                    )
            ) {
                is UseCaseResult.Success -> {
                    repositories = result.value.repositories
                    selectedRepositoryId = result.value.addedRepository.id.value
                    selectedWorktreePath = result.value.worktrees.firstOrNull()?.path
                    worktreesByRepositoryPath[result.value.addedRepository.path] = result.value.worktrees
                    addRepositoryPathInput = ""
                    createWorktreeState =
                        createWorktreeState.copy(
                            branchInput = "",
                            worktreePathInput = "",
                            baseBranchInput = "",
                            createBranch = true,
                            isSubmitting = false,
                            createdWorktreePath = null,
                        )
                    success =
                        SuccessState(
                            message =
                                MessageText(
                                    key = "screen.repositories.repository_added",
                                    args = listOf(result.value.addedRepository.name),
                                ),
                        )
                }

                is UseCaseResult.Failure -> {
                    error = mapFailureToErrorState(result.value)
                }
            }
            isLoading = false
            publishState()
        }
    }

    fun onSelectRepository(repositoryId: String) {
        if (selectedRepositoryId == repositoryId) {
            return
        }
        selectedRepositoryId = repositoryId
        selectedWorktreePath = null
        clearMessages()
        publishState()
        val selectedRepository = repositories.firstOrNull { it.id.value == repositoryId } ?: return
        loadWorktreesForRepository(path = selectedRepository.path)
    }

    fun onSelectWorktree(worktreePath: String?) {
        selectedWorktreePath = worktreePath
        clearMessages()
        publishState()
    }

    fun onRefreshSelectedRepository() {
        val repositoryPath = selectedRepository()?.path ?: return
        clearMessages()
        loadWorktreesForRepository(path = repositoryPath)
    }

    fun onCreateWorktreeBranchChanged(value: String) {
        val selectedRepositoryPath = selectedRepository()?.path.orEmpty()
        val normalizedBranch = value.trim()
        val currentWorktreePath = createWorktreeState.worktreePathInput
        val updatedWorktreePath =
            if (currentWorktreePath.isBlank() && normalizedBranch.isNotBlank() && selectedRepositoryPath.isNotBlank()) {
                suggestWorktreePath(repositoryPath = selectedRepositoryPath, branch = normalizedBranch)
            } else {
                currentWorktreePath
            }
        createWorktreeState =
            createWorktreeState.copy(
                branchInput = value,
                worktreePathInput = updatedWorktreePath,
                createdWorktreePath = null,
            )
        clearMessages()
        publishState()
    }

    fun onCreateWorktreePathChanged(value: String) {
        createWorktreeState =
            createWorktreeState.copy(
                worktreePathInput = value,
                createdWorktreePath = null,
            )
        clearMessages()
        publishState()
    }

    fun onCreateWorktreeBaseBranchChanged(value: String) {
        createWorktreeState = createWorktreeState.copy(baseBranchInput = value)
        clearMessages()
        publishState()
    }

    fun onCreateWorktreeCreateBranchChanged(value: Boolean) {
        createWorktreeState = createWorktreeState.copy(createBranch = value)
        clearMessages()
        publishState()
    }

    fun onCreateWorktree() {
        if (createWorktreeState.isSubmitting || isLoading) {
            return
        }
        val repositoryPath = selectedRepository()?.path ?: return
        scope.launch {
            createWorktreeState =
                createWorktreeState.copy(
                    isSubmitting = true,
                    createdWorktreePath = null,
                )
            clearMessages()
            publishState()
            when (
                val result =
                    graph.createWorktreeUseCase.execute(
                        input =
                            CreateWorktreeUseCase.Input(
                                repositoryPath = repositoryPath,
                                branch = createWorktreeState.branchInput,
                                worktreePath = createWorktreeState.worktreePathInput,
                                createBranch = createWorktreeState.createBranch,
                                baseBranch = createWorktreeState.baseBranchInput,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    val worktrees = result.value.allWorktrees
                    worktreesByRepositoryPath[repositoryPath] = worktrees
                    selectedWorktreePath = result.value.createdWorktree.path
                    createWorktreeState =
                        createWorktreeState.copy(
                            isSubmitting = false,
                            createdWorktreePath = result.value.createdWorktree.path,
                        )
                    success =
                        SuccessState(
                            message =
                                MessageText(
                                    key = "screen.create_worktree.success",
                                    args = listOf(result.value.createdWorktree.name),
                                ),
                        )
                }

                is UseCaseResult.Failure -> {
                    createWorktreeState = createWorktreeState.copy(isSubmitting = false)
                    error = mapFailureToErrorState(result.value)
                }
            }
            publishState()
        }
    }

    fun onAddTask(
        title: String,
        description: String?,
        columnId: String,
    ) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) {
            error =
                ErrorState(
                    code = "app.validation.task_title_blank",
                    message = MessageText(key = "app.validation.task_title_blank", args = emptyList()),
                    details = null,
                    isRetryable = false,
                )
            publishState()
            return
        }
        val scopeKey = selectedScopeKey() ?: return
        val existingTasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
        val column = KanbanColumnType.fromString(columnId)
        val maxOrder = existingTasks.filter { it.columnId == column }.maxOfOrNull { it.order } ?: 0
        existingTasks +=
            KanbanTask.create(
                title = normalizedTitle,
                description = description?.trim()?.ifBlank { null },
                columnId = column,
                worktreePath = currentWorktreePath(),
                createdAt = currentEpochMillis(),
                order = maxOrder + 1,
            )
        clearMessages()
        publishState()
    }

    fun onMoveTask(
        taskId: String,
        columnId: String,
    ) {
        val scopeKey = selectedScopeKey() ?: return
        val existingTasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
        val index = existingTasks.indexOfFirst { it.id.value == taskId }
        if (index == -1) {
            return
        }
        val nextColumn = KanbanColumnType.fromString(columnId)
        val maxOrder = existingTasks.filter { it.columnId == nextColumn }.maxOfOrNull { it.order } ?: 0
        existingTasks[index] = existingTasks[index].moveTo(nextColumn).withOrder(maxOrder + 1)
        clearMessages()
        publishState()
    }

    fun onDeleteTask(taskId: String) {
        val scopeKey = selectedScopeKey() ?: return
        val existingTasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
        if (existingTasks.removeAll { it.id.value == taskId }) {
            clearMessages()
            publishState()
        }
    }

    fun onDismissError() {
        error = null
        publishState()
    }

    fun onDismissSuccess() {
        success = null
        publishState()
    }

    fun destroy() {
        scope.cancel()
    }

    private fun loadInitial() {
        scope.launch {
            isLoading = true
            publishState()
            when (val result = graph.loadRepositoriesUseCase.execute()) {
                is UseCaseResult.Success -> {
                    repositories = result.value
                    selectedRepositoryId = repositories.firstOrNull()?.id?.value
                    selectedWorktreePath = null
                }

                is UseCaseResult.Failure -> {
                    error = mapFailureToErrorState(result.value)
                }
            }
            repositories.forEach { repository ->
                loadWorktreesForRepositoryInternal(path = repository.path)
            }
            isLoading = false
            publishState()
        }
    }

    private fun loadWorktreesForRepository(path: String) {
        scope.launch {
            isLoading = true
            publishState()
            loadWorktreesForRepositoryInternal(path = path)
            isLoading = false
            publishState()
        }
    }

    private suspend fun loadWorktreesForRepositoryInternal(path: String) {
        val normalizedPath = normalizePath(path)
        if (normalizedPath.isBlank()) {
            return
        }
        runCatching {
            graph.gitClient.listWorktrees(atRepoPath = normalizedPath)
        }.onSuccess { worktrees ->
            worktreesByRepositoryPath[normalizedPath] = worktrees
            if (selectedRepository()?.path == normalizedPath && selectedWorktreePath != null) {
                val stillExists = worktrees.any { it.path == selectedWorktreePath }
                if (!stillExists) {
                    selectedWorktreePath = null
                }
            }
        }.onFailure { throwable ->
            val domainFailure = DomainFailureMapper.fromThrowable(throwable)
            error = mapFailureToErrorState(domainFailure)
        }
    }

    private fun publishState() {
        val selectedRepository =
            repositories.firstOrNull { it.id.value == selectedRepositoryId }
                ?: repositories.firstOrNull()
                    .also { selectedRepositoryId = it?.id?.value }
        val selectedRepositoryPath = selectedRepository?.path.orEmpty()
        val availableWorktrees = worktreesByRepositoryPath[selectedRepositoryPath].orEmpty()
        if (selectedWorktreePath != null && availableWorktrees.none { it.path == selectedWorktreePath }) {
            selectedWorktreePath = null
        }
        createWorktreeState =
            createWorktreeState.copy(
                repositoryPath = selectedRepositoryPath,
            )

        mutableState.value =
            State(
                isLoading = isLoading,
                repositories = buildRepositoryItems(),
                selectedRepositoryId = selectedRepositoryId,
                selectedWorktreePath = selectedWorktreePath,
                addRepositoryPathInput = addRepositoryPathInput,
                createWorktree = createWorktreeState,
                kanbanTasks = currentKanbanTasks(),
                error = error,
                success = success,
            )
    }

    private fun buildRepositoryItems(): List<RepositoryItem> =
        repositories.map { repository ->
            RepositoryItem(
                id = repository.id.value,
                name = repository.name,
                path = repository.path,
                worktrees =
                    worktreesByRepositoryPath[repository.path]
                        .orEmpty()
                        .sortedWith(
                            compareByDescending<Worktree> { it.isMain }
                                .thenBy { it.name.lowercase() },
                        ).map {
                            WorktreeItem(
                                path = it.path,
                                name = it.name,
                                branch = it.branch,
                                isMain = it.isMain,
                                isLocked = it.isLocked,
                                isPrunable = it.isPrunable,
                            )
                        },
            )
        }

    private fun currentKanbanTasks(): List<KanbanTaskItem> {
        val scopeKey = selectedScopeKey() ?: return emptyList()
        val tasks = tasksByScope.getOrPut(scopeKey) { createDefaultTasks(currentWorktreePath()) }
        return tasks
            .sortedWith(compareBy<KanbanTask> { it.columnId.name }.thenBy { it.order })
            .map {
                KanbanTaskItem(
                    id = it.id.value,
                    title = it.title,
                    description = it.description,
                    columnId = it.columnId.name,
                    order = it.order,
                )
            }
    }

    private fun createDefaultTasks(worktreePath: String?): MutableList<KanbanTask> {
        val now = currentEpochMillis()
        return mutableListOf(
            KanbanTask.create(
                title = "Add unit tests",
                description = "Cover UserService with tests",
                columnId = KanbanColumnType.TODO,
                worktreePath = worktreePath,
                createdAt = now,
                order = 1,
            ),
            KanbanTask.create(
                title = "Update README",
                description = "Add setup instructions for new developers",
                columnId = KanbanColumnType.TODO,
                worktreePath = worktreePath,
                createdAt = now,
                order = 2,
            ),
            KanbanTask.create(
                title = "Refactor data layer",
                description = null,
                columnId = KanbanColumnType.TODO,
                worktreePath = worktreePath,
                createdAt = now,
                order = 3,
            ),
            KanbanTask.create(
                title = "Fix navigation bug",
                description = "Back button not working on detail screen",
                columnId = KanbanColumnType.IN_PROGRESS,
                worktreePath = worktreePath,
                createdAt = now,
                order = 1,
            ),
            KanbanTask.create(
                title = "Review PR #42",
                description = null,
                columnId = KanbanColumnType.REVIEW,
                worktreePath = worktreePath,
                createdAt = now,
                order = 1,
            ),
            KanbanTask.create(
                title = "Implement authentication flow",
                description = "Add login/logout functionality with OAuth2",
                columnId = KanbanColumnType.DONE,
                worktreePath = worktreePath,
                createdAt = now,
                order = 1,
            ),
        )
    }

    private fun mapFailureToErrorState(failure: DomainFailure): ErrorState? {
        val uiError = failureToUiErrorMapper.map(failure) ?: return null
        return mapUiErrorToErrorState(uiError)
    }

    private fun mapUiErrorToErrorState(uiError: UiError): ErrorState {
        val message =
            when (val text = uiError.message) {
                is UiText.Key ->
                    MessageText(
                        key = text.key,
                        args = text.args,
                    )
            }
        val details =
            when (val text = uiError.details) {
                null -> null
                is UiText.Key ->
                    MessageText(
                        key = text.key,
                        args = text.args,
                    )
            }
        return ErrorState(
            code = uiError.code,
            message = message,
            details = details,
            isRetryable = uiError.isRetryable,
        )
    }

    private fun clearMessages() {
        error = null
        success = null
    }

    private fun selectedRepository(): Repository? = repositories.firstOrNull { it.id.value == selectedRepositoryId }

    private fun selectedScopeKey(): String? {
        val repository = selectedRepository() ?: return null
        return selectedWorktreePath?.let { "worktree:$it" } ?: "repo:${repository.id.value}"
    }

    private fun currentWorktreePath(): String? = selectedWorktreePath

    private fun suggestWorktreePath(
        repositoryPath: String,
        branch: String,
    ): String {
        val repoName = repositoryPath.substringAfterLast('/')
        val parentPath = repositoryPath.substringBeforeLast('/', missingDelimiterValue = "")
        val normalizedBranch =
            branch
                .trim()
                .replace("/", "-")
                .replace("\\", "-")
                .replace(" ", "-")
        return if (parentPath.isBlank()) {
            "$repositoryPath-$normalizedBranch"
        } else {
            "$parentPath/$repoName-$normalizedBranch"
        }
    }

    private fun normalizePath(path: String): String = path.trim().trimEnd('/')

    private fun currentEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

    data class State(
        val isLoading: Boolean = false,
        val repositories: List<RepositoryItem> = emptyList(),
        val selectedRepositoryId: String? = null,
        val selectedWorktreePath: String? = null,
        val addRepositoryPathInput: String = "",
        val createWorktree: CreateWorktreeState = CreateWorktreeState(),
        val kanbanTasks: List<KanbanTaskItem> = emptyList(),
        val error: ErrorState? = null,
        val success: SuccessState? = null,
    )

    data class RepositoryItem(
        val id: String,
        val name: String,
        val path: String,
        val worktrees: List<WorktreeItem>,
    )

    data class WorktreeItem(
        val path: String,
        val name: String,
        val branch: String,
        val isMain: Boolean,
        val isLocked: Boolean,
        val isPrunable: Boolean,
    )

    data class CreateWorktreeState(
        val repositoryPath: String = "",
        val branchInput: String = "",
        val worktreePathInput: String = "",
        val baseBranchInput: String = "",
        val createBranch: Boolean = true,
        val isSubmitting: Boolean = false,
        val createdWorktreePath: String? = null,
    )

    data class KanbanTaskItem(
        val id: String,
        val title: String,
        val description: String?,
        val columnId: String,
        val order: Int,
    )

    data class ErrorState(
        val code: String,
        val message: MessageText,
        val details: MessageText?,
        val isRetryable: Boolean,
    )

    data class SuccessState(
        val message: MessageText,
    )

    data class MessageText(
        val key: String,
        val args: List<String>,
    )
}
