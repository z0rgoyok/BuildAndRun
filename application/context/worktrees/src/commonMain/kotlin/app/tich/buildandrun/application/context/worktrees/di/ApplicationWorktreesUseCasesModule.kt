package app.tich.buildandrun.application.context.worktrees.di

import app.tich.buildandrun.application.context.worktrees.usecase.*
import org.koin.core.module.Module
import org.koin.dsl.module

fun applicationWorktreesUseCasesModule(): Module =
    module {
        single { CreateWorktreeUseCase(gitClient = get()) }
        single { LoadBranchesUseCase(gitClient = get()) }
        single { CopyConfiguredFilesUseCase(preferencesStore = get(), fileSystemHandling = get()) }
        single { LoadRepositoryWorktreesUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadWorktreeStatusUseCase(gitClient = get()) }
        single {
            LoadRepositoryWorktreeSnapshotUseCase(
                loadRepositoryWorktreesUseCase = get(),
                loadWorktreeStatusUseCase = get(),
            )
        }
        single {
            CreateWorktreeFlowUseCase(
                createWorktreeUseCase = get(),
                copyConfiguredFilesUseCase = get(),
                loadRepositoryWorktreesUseCase = get(),
                loadBranchesUseCase = get(),
                preferencesStore = get(),
            )
        }
        single { PushWorktreeUseCase(gitClient = get()) }
        single { PullWorktreeUseCase(gitClient = get()) }
        single { CreatePullRequestUseCase(gitClient = get()) }
        single { LoadPullRequestUrlUseCase(gitClient = get()) }
        single { LockWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { UnlockWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { RemoveWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { CompleteWorktreeUseCase(gitClient = get(), preferencesStore = get()) }
        single { LoadHasRemoteBranchUseCase(gitClient = get()) }
        single { PruneWorktreesUseCase(gitClient = get(), preferencesStore = get()) }
        single { ReconcileSelectedWorktreePathUseCase() }
    }
