import Foundation

struct SidebarKeyboardNavigation {
    enum Key: Equatable {
        case up
        case down
        case left
        case right
        case space
    }

    struct Output: Equatable {
        var selection: SidebarSelection?
        var expandedRepositoryIds: Set<String>
    }

    static func handle(
        key: Key,
        selection: SidebarSelection?,
        repositories: [String],
        expandedRepositoryIds: Set<String>
    ) -> Output {
        var expanded = expandedRepositoryIds
        var selection = selection

        switch key {
        case .up, .down:
            let visible = visibleSelections(
                repositories: repositories,
                expandedRepositoryIds: expanded
            )

            guard !visible.isEmpty else {
                return Output(selection: selection, expandedRepositoryIds: expanded)
            }

            let currentIndex = selection.flatMap { indexOf(selection: $0, in: visible) }

            if key == .down {
                if let currentIndex, visible.indices.contains(currentIndex + 1) {
                    selection = visible[currentIndex + 1]
                } else {
                    selection = visible.first
                }
            } else {
                if let currentIndex, visible.indices.contains(currentIndex - 1) {
                    selection = visible[currentIndex - 1]
                } else {
                    selection = visible.last
                }
            }

        case .right:
            switch selection {
            case .repository(let repositoryId):
                expanded.insert(repositoryId)
            case .worktree, .none:
                break
            }

        case .left:
            switch selection {
            case .worktree(_, let repositoryId):
                selection = .repository(repositoryId: repositoryId)
            case .repository(let repositoryId):
                if expanded.contains(repositoryId) {
                    expanded.remove(repositoryId)
                }
            case .none:
                break
            }

        case .space:
            switch selection {
            case .repository(let repositoryId):
                if expanded.contains(repositoryId) {
                    expanded.remove(repositoryId)
                } else {
                    expanded.insert(repositoryId)
                }
            case .worktree, .none:
                break
            }
        }

        return Output(selection: selection, expandedRepositoryIds: expanded)
    }

    private static func visibleSelections(
        repositories: [String],
        expandedRepositoryIds: Set<String>
    ) -> [SidebarSelection] {
        var visible: [SidebarSelection] = []
        visible.reserveCapacity(repositories.count)

        for repositoryId in repositories {
            visible.append(.repository(repositoryId: repositoryId))
            if expandedRepositoryIds.contains(repositoryId) {
                continue
            }
        }

        return visible
    }

    private static func indexOf(selection: SidebarSelection, in visible: [SidebarSelection]) -> Int? {
        for (index, item) in visible.enumerated() {
            if isSameSelection(lhs: item, rhs: selection) {
                return index
            }
        }

        return nil
    }

    private static func isSameSelection(lhs: SidebarSelection, rhs: SidebarSelection) -> Bool {
        switch (lhs, rhs) {
        case (.repository(let a), .repository(let b)):
            return a == b
        case (.worktree(let aPath, let aRepoId), .worktree(let bPath, let bRepoId)):
            return aPath == bPath && aRepoId == bRepoId
        case (.repository, .worktree), (.worktree, .repository):
            return false
        }
    }
}
