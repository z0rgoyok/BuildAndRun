import Shared
import SwiftUI

struct RepositoryStatusPlaceholderRow: View {
    @EnvironmentObject var root: KmpRoot

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        HStack(spacing: DS.Spacing.md) {
            Label(labels.selectWorktreeHint, systemImage: "info.circle")
                .foregroundStyle(DS.Colors.textTertiary)
        }
        .font(.caption)
    }
}
