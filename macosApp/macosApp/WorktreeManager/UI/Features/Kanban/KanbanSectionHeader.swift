import Shared
import SwiftUI

struct KanbanSectionHeader: View {
    @EnvironmentObject var root: KmpRoot
    let onAddTask: () -> Void

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        HStack {
            HStack(spacing: DS.Spacing.sm) {
                Image(systemName: "square.3.layers.3d")
                    .font(.system(size: 14))
                    .foregroundStyle(DS.Colors.textSecondary)

                Text(labels.tasks)
                    .font(DS.Typography.sectionHeader)
                    .foregroundStyle(DS.Colors.textSecondary)
                    .textCase(.uppercase)
            }

            Spacer()

            Button {
                onAddTask()
            } label: {
                Label(labels.addTask, systemImage: "plus")
            }
            .buttonStyle(.bordered)
            .controlSize(.small)
        }
        .padding(.horizontal, DS.Spacing.lg)
        .padding(.vertical, DS.Spacing.sm)
        .background(DS.Colors.surfaceTertiary)
    }
}
