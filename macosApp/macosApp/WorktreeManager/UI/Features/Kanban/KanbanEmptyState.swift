import Shared
import SwiftUI

struct KanbanEmptyState: View {
    @EnvironmentObject var root: KmpRoot

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: DS.Spacing.lg) {
            Image(systemName: "square.3.layers.3d")
                .font(.system(size: 56))
                .foregroundStyle(DS.Colors.textQuaternary)

            VStack(spacing: DS.Spacing.sm) {
                Text(labels.selectProjectOrWorktree)
                    .font(.title3)
                    .fontWeight(.medium)

                Text(labels.chooseItemFromSidebar)
                    .font(.subheadline)
                    .foregroundStyle(DS.Colors.textSecondary)
                    .multilineTextAlignment(.center)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(DS.Colors.surfaceTertiary)
    }
}
