import Shared
import SwiftUI

struct WorktreeStatusRow: View {
    @EnvironmentObject var root: KmpRoot
    let status: WorktreeStatus?

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        HStack(spacing: DS.Spacing.md) {
            if let status = status {
                let hasAnySignal = status.isDirty || status.ahead > 0 || status.behind > 0 || status.prStatus != nil || !status.hasRemote

                if !hasAnySignal {
                    Label(labels.clean, systemImage: "checkmark.circle")
                        .foregroundStyle(.secondary)
                }

                if status.isDirty {
                    Label(labels.modified, systemImage: "pencil.circle.fill")
                        .foregroundStyle(.orange)
                }

                if status.ahead > 0 {
                    Label(root.store.texts.resolveStatusToPush(commits: "\(status.ahead)"), systemImage: "arrow.up.circle.fill")
                        .foregroundStyle(.blue)
                }

                if status.behind > 0 {
                    Label(root.store.texts.resolveStatusBehind(commits: "\(status.behind)"), systemImage: "arrow.down.circle.fill")
                        .foregroundStyle(.purple)
                }

                if let pr = status.prStatus {
                    WorktreePRBadge(pr: pr)
                } else if !status.hasRemote {
                    Label(labels.notPushed, systemImage: "icloud.slash")
                        .foregroundStyle(.secondary)
                }
            } else {
                ProgressView()
                    .scaleEffect(0.6)
                Text(labels.loadingStatus)
                    .foregroundStyle(.secondary)
            }
        }
        .font(.caption)
    }
}
