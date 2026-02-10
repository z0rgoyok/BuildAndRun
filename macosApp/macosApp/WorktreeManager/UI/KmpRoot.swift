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

    let store: AppStore

    private var cancellation: DecomposeCancellation?

    init(store: AppStore = AppStoreFactory.shared.create()) {
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
            store.onPresentSheet(kind: .addRepository, worktreePath: nil)
        case .addWorktree:
            store.onPresentSheet(kind: .addWorktree, worktreePath: nil)
        case .createPR(let worktreePath):
            store.onPresentSheet(kind: .createPr, worktreePath: worktreePath)
        case .completeWorktree(let worktreePath):
            store.onPresentSheet(kind: .completeWorktree, worktreePath: worktreePath)
        case .configureEditors:
            store.onPresentSheet(kind: .configureEditors, worktreePath: nil)
        case .help:
            store.onPresentSheet(kind: .help, worktreePath: nil)
        }
    }

    func dismissSheet() {
        store.onDismissSheet()
    }

    func selectChild(_ child: Child) {
        switch child {
        case .workspace:
            store.onSelectChild(child: .workspace)
        case .settings:
            store.onSelectChild(child: .settings)
        case .help:
            store.onSelectChild(child: .help)
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
        store.onSetSidebarRepositoryExpanded(repositoryId: repositoryId, expanded: expanded)
    }

    func toggleVisibleSidebarRepositoriesExpansion(selection: SidebarSelection?) {
        let preferredRepositoryId = selection?.repositoryId ?? state.selectedRepositoryId
        store.onToggleVisibleSidebarRepositoriesExpansion(
            includeArchivedRepositories: isSidebarArchivedSectionExpanded,
            preferredRepositoryId: preferredRepositoryId
        )
    }

    func areVisibleSidebarRepositoriesExpanded() -> Bool {
        store.areVisibleSidebarRepositoriesExpanded(includeArchivedRepositories: isSidebarArchivedSectionExpanded)
    }

    func hasVisibleSidebarRepositories() -> Bool {
        store.hasVisibleSidebarRepositories(includeArchivedRepositories: isSidebarArchivedSectionExpanded)
    }

    func syncSidebarSelectionExpansion(selection: SidebarSelection?) {
        guard let currentSelection = selection else { return }
        let selectedRepositoryId = currentSelection.repositoryId
        guard !selectedRepositoryId.isEmpty else { return }

        store.onSyncSidebarSelectionExpansion(repositoryId: selectedRepositoryId)

        let selectedRepository = state.repositories.first { $0.id == selectedRepositoryId }
        if selectedRepository?.isArchived == true {
            isSidebarArchivedSectionExpanded = true
        }
    }

    func setSidebarGroupCollapsed(groupId: String, collapsed: Bool) {
        store.onSetSidebarGroupCollapsed(groupId: groupId, collapsed: collapsed)
    }

    func presentSidebarCopySettings(for repository: AppStore.RepositoryItem) {
        sidebarCopySettingsTarget =
            SidebarCopySettingsTarget(
                id: repository.id,
                name: repository.name
            )
    }
}

struct SidebarCopySettingsTarget: Identifiable {
    let id: String
    let name: String
}
