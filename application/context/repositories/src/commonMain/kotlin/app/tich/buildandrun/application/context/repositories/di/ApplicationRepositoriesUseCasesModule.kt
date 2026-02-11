package app.tich.buildandrun.application.context.repositories.di

import app.tich.buildandrun.application.context.repositories.usecase.*
import org.koin.core.module.Module
import org.koin.dsl.module

fun applicationRepositoriesUseCasesModule(): Module =
    module {
        single { AddRepositoryUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadRepositoriesUseCase(preferencesStore = get()) }
        single { RemoveRepositoryUseCase(preferencesStore = get()) }
        single { SetRepositoryArchivedStateUseCase(preferencesStore = get()) }
        single { SetRepositoryGroupUseCase(preferencesStore = get()) }
        single { RestoreAppSessionUseCase(preferencesStore = get(), loadRepositoriesUseCase = get()) }
        single { AppSessionPersistenceUseCase(preferencesStore = get()) }
        single { PersistKanbanTasksUseCase(preferencesStore = get()) }
        single { ClearKanbanTasksUseCase(preferencesStore = get()) }

        single { ReorderRepositoryGroupsUseCase(preferencesStore = get()) }
        single { CreateRepositoryGroupUseCase(preferencesStore = get()) }
        single { RenameRepositoryGroupUseCase(preferencesStore = get()) }
        single { DeleteRepositoryGroupUseCase(preferencesStore = get()) }

        single { SetWorktreeBasePathUseCase(preferencesStore = get()) }
        single { LoadPreferredBaseBranchUseCase(preferencesStore = get()) }
        single { SetPreferredBaseBranchUseCase(preferencesStore = get()) }
        single { SetDefaultCopyPatternsUseCase(preferencesStore = get()) }
        single { SetRepositoryCopyPatternsUseCase(preferencesStore = get()) }

        single { SetRememberEditorChoiceUseCase(preferencesStore = get()) }
        single { SetEditorEnabledUseCase(preferencesStore = get()) }
        single { SetPreferredEditorUseCase(preferencesStore = get()) }
        single { OpenInEditorUseCase(preferencesStore = get(), editorOpening = get()) }
        single { LoadPresentationPreferencesUseCase(preferencesStore = get()) }

        single { SetSidebarMembershipStateUseCase(preferencesStore = get()) }
        single { ToggleSidebarRepositoriesExpansionUseCase(preferencesStore = get()) }
        single { SyncSidebarSelectionExpansionUseCase(preferencesStore = get()) }

        single { AddKanbanTaskUseCase() }
        single { MoveKanbanTaskUseCase() }
        single { DeleteKanbanTaskUseCase() }
        single { UpdateKanbanTaskUseCase() }
    }
