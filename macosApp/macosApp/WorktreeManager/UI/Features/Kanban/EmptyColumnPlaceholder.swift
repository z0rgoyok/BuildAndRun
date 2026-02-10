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

            Button {
                onAdd()
            } label: {
                Label(labels.addTask, systemImage: "plus")
                    .font(.system(size: 12, weight: .medium))
            }
            .buttonStyle(.bordered)
            .controlSize(.small)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, DS.Spacing.xxl)
    }
}
