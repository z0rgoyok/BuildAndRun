import Combine
import Foundation
import Shared
import SwiftUI

@MainActor
final class KmpRoot: ObservableObject {
    enum Child: Equatable {
        case workspace
        case settings
        case help
    }

    enum Sheet: Identifiable, Equatable {
        case addRepository
        case addWorktree
        case createPR(worktreePath: String)
        case completeWorktree(worktreePath: String)
        case configureEditors
        case help

        var id: String {
            switch self {
            case .addRepository: return "addRepository"
            case .addWorktree: return "addWorktree"
            case .createPR(let path): return "createPR:\(path)"
            case .completeWorktree(let path): return "completeWorktree:\(path)"
            case .configureEditors: return "configureEditors"
            case .help: return "help"
            }
        }
    }

    @Published private(set) var state: AppStore.State
    @Published var isSidebarArchivedSectionExpanded: Bool = false
    @Published var sidebarCopySettingsTarget: SidebarCopySettingsTarget?

    let store: AppRootComponent
    private var cancellation: DecomposeCancellation?

    init(store: AppRootComponent = AppStoreFactory.shared.create()) {
        self.store = store
        self.state = store.state.value

        self.cancellation =
            store.state.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.state = nextState
                }
            }
    }

    deinit {
        cancellation?.cancel()
        store.destroy()
    }

    func presentSheet(_ sheet: Sheet) {
        switch sheet {
        case .addRepository:
            store.navigation.onPresentSheet(kind: .addRepository, worktreePath: nil)
        case .addWorktree:
            store.navigation.onPresentSheet(kind: .addWorktree, worktreePath: nil)
        case .createPR(let worktreePath):
            store.navigation.onPresentSheet(kind: .createPr, worktreePath: worktreePath)
        case .completeWorktree(let worktreePath):
            store.navigation.onPresentSheet(kind: .completeWorktree, worktreePath: worktreePath)
        case .configureEditors:
            store.navigation.onPresentSheet(kind: .configureEditors, worktreePath: nil)
        case .help:
            store.navigation.onPresentSheet(kind: .help, worktreePath: nil)
        }
    }

    func dismissSheet() {
        store.navigation.onDismissSheet()
    }

    func selectChild(_ child: Child) {
        switch child {
        case .workspace:
            store.navigation.onSelectChild(child: .workspace)
        case .settings:
            store.navigation.onSelectChild(child: .settings)
        case .help:
            store.navigation.onSelectChild(child: .help)
        }
    }

    var child: Child {
        if state.activeChild === AppChild.workspace {
            return .workspace
        }
        if state.activeChild === AppChild.settings {
            return .settings
        }
        return .help
    }

    var sheet: Sheet? {
        guard let activeSheet = state.activeSheet else {
            return nil
        }
        if activeSheet.kind === AppSheetKind.addRepository {
            return .addRepository
        }
        if activeSheet.kind === AppSheetKind.addWorktree {
            return .addWorktree
        }
        if activeSheet.kind === AppSheetKind.configureEditors {
            return .configureEditors
        }
        if activeSheet.kind === AppSheetKind.help {
            return .help
        }
        if activeSheet.kind === AppSheetKind.createPr {
            guard let worktreePath = activeSheet.worktreePath else {
                return nil
            }
            return .createPR(worktreePath: worktreePath)
        }
        guard let worktreePath = activeSheet.worktreePath else {
            return nil
        }
        return .completeWorktree(worktreePath: worktreePath)
    }

    var repositories: [AppStore.RepositoryItem] {
        state.repositories
    }

    var selectedRepository: AppStore.RepositoryItem? {
        guard let selectedId = state.selectedRepositoryId else { return nil }
        return state.repositories.first { $0.id == selectedId }
    }

    var selectedWorktree: AppStore.WorktreeItem? {
        guard let selectedPath = state.selectedWorktreePath else { return nil }
        return selectedRepository?.worktrees.first { $0.path == selectedPath }
    }

    func isSidebarRepositoryExpanded(repositoryId: String) -> Bool {
        state.expandedRepositoryIds.contains(repositoryId)
    }

    func isSidebarGroupCollapsed(groupId: String) -> Bool {
        state.collapsedGroupIds.contains(groupId)
    }

    func setSidebarRepositoryExpanded(repositoryId: String, expanded: Bool) {
        store.sidebar.onSetSidebarRepositoryExpanded(repositoryId: repositoryId, expanded: expanded)
    }

    func toggleVisibleSidebarRepositoriesExpansion(selection: SidebarSelection?) {
        let preferredRepositoryId = selection?.repositoryId ?? state.selectedRepositoryId
        store.sidebar.onToggleVisibleSidebarRepositoriesExpansion(
            includeArchivedRepositories: isSidebarArchivedSectionExpanded,
            preferredRepositoryId: preferredRepositoryId
        )
    }

    func areVisibleSidebarRepositoriesExpanded() -> Bool {
        store.sidebar.areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: isSidebarArchivedSectionExpanded)
    }

    func hasVisibleSidebarRepositories() -> Bool {
        store.sidebar.hasVisibleSidebarRepositories(includeArchivedRepositories: isSidebarArchivedSectionExpanded)
    }

    func syncSidebarSelectionExpansion(selection: SidebarSelection?) {
        guard let currentSelection = selection else { return }
        let selectedRepositoryId = currentSelection.repositoryId
        guard !selectedRepositoryId.isEmpty else { return }

        store.sidebar.onSyncSidebarSelectionExpansion(repositoryId: selectedRepositoryId)

        let selectedRepository = state.repositories.first { $0.id == selectedRepositoryId }
        if selectedRepository?.isArchived == true {
            isSidebarArchivedSectionExpanded = true
        }
    }

    func setSidebarGroupCollapsed(groupId: String, collapsed: Bool) {
        store.sidebar.onSetSidebarGroupCollapsed(groupId: groupId, collapsed: collapsed)
    }

    func presentSidebarCopySettings(for repository: AppStore.RepositoryItem) {
        sidebarCopySettingsTarget =
            SidebarCopySettingsTarget(
                id: repository.id,
                name: repository.name
            )
    }
}
