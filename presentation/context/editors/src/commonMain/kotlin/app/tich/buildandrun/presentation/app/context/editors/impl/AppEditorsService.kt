package app.tich.buildandrun.presentation.app.context.editors.impl

import app.tich.buildandrun.application.context.repositories.usecase.OpenInEditorUseCase
import app.tich.buildandrun.application.context.repositories.usecase.SetEditorEnabledUseCase
import app.tich.buildandrun.application.context.repositories.usecase.SetPreferredEditorUseCase
import app.tich.buildandrun.application.context.repositories.usecase.SetRememberEditorChoiceUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenPathInFinderUseCase
import app.tich.buildandrun.application.context.shared.usecase.OpenPathInTerminalUseCase
import app.tich.buildandrun.application.context.shared.usecase.UseCaseResult
import app.tich.buildandrun.domain.context.editors.model.Editor
import app.tich.buildandrun.presentation.app.AppEditorsFeature
import app.tich.buildandrun.presentation.app.context.state.EditorsContextState
import app.tich.buildandrun.presentation.app.context.state.MessagesContextState
import app.tich.buildandrun.presentation.app.context.state.RepositoriesContextState
import app.tich.buildandrun.presentation.app.core.AppErrorStateMapper
import app.tich.buildandrun.presentation.app.core.AppExecutionScope
import app.tich.buildandrun.presentation.app.core.AppStateRefresher
import kotlinx.coroutines.launch

class AppEditorsService(
    private val executionScope: AppExecutionScope,
    private val stateRefresher: AppStateRefresher,
    private val errorMapper: AppErrorStateMapper,
    private val repositoriesState: RepositoriesContextState,
    private val editorsState: EditorsContextState,
    private val messagesState: MessagesContextState,
    private val setRememberEditorChoiceUseCase: SetRememberEditorChoiceUseCase,
    private val setEditorEnabledUseCase: SetEditorEnabledUseCase,
    private val setPreferredEditorUseCase: SetPreferredEditorUseCase,
    private val openInEditorUseCase: OpenInEditorUseCase,
    private val openPathInFinderUseCase: OpenPathInFinderUseCase,
    private val openPathInTerminalUseCase: OpenPathInTerminalUseCase,
) : AppEditorsFeature {
    override fun onSetRememberEditorChoice(value: Boolean) {
        val repositoryId = repositoriesState.selectedRepository()?.id?.value
        when (
            val result =
                setRememberEditorChoiceUseCase.execute(
                    input = SetRememberEditorChoiceUseCase.Input(value = value, repositoryId = repositoryId),
                )
        ) {
            is UseCaseResult.Success -> {
                editorsState.rememberEditorChoice = result.value.rememberEditorChoice
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onSetEditorEnabled(
        editorId: String,
        enabled: Boolean,
    ) {
        when (
            val result =
                setEditorEnabledUseCase.execute(
                    input =
                        SetEditorEnabledUseCase.Input(
                            editorId = editorId,
                            enabled = enabled,
                            allEditorIds = editorsState.allEditors.map(Editor::id),
                            currentEnabledEditorIds = editorsState.enabledEditorIds,
                        ),
                )
        ) {
            is UseCaseResult.Success -> {
                editorsState.enabledEditorIds = result.value.enabledEditorIds
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onSetPreferredEditor(editorId: String?) {
        val repository = repositoriesState.selectedRepository() ?: return
        when (
            val result =
                setPreferredEditorUseCase.execute(
                    input = SetPreferredEditorUseCase.Input(repositoryId = repository.id.value, editorId = editorId),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
            }
        }
        stateRefresher.publishAll()
    }

    override fun onOpenInEditor(
        worktreePath: String,
        editorId: String?,
    ) {
        val repositoryId = repositoriesState.selectedRepository()?.id?.value
        executionScope.scope.launch {
            when (
                val result =
                    openInEditorUseCase.execute(
                        input =
                            OpenInEditorUseCase.Input(
                                worktreePath = worktreePath,
                                editorId = editorId,
                                repositoryId = repositoryId,
                                rememberEditorChoice = editorsState.rememberEditorChoice,
                                enabledInstalledEditorIds =
                                    editorsState.editorItems
                                        .asSequence()
                                        .filter { it.isEnabled && it.isInstalled }
                                        .map { it.id }
                                        .toList(),
                                availableEditors = editorsState.allEditors,
                            ),
                    )
            ) {
                is UseCaseResult.Success -> {
                    messagesState.clear()
                }

                is UseCaseResult.Failure -> {
                    messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                }
            }
            stateRefresher.publishAll()
        }
    }

    override fun onOpenInFinder(worktreePath: String) {
        when (
            val result =
                openPathInFinderUseCase.execute(
                    input = OpenPathInFinderUseCase.Input(path = worktreePath),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                stateRefresher.publishAll()
            }
        }
    }

    override fun onOpenInTerminal(worktreePath: String) {
        when (
            val result =
                openPathInTerminalUseCase.execute(
                    input = OpenPathInTerminalUseCase.Input(path = worktreePath),
                )
        ) {
            is UseCaseResult.Success -> {
            }

            is UseCaseResult.Failure -> {
                messagesState.error = errorMapper.mapFailureToErrorState(result.value)
                stateRefresher.publishAll()
            }
        }
    }
}
