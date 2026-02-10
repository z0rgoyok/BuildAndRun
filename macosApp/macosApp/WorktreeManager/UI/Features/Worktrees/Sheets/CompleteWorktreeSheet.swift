import Shared
import SwiftUI

struct CompleteWorktreeSheet: View {
    @EnvironmentObject var root: KmpRoot
    @Environment(\.dismiss) var dismiss

    let worktreePath: String

    @State private var selectedAction: CompleteAction = .prMerged
    @State private var deleteLocalBranch = true
    @State private var deleteRemoteBranch = false
    @State private var pullTargetFirst = true
    @State private var forceDelete = false
    @State private var hasRemoteBranch = false
    @State private var isPreparing = false
    @State private var isSubmitting = false

    private var worktree: WorktreeItem? {
        root.selectedRepository?.worktrees.first { $0.path == worktreePath }
    }

    private var status: WorktreeStatus? {
        worktree?.status
    }

    private var hasMergedPR: Bool {
        status?.prStatus?.state == PRState.merged
    }

    private var hasOpenPR: Bool {
        guard let prStatus = status?.prStatus else { return false }
        return prStatus.state == PRState.open
    }

    private var isDirty: Bool {
        status?.isDirty == true
    }

    private var hasUnpushed: Bool {
        status?.hasUnpushedCommits == true
    }

    private var targetBranch: String {
        worktree?.baseBranch?.takeIfNotBlank
            ?? root.store.settings.preferredBaseBranch()?.takeIfNotBlank
            ?? "main"
    }

    private var canDeleteBranch: Bool {
        guard let worktree else { return false }
        return !worktree.branch.isEmpty && worktree.branch != "detached HEAD"
    }

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: selectedAction.icon)
                .font(.system(size: 48))
                .foregroundStyle(selectedAction.color)

            Text("Complete Worktree")
                .font(.headline)

            Text("Clean up '\(worktree?.name ?? "")'")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            if isDirty || hasUnpushed || hasOpenPR {
                VStack(alignment: .leading, spacing: 6) {
                    if isDirty {
                        Label("Uncommitted changes", systemImage: "exclamationmark.triangle.fill")
                            .foregroundStyle(.orange)
                            .font(.caption)
                    }
                    if hasUnpushed {
                        Label("Unpushed commits (\(status?.ahead ?? 0))", systemImage: "exclamationmark.triangle.fill")
                            .foregroundStyle(.orange)
                            .font(.caption)
                    }
                    if hasOpenPR {
                        Label("PR is still open", systemImage: "exclamationmark.triangle.fill")
                            .foregroundStyle(.orange)
                            .font(.caption)
                    }
                }
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.orange.opacity(0.1))
                .cornerRadius(8)
            }

            VStack(alignment: .leading, spacing: 8) {
                Text("What happened with this work?")
                    .font(.subheadline)
                    .fontWeight(.medium)

                ForEach(CompleteAction.allCases, id: \.self) { action in
                    HStack(spacing: 10) {
                        Image(systemName: selectedAction == action ? "largecircle.fill.circle" : "circle")
                            .foregroundStyle(selectedAction == action ? action.color : .secondary)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(action.rawValue)
                                .fontWeight(selectedAction == action ? .medium : .regular)
                            Text(action.description)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        selectedAction = action
                    }
                }
            }
            .padding()
            .background(Color(nsColor: .controlBackgroundColor))
            .cornerRadius(8)

            VStack(alignment: .leading, spacing: 12) {
                Text("Cleanup options")
                    .font(.subheadline)
                    .fontWeight(.medium)

                if canDeleteBranch {
                    Toggle("Delete local branch", isOn: $deleteLocalBranch)
                    if hasRemoteBranch {
                        Toggle("Delete remote branch", isOn: $deleteRemoteBranch)
                    }
                }

                if selectedAction != .discard {
                    Toggle("Update \(targetBranch) from remote first", isOn: $pullTargetFirst)
                }

                if isDirty {
                    Toggle("Force delete", isOn: $forceDelete)
                        .foregroundStyle(.red)
                }
            }
            .padding()
            .background(Color(nsColor: .controlBackgroundColor))
            .cornerRadius(8)

            HStack(spacing: 12) {
                Button("Cancel") {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Button(selectedAction == .discard ? "Delete" : "Complete") {
                    complete()
                }
                .keyboardShortcut(.defaultAction)
                .buttonStyle(.borderedProminent)
                .tint(selectedAction.color)
                .disabled((isDirty && !forceDelete) || isPreparing || isSubmitting || worktree == nil)
            }
        }
        .padding(24)
        .frame(width: 430)
        .overlay {
            if isPreparing {
                BlockingProgressOverlay(title: "Preparing…")
            } else if isSubmitting {
                BlockingProgressOverlay(title: "Completing worktree…")
            }
        }
        .task {
            await prepare()
        }
    }

    private func complete() {
        guard let worktree else { return }
        isSubmitting = true
        root.store.gitActions.onCompleteWorktree(
            worktreePath: worktree.path,
            targetBranch: targetBranch,
            mergeIntoTarget: selectedAction == .mergeLocally,
            pullTargetFirst: pullTargetFirst && selectedAction != .discard,
            deleteLocalBranch: deleteLocalBranch && canDeleteBranch,
            deleteRemoteBranch: deleteRemoteBranch && hasRemoteBranch,
            force: forceDelete,
        )
        isSubmitting = false
        dismiss()
    }

    private func prepare() async {
        guard !isPreparing else { return }
        isPreparing = true
        defer { isPreparing = false }

        if hasMergedPR || hasOpenPR {
            selectedAction = .prMerged
        }

        root.store.gitActions.onLoadHasRemoteBranch(worktreePath: worktreePath)
        hasRemoteBranch =
            root.worktreesState.remoteBranches
                .first(where: { $0.worktreePath == worktreePath })?
                .hasRemote
                ?? false
    }
}

private extension String {
    var takeIfNotBlank: String? {
        let value = trimmingCharacters(in: .whitespacesAndNewlines)
        return value.isEmpty ? nil : value
    }
}
