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

    @Published private(set) var navigationState: AppNavigationState
    @Published private(set) var activityState: ActivityState
    @Published private(set) var repositoriesState: RepositoriesState
    @Published private(set) var worktreesState: WorktreesState
    @Published private(set) var settingsState: SettingsState
    @Published private(set) var editorsState: EditorsState
    @Published private(set) var kanbanState: KanbanState
    @Published private(set) var messagesState: MessagesState
    @Published var isSidebarArchivedSectionExpanded: Bool = false
    @Published var sidebarCopySettingsTarget: SidebarCopySettingsTarget?

    let store: AppRootComponent
    private var cancellations: [DecomposeCancellation] = []

    init(store: AppRootComponent = AppRootFactory.shared.create()) {
        self.store = store
        self.navigationState = store.navigationState.value
        self.activityState = store.activityState.value
        self.repositoriesState = store.repositoriesState.value
        self.worktreesState = store.worktreesState.value
        self.settingsState = store.settingsState.value
        self.editorsState = store.editorsState.value
        self.kanbanState = store.kanbanState.value
        self.messagesState = store.messagesState.value

        cancellations = [
            store.navigationState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.navigationState = nextState
                }
            },
            store.activityState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.activityState = nextState
                }
            },
            store.repositoriesState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.updateRepositoriesState(nextState)
                }
            },
            store.worktreesState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.worktreesState = nextState
                }
            },
            store.settingsState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.settingsState = nextState
                }
            },
            store.editorsState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.editorsState = nextState
                }
            },
            store.kanbanState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.kanbanState = nextState
                }
            },
            store.messagesState.subscribe { [weak self] nextState in
                Task { @MainActor [weak self] in
                    self?.messagesState = nextState
                }
            }
        ]
    }

    deinit {
        cancellations.forEach { $0.cancel() }
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
        if navigationState.activeChild == .workspace {
            return .workspace
        }
        if navigationState.activeChild == .settings {
            return .settings
        }
        return .help
    }

    var sheet: Sheet? {
        guard let activeSheet = navigationState.activeSheet else {
            return nil
        }
        if activeSheet.kind == .addRepository {
            return .addRepository
        }
        if activeSheet.kind == .addWorktree {
            return .addWorktree
        }
        if activeSheet.kind == .configureEditors {
            return .configureEditors
        }
        if activeSheet.kind == .help {
            return .help
        }
        if activeSheet.kind == .createPr {
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

    var repositories: [RepositoryItem] {
        repositoriesState.repositories
    }

    var selectedRepository: RepositoryItem? {
        guard let selectedId = repositoriesState.selectedRepositoryId else { return nil }
        return repositoriesState.repositories.first { $0.id == selectedId }
    }

    var selectedWorktree: WorktreeItem? {
        guard let selectedPath = worktreesState.selectedWorktreePath else { return nil }
        return selectedRepository?.worktrees.first { $0.path == selectedPath }
    }

    func isSidebarRepositoryExpanded(repositoryId: String) -> Bool {
        repositoriesState.expandedRepositoryIds.contains(repositoryId)
    }

    func isSidebarGroupCollapsed(groupId: String) -> Bool {
        repositoriesState.collapsedGroupIds.contains(groupId)
    }

    func setSidebarRepositoryExpanded(repositoryId: String, expanded: Bool) {
        store.sidebar.onSetSidebarRepositoryExpanded(repositoryId: repositoryId, expanded: expanded)
    }

    func toggleVisibleSidebarRepositoriesExpansion(selection: SidebarSelection?) {
        let preferredRepositoryId = selection?.repositoryId ?? repositoriesState.selectedRepositoryId
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

        let selectedRepository = repositoriesState.repositories.first { $0.id == selectedRepositoryId }
        if selectedRepository?.isArchived == true {
            isSidebarArchivedSectionExpanded = true
        }
    }

    func setSidebarGroupCollapsed(groupId: String, collapsed: Bool) {
        store.sidebar.onSetSidebarGroupCollapsed(groupId: groupId, collapsed: collapsed)
    }

    func presentSidebarCopySettings(for repository: RepositoryItem) {
        sidebarCopySettingsTarget =
            SidebarCopySettingsTarget(
                id: repository.id,
                name: repository.name
            )
    }

    private func updateRepositoriesState(_ nextState: RepositoriesState) {
        if repositoriesState.sidebarSections.isEmpty {
            repositoriesState = nextState
        } else {
            withAnimation(DS.Animation.quick) {
                repositoriesState = nextState
            }
        }
    }
}
