import Combine
import Foundation
import Shared

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
    @Published var child: Child = .workspace
    @Published var sheet: Sheet? = nil

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
        self.sheet = sheet
    }

    func dismissSheet() {
        sheet = nil
    }

    func selectChild(_ child: Child) {
        self.child = child
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
}
