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
    @Published var sidebarExpandedRepositoryIds: Set<String> = []
    @Published var isSidebarArchivedSectionExpanded: Bool = false
    @Published var sidebarCopySettingsTarget: SidebarCopySettingsTarget?

    let store: AppStore

    private var cancellation: DecomposeCancellation?

    init(store: AppStore = AppStoreFactory.shared.create()) {
        self.store = store
        self.state = store.state.value
        self.sidebarExpandedRepositoryIds = store.loadExpandedRepositoryIds() 

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

    var areAllSidebarRepositoriesExpanded: Bool {
        let allRepositoryIds = Set(state.repositories.map(\.id))
        return !allRepositoryIds.isEmpty && allRepositoryIds.isSubset(of: sidebarExpandedRepositoryIds)
    }

    func setSidebarRepositoryExpanded(repositoryId: String, expanded: Bool) {
        if expanded {
            sidebarExpandedRepositoryIds.insert(repositoryId)
        } else {
            sidebarExpandedRepositoryIds.remove(repositoryId)
        }
        store.setExpandedRepositoryIds(ids: sidebarExpandedRepositoryIds)
    }

    func toggleSidebarAllRepositoriesExpansion(selection: SidebarSelection?) {
        let allRepositoryIds = Set(state.repositories.map(\.id))
        guard !allRepositoryIds.isEmpty else { return }

        if areAllSidebarRepositoriesExpanded {
            sidebarExpandedRepositoryIds = []
            if let selectedRepositoryId = selection?.repositoryId, !selectedRepositoryId.isEmpty {
                sidebarExpandedRepositoryIds.insert(selectedRepositoryId)
            } else if let selectedRepositoryId = state.selectedRepositoryId, !selectedRepositoryId.isEmpty {
                sidebarExpandedRepositoryIds.insert(selectedRepositoryId)
            }
        } else {
            sidebarExpandedRepositoryIds.formUnion(allRepositoryIds)
        }

        store.setExpandedRepositoryIds(ids: sidebarExpandedRepositoryIds)
    }

    func syncSidebarSelectionExpansion(selection: SidebarSelection?) {
        guard let currentSelection = selection else { return }
        let selectedRepositoryId = currentSelection.repositoryId
        guard !selectedRepositoryId.isEmpty else { return }

        sidebarExpandedRepositoryIds.insert(selectedRepositoryId)
        store.setExpandedRepositoryIds(ids: sidebarExpandedRepositoryIds)

        let selectedRepository = state.repositories.first { $0.id == selectedRepositoryId }
        if selectedRepository?.isArchived == true {
            isSidebarArchivedSectionExpanded = true
        }
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
