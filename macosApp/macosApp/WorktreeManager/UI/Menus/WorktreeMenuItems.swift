import AppKit
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
        let worktree: AppStore.WorktreeItem

        private var configuredEditors: [AppStore.EditorItem] {
            root.state.editors.filter { $0.isEnabled && $0.isInstalled }
        }

        private var selectedEditorId: String {
            root.state.preferredEditorId ?? ""
        }

        var body: some View {
            Section {
                Button("Open in Editor") {
                    root.store.onOpenInEditor(
                        worktreePath: worktree.path,
                        editorId: root.state.preferredEditorId,
                    )
                }
                .keyboardShortcut("o", modifiers: .command)
                .disabled(configuredEditors.isEmpty)

                Menu("Open in...") {
                    if configuredEditors.isEmpty {
                        Text("No configured editors")
                            .foregroundStyle(.secondary)
                    } else {
                        Picker(
                            "",
                            selection: Binding(
                                get: { selectedEditorId },
                                set: { newId in
                                    root.store.onSetPreferredEditor(editorId: newId)
                                    root.store.onOpenInEditor(
                                        worktreePath: worktree.path,
                                        editorId: newId,
                                    )
                                }
                            )
                        ) {
                            ForEach(configuredEditors, id: \.id) { editor in
                                Text(editor.name).tag(editor.id)
                            }
                        }
                        .pickerStyle(.inline)
                        .labelsHidden()
                    }

                    Divider()

                    Button(root.state.rememberEditorChoice ? "Forget Editor Choice" : "Remember Editor Choice") {
                        root.store.onSetRememberEditorChoice(value: !root.state.rememberEditorChoice)
                        if root.state.rememberEditorChoice {
                            root.store.onSetPreferredEditor(editorId: nil)
                        }
                    }

                    Button("Configure Editors...") {
                        root.presentSheet(.configureEditors)
                    }
                }
                .disabled(configuredEditors.isEmpty)
            }

            Section {
                Button("Show in Finder") {
                    root.store.onOpenInFinder(worktreePath: worktree.path)
                }
                .keyboardShortcut("f", modifiers: [.command, .shift])

                Button("Open in Terminal") {
                    root.store.onOpenInTerminal(worktreePath: worktree.path)
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
                    Button("Push") {
                        root.store.onPush(worktreePath: worktree.path)
                    }
                    .keyboardShortcut("p", modifiers: [.command, .shift])

                    Button("Pull") {
                        root.store.onPull(worktreePath: worktree.path)
                    }
                    .keyboardShortcut("p", modifiers: [.command, .option])
                }

                Section {
                    if worktree.status?.prStatus != nil {
                        Button("View Pull Request") {
                            root.store.onOpenPullRequest(worktreePath: worktree.path)
                        }
                    } else {
                        Button("Create Pull Request...") {
                            root.presentSheet(.createPR(worktreePath: worktree.path))
                        }
                    }
                }

                Section {
                    if worktree.isLocked {
                        Button("Unlock") {
                            root.store.onUnlockWorktree(worktreePath: worktree.path)
                        }
                    } else {
                        Button("Lock") {
                            root.store.onLockWorktree(worktreePath: worktree.path)
                        }
                    }
                }

                Section {
                    Button("Finish Worktree...") {
                        root.presentSheet(.completeWorktree(worktreePath: worktree.path))
                    }

                    Button("Remove Worktree", role: .destructive) {
                        root.store.onRemoveWorktree(
                            worktreePath: worktree.path,
                            force: true,
                            deleteBranch: true,
                        )
                    }
                }
            }

            Section {
                Button("Refresh Status") {
                    root.store.onRefreshWorktreeStatus(worktreePath: worktree.path)
                }
                .keyboardShortcut("r", modifiers: .command)
            }
        }
    }
}
