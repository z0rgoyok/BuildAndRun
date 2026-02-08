import Shared
import SwiftUI

struct KanbanCardCompact: View {
    let task: MacOSAppStore.KanbanTaskItem
    let isDragging: Bool

    @State private var isHovered = false

    var body: some View {
        HStack(spacing: DS.Spacing.sm) {
            Circle()
                .fill(statusColor)
                .frame(width: 8, height: 8)

            Text(task.title)
                .font(DS.Typography.cardTitle)
                .foregroundStyle(DS.Colors.textPrimary)
                .lineLimit(1)

            Spacer()

            if isHovered {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 10))
                    .foregroundStyle(DS.Colors.textQuaternary)
            }
        }
        .padding(.horizontal, DS.Spacing.md)
        .padding(.vertical, DS.Spacing.sm)
        .background(isHovered ? DS.Colors.cardBackgroundHover : DS.Colors.cardBackground)
        .cornerRadius(DS.Radius.sm)
        .opacity(isDragging ? 0.5 : 1)
        .onHover { isHovered = $0 }
    }

    private var statusColor: Color {
        if task.columnId === KanbanColumnType.todo {
            return DS.Colors.statusTodo
        }
        if task.columnId === KanbanColumnType.inProgress {
            return DS.Colors.statusInProgress
        }
        if task.columnId === KanbanColumnType.review {
            return DS.Colors.statusReview
        }
        if task.columnId === KanbanColumnType.done {
            return DS.Colors.statusDone
        }
        return DS.Colors.statusTodo
    }
}
