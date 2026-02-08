import Shared
import SwiftUI

struct WorktreeMenuItems: View {
    @ObservedObject var root: KmpRoot
    let worktreePath: String?
    let includeNewWorktree: Bool

    init(root: KmpRoot, worktreePath: String?, includeNewWorktree: Bool) {
        self.root = root
        self.worktreePath = worktreePath
        self.includeNewWorktree = includeNewWorktree
    }

    var body: some View {
        Group {
            if let worktree = root.selectedWorktree, worktreePath == worktree.path {
                WorktreeBoundMenuItems(root: root, worktree: worktree)

                if includeNewWorktree {
                    Divider()
                    Button("New Worktree...") {
                        root.presentSheet(.addWorktree)
                    }
                    .keyboardShortcut("n", modifiers: .command)
                    .disabled(root.selectedRepository == nil)
                }
            } else {
                WorktreeUnboundMenuItems()
            }
        }
    }

    private struct WorktreeUnboundMenuItems: View {
        var body: some View {
            Section {
                Button("Open in Editor") {}
                    .keyboardShortcut("o", modifiers: .command)
                    .disabled(true)

                Menu("Open in...") {}
                    .disabled(true)
            }

            Section {
                Button("Show in Finder") {}
                    .keyboardShortcut("f", modifiers: [.command, .shift])
                    .disabled(true)

                Button("Open in Terminal") {}
                    .keyboardShortcut("t", modifiers: [.command, .shift])
                    .disabled(true)

                Button("Copy Path") {}
                    .keyboardShortcut("c", modifiers: [.command, .shift])
                    .disabled(true)
            }

            Section {
                Button("Push") {}
                    .keyboardShortcut("p", modifiers: [.command, .shift])
                    .disabled(true)

                Button("Pull") {}
                    .keyboardShortcut("p", modifiers: [.command, .option])
                    .disabled(true)
            }

            Section {
                Button("Refresh Status") {}
                    .keyboardShortcut("r", modifiers: .command)
                    .disabled(true)
            }
        }
    }

    private struct WorktreeBoundMenuItems: View {
        @ObservedObject var root: KmpRoot
        let worktree: MacOSAppStore.WorktreeItem

        var body: some View {
            Section {
                Button("Open in Editor") {}
                .keyboardShortcut("o", modifiers: .command)
                .disabled(true)

                Menu("Open in...") {}
                    .disabled(true)
            }

            Section {
                Button("Show in Finder") {
                    NSWorkspace.shared.selectFile(nil, inFileViewerRootedAtPath: worktree.path)
                }
                .keyboardShortcut("f", modifiers: [.command, .shift])

                Button("Open in Terminal") {
                    NSWorkspace.shared.open(URL(fileURLWithPath: worktree.path))
                }
                .keyboardShortcut("t", modifiers: [.command, .shift])

                Button("Copy Path") {
                    NSPasteboard.general.clearContents()
                    NSPasteboard.general.setString(worktree.path, forType: .string)
                }
                .keyboardShortcut("c", modifiers: [.command, .shift])
            }

            if !worktree.isMain {
                Section {
                    Button("Push") {}
                    .keyboardShortcut("p", modifiers: [.command, .shift])
                    .disabled(true)

                    Button("Pull") {}
                    .keyboardShortcut("p", modifiers: [.command, .option])
                    .disabled(true)
                }

                Section {
                    Button("Create Pull Request...") {}
                        .disabled(true)
                }

                Section {
                    if worktree.isLocked {
                        Button("Unlock") {}
                            .disabled(true)
                    } else {
                        Button("Lock") {}
                            .disabled(true)
                    }
                }

                Section {
                    Button("Finish Worktree...") {
                        root.presentSheet(.completeWorktree(worktreePath: worktree.path))
                    }
                    .disabled(true)
                }
            }

            Section {
                Button("Refresh Status") {}
                .keyboardShortcut("r", modifiers: .command)
                .disabled(true)
            }
        }
    }
}
