import Shared
import SwiftUI

struct KanbanColumn: View {
    let title: String
    let columnId: KanbanColumnType
    let tasks: [MacOSAppStore.KanbanTaskItem]
    @Binding var draggedTaskId: String?
    let onAddTask: () -> Void
    let onMoveTask: (String, KanbanColumnType) -> Void
    let onDeleteTask: (String) -> Void

    @State private var isTargeted = false

    var body: some View {
        VStack(spacing: 0) {
            ColumnHeader(
                columnId: columnId,
                title: title,
                count: tasks.count,
                onAdd: onAddTask
            )

            ScrollView {
                LazyVStack(spacing: DS.Spacing.sm) {
                    ForEach(tasks, id: \.id) { task in
                        KanbanCard(
                            task: task,
                            isDragging: draggedTaskId == task.id,
                            onDelete: { onDeleteTask(task.id) }
                        )
                        .onDrag {
                            draggedTaskId = task.id
                            return NSItemProvider(object: task.id as NSString)
                        }
                    }

                    if tasks.isEmpty {
                        EmptyColumnPlaceholder(onAdd: onAddTask)
                    } else {
                        Button(action: onAddTask) {
                            HStack(spacing: DS.Spacing.xs) {
                                Image(systemName: "plus")
                                    .font(.system(size: 11, weight: .medium))
                                Text("Add task")
                                    .font(.system(size: 12))
                            }
                            .foregroundStyle(DS.Colors.textSecondary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, DS.Spacing.sm)
                        }
                        .buttonStyle(.plain)
                        .background(
                            RoundedRectangle(cornerRadius: DS.Radius.md)
                                .stroke(style: StrokeStyle(lineWidth: 1, dash: [5, 3]))
                                .foregroundStyle(DS.Colors.borderSubtle)
                        )
                    }
                }
                .padding(DS.Spacing.sm)
            }
        }
        .frame(minWidth: DS.Sizes.columnMinWidth, maxWidth: .infinity)
        .columnStyle()
        .dropTargetStyle(isTargeted: isTargeted && draggedTaskId != nil)
        .onDrop(
            of: [.text],
            delegate: ColumnDropDelegate(
                columnId: columnId,
                draggedTaskId: $draggedTaskId,
                isTargeted: $isTargeted,
                onMove: onMoveTask
            )
        )
    }
}
