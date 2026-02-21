import Shared
import SwiftUI

struct WorktreeStatusIndicators: View {
    @EnvironmentObject var root: KmpRoot
    let status: WorktreeStatus
    let isSelected: Bool

    private var labels: KanbanLabels { root.store.kanbanLabels }

    private var selectedColor: Color {
        DS.Colors.sidebarSelectedTextSecondary
    }

    var body: some View {
        HStack(spacing: DS.Spacing.xxs) {
            if status.isDirty {
                Circle()
                    .fill(Color.orange)
                    .frame(width: 6, height: 6)
                    .help(labels.uncommittedChanges)
            }

            if status.ahead > 0 {
                HStack(spacing: 1) {
                    Image(systemName: "arrow.up")
                        .font(.system(size: 8, weight: .bold))
                    Text("\(status.ahead)")
                        .font(.system(size: 9))
                }
                .foregroundStyle(isSelected ? selectedColor : .blue)
                .help(root.store.texts.resolveStatusToPush(commits: "\(status.ahead)"))
            }

            if status.behind > 0 {
                HStack(spacing: 1) {
                    Image(systemName: "arrow.down")
                        .font(.system(size: 8, weight: .bold))
                    Text("\(status.behind)")
                        .font(.system(size: 9))
                }
                .foregroundStyle(isSelected ? selectedColor : .purple)
                .help(root.store.texts.resolveStatusBehind(commits: "\(status.behind)"))
            }

            if let pr = status.prStatus {
                Image(systemName: pr.state === PRState.merged ? "checkmark.circle.fill" : "arrow.triangle.pull")
                    .font(.system(size: 9))
                    .foregroundStyle(
                        isSelected ? selectedColor :
                        pr.state === PRState.merged ? .purple : .green
                    )
                    .help(pr.state === PRState.merged ? labels.prMerged : "\(labels.prShort) #\(pr.number)")
            }
        }
    }
}
