import SwiftUI

// MARK: - Worktree Commands

struct WorktreeCommands: Commands {
    @ObservedObject var root: KmpRoot

    var body: some Commands {
        CommandMenu("Worktree") {
            WorktreeMenuItems(root: root, worktreePath: root.state.selectedWorktreePath, includeNewWorktree: true)
        }

        CommandMenu("Repository") {
            Section {
                Button("Add Repository...") {
                    root.presentSheet(.addRepository)
                }
                .keyboardShortcut("o", modifiers: [.command, .shift])

                Button("Show in Finder") {
                    guard let repo = root.selectedRepository else { return }
                    NSWorkspace.shared.selectFile(nil, inFileViewerRootedAtPath: repo.path)
                }
                .disabled(root.selectedRepository == nil)
            }

            Section {
                if let repo = root.selectedRepository {
                    if repo.isArchived {
                        Button("Restore Project") {
                            root.store.onRestoreRepository(repositoryId: repo.id)
                        }
                    } else {
                        Button("Archive Project") {
                            root.store.onArchiveRepository(repositoryId: repo.id)
                        }
                    }
                } else {
                    Button("Archive Project") {}
                        .disabled(true)
                }
            }

            Section {
                Button("Refresh All") {
                    root.store.onRefreshSelectedRepository()
                }
                .keyboardShortcut("r", modifiers: [.command, .shift])
                .disabled(root.selectedRepository == nil)
            }
        }
    }
}
