import Shared
import SwiftUI

struct KanbanBoard: View {
    @EnvironmentObject var root: KmpRoot
    let selection: SidebarSelection?
    @State private var draggedTaskId: String?
    @State private var showAddTask = false
    @State private var addTaskColumnId: KanbanColumnType = KanbanColumnType.todo
    @State private var editingTaskId: String?

    var body: some View {
        VStack(spacing: 0) {
            if selection != nil {
                detailHeader

                if selection?.worktreePath == nil {
                    KanbanSectionHeader {
                        addTaskColumnId = KanbanColumnType.todo
                        showAddTask = true
                    }

                    GeometryReader { geometry in
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(alignment: .top, spacing: DS.Spacing.md) {
                                ForEach(columns, id: \.id) { column in
                                    KanbanColumn(
                                        title: column.title,
                                        columnId: column.id,
                                        tasks: tasks(for: column.id),
                                        draggedTaskId: $draggedTaskId,
                                        onAddTask: {
                                            addTaskColumnId = column.id
                                            showAddTask = true
                                        },
                                        onMoveTask: { taskId, newColumnId in
                                            root.store.onMoveTask(taskId: taskId, column: newColumnId)
                                        },
                                        onEditTask: { task in
                                            editingTaskId = task.id
                                        },
                                        onDeleteTask: { taskId in
                                            root.store.onDeleteTask(taskId: taskId)
                                        }
                                    )
                                }
                            }
                            .padding(DS.Spacing.lg)
                            .frame(minWidth: geometry.size.width, alignment: .topLeading)
                        }
                        .background(DS.Colors.surfaceTertiary)
                    }
                }
            } else {
                KanbanEmptyState()
            }
        }
        .sheet(isPresented: $showAddTask) {
            AddTaskSheet(columnId: addTaskColumnId) { title, description in
                root.store.onAddTask(title: title, description: description, column: addTaskColumnId)
            }
            .environmentObject(root)
        }
        .sheet(
            isPresented: Binding(
                get: { editingTask != nil },
                set: { newValue in
                    if !newValue {
                        editingTaskId = nil
                    }
                }
            )
        ) {
            if let task = editingTask {
                EditTaskSheet(task: task) { taskId, title, description in
                    root.store.onUpdateTask(
                        taskId: taskId,
                        title: title,
                        description: description
                    )
                }
                .environmentObject(root)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }

    @ViewBuilder
    private var detailHeader: some View {
        if let worktree = root.selectedWorktree {
            WorktreeDetailHeader(worktree: worktree)
                .environmentObject(root)
        } else if let repository = root.selectedRepository {
            RepositoryDetailHeader(repository: repository)
                .environmentObject(root)
        }
    }

    private var columns: [(id: KanbanColumnType, title: String)] {
        [
            (id: KanbanColumnType.todo, title: KanbanColumnType.todo.displayName),
            (id: KanbanColumnType.inProgress, title: KanbanColumnType.inProgress.displayName),
            (id: KanbanColumnType.review, title: KanbanColumnType.review.displayName),
            (id: KanbanColumnType.done, title: KanbanColumnType.done.displayName),
        ]
    }

    private func tasks(for columnId: KanbanColumnType) -> [AppStore.KanbanTaskItem] {
        root.state.kanbanTasks
            .filter { $0.columnId === columnId }
            .sorted { $0.order < $1.order }
    }

    private var editingTask: AppStore.KanbanTaskItem? {
        guard let editingTaskId else {
            return nil
        }
        return root.state.kanbanTasks.first { $0.id == editingTaskId }
    }
}

#Preview {
    KanbanBoard(selection: nil)
        .environmentObject(KmpRoot())
        .frame(width: 1000, height: 600)
}
