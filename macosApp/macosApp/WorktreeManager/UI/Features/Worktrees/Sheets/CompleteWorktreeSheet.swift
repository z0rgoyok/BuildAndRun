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
    @State private var isSubmissionRequested = false

    private var labels: KanbanLabels { root.store.kanbanLabels }

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
        return !worktree.branch.isEmpty && !worktree.isDetachedHead
    }

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: selectedAction.icon)
                .font(.system(size: 48))
                .foregroundStyle(selectedAction.color)

            Text(labels.completeWorktreeTitle)
                .font(.headline)

            Text(root.store.texts.resolveCompleteWorktreeCleanup(name: worktree?.name ?? ""))
                .font(.subheadline)
                .foregroundStyle(.secondary)

            if isDirty || hasUnpushed || hasOpenPR {
                VStack(alignment: .leading, spacing: 6) {
                    if isDirty {
                        Label(labels.completeWorktreeWarningUncommittedChanges, systemImage: "exclamationmark.triangle.fill")
                            .foregroundStyle(.orange)
                            .font(.caption)
                    }
                    if hasUnpushed {
                        Label(
                            root.store.texts.resolveCompleteWorktreeUnpushedCommits(commits: "\(status?.ahead ?? 0)"),
                            systemImage: "exclamationmark.triangle.fill"
                        )
                            .foregroundStyle(.orange)
                            .font(.caption)
                    }
                    if hasOpenPR {
                        Label(labels.completeWorktreeWarningOpenPr, systemImage: "exclamationmark.triangle.fill")
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
                Text(labels.completeWorktreeQuestion)
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
                Text(labels.completeWorktreeCleanupOptions)
                    .font(.subheadline)
                    .fontWeight(.medium)

                if canDeleteBranch {
                    Toggle(labels.completeWorktreeDeleteLocalBranch, isOn: $deleteLocalBranch)
                    if hasRemoteBranch {
                        Toggle(labels.completeWorktreeDeleteRemoteBranch, isOn: $deleteRemoteBranch)
                    }
                }

                if selectedAction != .discard {
                    Toggle(root.store.texts.resolveCompleteWorktreeUpdateTargetFromRemoteFirst(branch: targetBranch), isOn: $pullTargetFirst)
                }

                if isDirty {
                    Toggle(labels.completeWorktreeForceDelete, isOn: $forceDelete)
                        .foregroundStyle(.red)
                }
            }
            .padding()
            .background(Color(nsColor: .controlBackgroundColor))
            .cornerRadius(8)

            HStack(spacing: 12) {
                Button(labels.completeWorktreeCancel) {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Button(selectedAction == .discard ? labels.completeWorktreeDelete : labels.completeWorktreeComplete) {
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
                BlockingProgressOverlay(title: labels.completeWorktreePreparing)
            } else if isSubmitting {
                BlockingProgressOverlay(title: labels.completeWorktreeSubmitting)
            }
        }
        .task {
            await prepare()
        }
        .onChange(of: root.messagesState.success?.message) { _, next in
            guard isSubmissionRequested, next != nil else { return }
            isSubmitting = false
            isSubmissionRequested = false
            dismiss()
        }
        .onChange(of: root.messagesState.error?.message) { _, next in
            guard isSubmissionRequested, next != nil else { return }
            isSubmitting = false
            isSubmissionRequested = false
        }
    }

    private func complete() {
        guard let worktree else { return }
        guard !isSubmitting else { return }
        isSubmitting = true
        isSubmissionRequested = true
        root.store.gitActions.onCompleteWorktree(
            worktreePath: worktree.path,
            targetBranch: targetBranch,
            mergeIntoTarget: selectedAction == .mergeLocally,
            pullTargetFirst: pullTargetFirst && selectedAction != .discard,
            deleteLocalBranch: deleteLocalBranch && canDeleteBranch,
            deleteRemoteBranch: deleteRemoteBranch && hasRemoteBranch,
            force: forceDelete,
        )
    }

    private func prepare() async {
        guard !isPreparing else { return }
        isPreparing = true

        if hasMergedPR || hasOpenPR {
            selectedAction = .prMerged
        }

        root.store.gitActions.onLoadHasRemoteBranch(worktreePath: worktreePath)
        for _ in 0 ..< 100 {
            if let hasRemote =
                root.worktreesState.remoteBranches
                    .first(where: { $0.worktreePath == worktreePath })?
                    .hasRemote
            {
                hasRemoteBranch = hasRemote
                break
            }
            if root.messagesState.error != nil {
                break
            }
            try? await Task.sleep(for: .milliseconds(50))
        }
        isPreparing = false
    }
}

private extension String {
    var takeIfNotBlank: String? {
        let value = trimmingCharacters(in: .whitespacesAndNewlines)
        return value.isEmpty ? nil : value
    }
}
