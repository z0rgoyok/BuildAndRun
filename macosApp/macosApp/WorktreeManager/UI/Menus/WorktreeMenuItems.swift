import AppKit
import Shared
import SwiftUI

struct WorktreeMenuItems: View {
    @ObservedObject var root: KmpRoot
    let worktreePath: String?
    let includeNewWorktree: Bool

    private var labels: KanbanLabels { root.store.kanbanLabels }

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
                    Button(labels.newWorktree + "…") {
                        root.presentSheet(.addWorktree)
                    }
                    .keyboardShortcut("n", modifiers: .command)
                    .disabled(root.selectedRepository == nil)
                }
            } else {
                WorktreeUnboundMenuItems(labels: labels)
            }
        }
    }

    private struct WorktreeUnboundMenuItems: View {
        let labels: KanbanLabels

        var body: some View {
            Section {
                Button(labels.openInEditor) {}
                    .keyboardShortcut("o", modifiers: .command)
                    .disabled(true)

                Menu(labels.openIn) {}
                    .disabled(true)
            }

            Section {
                Button(labels.showInFinder) {}
                    .keyboardShortcut("f", modifiers: [.command, .shift])
                    .disabled(true)

                Button(labels.openInTerminal) {}
                    .keyboardShortcut("t", modifiers: [.command, .shift])
                    .disabled(true)

                Button(labels.copyPath) {}
                    .keyboardShortcut("c", modifiers: [.command, .shift])
                    .disabled(true)
            }

            Section {
                Button(labels.push) {}
                    .keyboardShortcut("p", modifiers: [.command, .shift])
                    .disabled(true)

                Button(labels.pull) {}
                    .keyboardShortcut("p", modifiers: [.command, .option])
                    .disabled(true)
            }

            Section {
                Button(labels.refreshStatus) {}
                    .keyboardShortcut("r", modifiers: .command)
                    .disabled(true)
            }
        }
    }

    private struct WorktreeBoundMenuItems: View {
        @ObservedObject var root: KmpRoot
        let worktree: AppStore.WorktreeItem

        private var labels: KanbanLabels { root.store.kanbanLabels }

        private var configuredEditors: [AppStore.EditorItem] {
            root.state.editors.filter { $0.isEnabled && $0.isInstalled }
        }

        private var selectedEditorId: String {
            root.state.preferredEditorId ?? ""
        }

        var body: some View {
            Section {
                Button(labels.openInEditor) {
                    root.store.editors.onOpenInEditor(
                        worktreePath: worktree.path,
                        editorId: root.state.preferredEditorId,
                    )
                }
                .keyboardShortcut("o", modifiers: .command)
                .disabled(configuredEditors.isEmpty)

                Menu(labels.openIn) {
                    if configuredEditors.isEmpty {
                        Text(labels.noConfiguredEditors)
                            .foregroundStyle(.secondary)
                    } else {
                        Picker(
                            "",
                            selection: Binding(
                                get: { selectedEditorId },
                                set: { newId in
                                    root.store.editors.onSetPreferredEditor(editorId: newId)
                                    root.store.editors.onOpenInEditor(
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

                    Button(root.state.rememberEditorChoice ? labels.forgetEditorChoice : labels.rememberEditorChoice) {
                        root.store.editors.onSetRememberEditorChoice(value: !root.state.rememberEditorChoice)
                        if root.state.rememberEditorChoice {
                            root.store.editors.onSetPreferredEditor(editorId: nil)
                        }
                    }

                    Button(labels.configureEditors) {
                        root.presentSheet(.configureEditors)
                    }
                }
                .disabled(configuredEditors.isEmpty)
            }

            Section {
                Button(labels.showInFinder) {
                    root.store.editors.onOpenInFinder(worktreePath: worktree.path)
                }
                .keyboardShortcut("f", modifiers: [.command, .shift])

                Button(labels.openInTerminal) {
                    root.store.editors.onOpenInTerminal(worktreePath: worktree.path)
                }
                .keyboardShortcut("t", modifiers: [.command, .shift])

                Button(labels.copyPath) {
                    NSPasteboard.general.clearContents()
                    NSPasteboard.general.setString(worktree.path, forType: .string)
                }
                .keyboardShortcut("c", modifiers: [.command, .shift])
            }

            if !worktree.isMain {
                Section {
                    Button(labels.push) {
                        root.store.gitActions.onPush(worktreePath: worktree.path)
                    }
                    .keyboardShortcut("p", modifiers: [.command, .shift])

                    Button(labels.pull) {
                        root.store.gitActions.onPull(worktreePath: worktree.path)
                    }
                    .keyboardShortcut("p", modifiers: [.command, .option])
                }

                Section {
                    if worktree.status?.prStatus != nil {
                        Button(labels.viewPullRequest) {
                            root.store.gitActions.onOpenPullRequest(worktreePath: worktree.path)
                        }
                    } else {
                        Button(labels.createPullRequest) {
                            root.presentSheet(.createPR(worktreePath: worktree.path))
                        }
                    }
                }

                Section {
                    if worktree.isLocked {
                        Button(labels.unlock) {
                            root.store.gitActions.onUnlockWorktree(worktreePath: worktree.path)
                        }
                    } else {
                        Button(labels.lock) {
                            root.store.gitActions.onLockWorktree(worktreePath: worktree.path)
                        }
                    }
                }

                Section {
                    Button(labels.finishWorktree) {
                        root.presentSheet(.completeWorktree(worktreePath: worktree.path))
                    }

                    Button(labels.removeWorktree, role: .destructive) {
                        root.store.gitActions.onRemoveWorktree(
                            worktreePath: worktree.path,
                            force: true,
                            deleteBranch: true,
                        )
                    }
                }
            }

            Section {
                Button(labels.refreshStatus) {
                    root.store.worktrees.onRefreshWorktreeStatus(worktreePath: worktree.path)
                }
                .keyboardShortcut("r", modifiers: .command)
            }
        }
    }
}
