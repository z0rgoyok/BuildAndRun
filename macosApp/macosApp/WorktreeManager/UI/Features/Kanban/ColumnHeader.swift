import Shared
import SwiftUI

struct ColumnHeader: View {
    let columnId: KanbanColumnType
    let title: String
    let count: Int
    let onAdd: () -> Void

    @State private var isHovered = false

    var body: some View {
        HStack(spacing: DS.Spacing.sm) {
            Image(systemName: iconName)
                .font(.system(size: 14))
                .foregroundStyle(statusColor)

            Text(title)
                .font(DS.Typography.columnHeader)
                .foregroundStyle(DS.Colors.textPrimary)

            Text("\(count)")
                .font(DS.Typography.columnCount)
                .foregroundStyle(DS.Colors.textSecondary)
                .padding(.horizontal, DS.Spacing.xs)
                .padding(.vertical, DS.Spacing.xxxs)
                .background(DS.Colors.surfaceSecondary)
                .cornerRadius(DS.Radius.pill)

            Spacer()

            Button(action: onAdd) {
                Image(systemName: "plus")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(DS.Colors.textSecondary)
                    .frame(width: 24, height: 24)
                    .background(isHovered ? DS.Colors.surfaceSecondary : Color.clear)
                    .cornerRadius(DS.Radius.sm)
            }
            .buttonStyle(.plain)
            .onHover { isHovered = $0 }
        }
        .padding(.horizontal, DS.Spacing.md)
        .padding(.vertical, DS.Spacing.sm)
    }

    private var iconName: String {
        columnId.icon
    }

    private var statusColor: Color {
        if columnId === KanbanColumnType.todo {
            return DS.Colors.statusTodo
        }
        if columnId === KanbanColumnType.inProgress {
            return DS.Colors.statusInProgress
        }
        if columnId === KanbanColumnType.review {
            return DS.Colors.statusReview
        }
        if columnId === KanbanColumnType.done {
            return DS.Colors.statusDone
        }
        return DS.Colors.statusTodo
    }
}
