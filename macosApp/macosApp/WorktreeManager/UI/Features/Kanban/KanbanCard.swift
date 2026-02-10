import Shared
import SwiftUI

struct KanbanCard: View {
    @EnvironmentObject var root: KmpRoot
    let task: AppStore.KanbanTaskItem
    let isDragging: Bool
    let onMoveTask: (String, KanbanColumnType) -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void

    @State private var isHovered = false
    @State private var showActions = false

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(alignment: .leading, spacing: DS.Spacing.sm) {
            HStack(alignment: .top, spacing: DS.Spacing.xs) {
                Text(task.title)
                    .font(DS.Typography.cardTitle)
                    .foregroundStyle(DS.Colors.textPrimary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                Spacer(minLength: DS.Spacing.xxs)

                if isHovered || showActions {
                    Menu {
                        Button(labels.editTaskAction) {
                            onEdit()
                        }
                        Divider()
                        Button(labels.deleteAction, role: .destructive) {
                            onDelete()
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.system(size: 12))
                            .foregroundStyle(DS.Colors.textSecondary)
                            .frame(width: 20, height: 20)
                            .background(showActions ? DS.Colors.surfaceSecondary : Color.clear)
                            .cornerRadius(DS.Radius.xs)
                    }
                    .buttonStyle(.plain)
                    .onTapGesture {
                        showActions.toggle()
                    }
                    .transition(.opacity)
                }
            }

            if let description = task.description_, !description.isEmpty {
                if let rendered = try? AttributedString(markdown: description) {
                    Text(rendered)
                        .font(DS.Typography.cardSubtitle)
                        .foregroundStyle(DS.Colors.textSecondary)
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                } else {
                    Text(description)
                        .font(DS.Typography.cardSubtitle)
                        .foregroundStyle(DS.Colors.textSecondary)
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                }
            }
        }
        .padding(DS.Spacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle(isHovered: isHovered, isDragging: isDragging)
        .opacity(isDragging ? 0.5 : 1)
        .scaleEffect(isDragging ? 1.02 : 1)
        .animation(DS.Animation.quick, value: isDragging)
        .animation(DS.Animation.quick, value: isHovered)
        .onHover { hovering in
            withAnimation(DS.Animation.quick) {
                isHovered = hovering
            }
        }
        .onTapGesture(count: 2) {
            onEdit()
        }
        .contextMenu {
            moveToMenu
            Divider()
            Button(labels.editTaskAction) {
                onEdit()
            }
            Divider()
            Button(labels.deleteAction, role: .destructive) {
                onDelete()
            }
        }
    }

    @ViewBuilder
    private var moveToMenu: some View {
        Menu(labels.moveTo) {
            ForEach(otherColumns, id: \.id) { column in
                Button(column.title) {
                    onMoveTask(task.id, column.id)
                }
            }
        }
    }

    private var otherColumns: [(id: KanbanColumnType, title: String)] {
        allColumns.filter { $0.id !== task.columnId }
    }

    private var allColumns: [(id: KanbanColumnType, title: String)] {
        [
            (id: KanbanColumnType.todo, title: KanbanColumnType.todo.displayName),
            (id: KanbanColumnType.inProgress, title: KanbanColumnType.inProgress.displayName),
            (id: KanbanColumnType.review, title: KanbanColumnType.review.displayName),
            (id: KanbanColumnType.done, title: KanbanColumnType.done.displayName),
        ]
    }
}
