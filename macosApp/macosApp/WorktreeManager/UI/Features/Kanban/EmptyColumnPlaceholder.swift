import Shared
import SwiftUI

struct EmptyColumnPlaceholder: View {
    @EnvironmentObject var root: KmpRoot
    let onAdd: () -> Void

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: DS.Spacing.md) {
            Image(systemName: "tray")
                .font(.system(size: 24))
                .foregroundStyle(DS.Colors.textQuaternary)

            Text(labels.noTasks)
                .font(.subheadline)
                .foregroundStyle(DS.Colors.textTertiary)

            DashedAddButton(title: labels.addTask, action: onAdd)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, DS.Spacing.xxl)
    }
}
