import Shared
import SwiftUI

struct BranchConflictSheet: View {
    @EnvironmentObject var root: KmpRoot
    let branchName: String
    let worktreeName: String
    let onUseExisting: () -> Void
    let onRecreate: () -> Void

    @Environment(\.dismiss) var dismiss

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.largeTitle)
                .foregroundStyle(.orange)

            Text(labels.branchConflictTitle)
                .font(.headline)

            Text(root.store.texts.resolveBranchConflictMessage(branch: branchName))
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            VStack(spacing: 10) {
                Button {
                    onUseExisting()
                } label: {
                    HStack {
                        Image(systemName: "arrow.right.circle")
                        VStack(alignment: .leading) {
                            Text(labels.branchConflictUseExisting)
                                .fontWeight(.medium)
                            Text(root.store.texts.resolveBranchConflictUseExistingDetail(branch: branchName))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                    }
                    .padding(10)
                    .frame(maxWidth: .infinity)
                    .background(Color(nsColor: .controlBackgroundColor))
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)

                Button {
                    onRecreate()
                } label: {
                    HStack {
                        Image(systemName: "arrow.counterclockwise")
                        VStack(alignment: .leading) {
                            Text(labels.branchConflictRecreate)
                                .fontWeight(.medium)
                            Text(labels.branchConflictRecreateDetail)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                    }
                    .padding(10)
                    .frame(maxWidth: .infinity)
                    .background(Color.red.opacity(0.1))
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }

            Button(labels.createCancel) {
                dismiss()
            }
            .keyboardShortcut(.cancelAction)
        }
        .padding(24)
        .frame(width: 360)
    }
}
