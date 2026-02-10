import Shared
import SwiftUI

struct KanbanBoard: View {
    @EnvironmentObject var root: KmpRoot
    let selection: SidebarSelection?
    @State private var draggedTaskId: String?
    @State private var editorMode: TaskEditorMode?

    var body: some View {
        VStack(spacing: 0) {
            if selection != nil {
                detailHeader

                if let mode = editorMode {
                    TaskEditorView(mode: mode) {
                        withAnimation(DS.Animation.standard) {
                            editorMode = nil
                        }
                    }
                } else if selection?.worktreePath == nil {
                    KanbanSectionHeader {
                        withAnimation(DS.Animation.standard) {
                            editorMode = .creating(columnId: KanbanColumnType.todo)
                        }
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
                                            withAnimation(DS.Animation.standard) {
                                                editorMode = .creating(columnId: column.id)
                                            }
                                        },
                                        onMoveTask: { taskId, newColumnId in
                                            root.store.onMoveTask(taskId: taskId, column: newColumnId)
                                        },
                                        onEditTask: { task in
                                            withAnimation(DS.Animation.standard) {
                                                editorMode = .editing(taskId: task.id)
                                            }
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
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .onChange(of: selection) { _ in
            editorMode = nil
        }
        .onChange(of: root.state.kanbanTasks.map(\.id)) { taskIds in
            guard case let .editing(taskId) = editorMode,
                  !taskIds.contains(taskId) else { return }
            editorMode = nil
        }
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
}

#Preview {
    KanbanBoard(selection: nil)
        .environmentObject(KmpRoot())
        .frame(width: 1000, height: 600)
}
