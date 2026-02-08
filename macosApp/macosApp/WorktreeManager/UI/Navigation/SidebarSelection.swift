import Foundation

enum SidebarSelection: Hashable {
    case repository(repositoryId: String)
    case worktree(worktreePath: String, repositoryId: String)

    var repositoryId: String {
        switch self {
        case .repository(let repositoryId):
            return repositoryId
        case .worktree(_, let repositoryId):
            return repositoryId
        }
    }

    var worktreePath: String? {
        switch self {
        case .repository:
            return nil
        case .worktree(let worktreePath, _):
            return worktreePath
        }
    }

    var displayName: String {
        switch self {
        case .repository(let repositoryId):
            return repositoryId
        case .worktree(let worktreePath, _):
            return URL(fileURLWithPath: worktreePath).lastPathComponent
        }
    }
}
