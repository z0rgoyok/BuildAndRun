import Shared
import SwiftUI

struct TaskEditorView: View {
    @EnvironmentObject var root: KmpRoot
    let mode: TaskEditorMode
    let onClose: () -> Void

    @State private var title: String = ""
    @State private var description: String = ""
    @State private var columnId: KanbanColumnType = KanbanColumnType.todo
    @FocusState private var isTitleFocused: Bool

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: 0) {
            TaskEditorHeader(
                mode: mode,
                columnId: columnId,
                isSaveDisabled: title.trimmingCharacters(in: .whitespaces).isEmpty,
                onBack: onClose,
                onChangeColumn: { newColumn in columnId = newColumn },
                onDelete: deleteTask,
                onSave: saveTask
            )

            Divider()

            GeometryReader { geometry in
                HStack(spacing: 0) {
                    mainContent
                        .frame(width: geometry.size.width * 2 / 3)

                    Divider()

                    TaskEditorSidebar()
                        .frame(width: geometry.size.width / 3)
                }
            }
        }
        .background(DS.Colors.surfaceTertiary)
        .onAppear(perform: loadInitialData)
        .onExitCommand(perform: onClose)
    }

    private var mainContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DS.Spacing.lg) {
                TextField(labels.taskTitle, text: $title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .textFieldStyle(.plain)
                    .focused($isTitleFocused)

                VStack(alignment: .leading, spacing: DS.Spacing.xxs) {
                    Text(labels.descriptionOptional)
                        .font(.caption)
                        .foregroundStyle(DS.Colors.textSecondary)
                    PlainTextEditorView(text: $description)
                        .frame(minHeight: 300)
                        .overlay(
                            RoundedRectangle(cornerRadius: DS.Radius.sm)
                                .stroke(DS.Colors.border, lineWidth: 1)
                        )
                }

                Text("\(description.count)")
                    .font(.caption)
                    .foregroundStyle(DS.Colors.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(DS.Spacing.xl)
        }
    }

    private func loadInitialData() {
        switch mode {
        case let .creating(col):
            columnId = col
            title = ""
            description = ""
            isTitleFocused = true
        case let .editing(taskId):
            if let task = root.state.kanbanTasks.first(where: { $0.id == taskId }) {
                title = task.title
                description = task.description_ ?? ""
                columnId = task.columnId
            }
        }
    }

    private func saveTask() {
        let trimmedTitle = title.trimmingCharacters(in: .whitespaces)
        guard !trimmedTitle.isEmpty else { return }
        let desc = description.isEmpty ? nil : description

        switch mode {
        case .creating:
            root.store.kanban.onAddTask(title: trimmedTitle, description: desc, column: columnId)
        case let .editing(taskId):
            root.store.kanban.onUpdateTask(taskId: taskId, title: trimmedTitle, description: desc)
            if let task = root.state.kanbanTasks.first(where: { $0.id == taskId }),
               task.columnId !== columnId
            {
                root.store.kanban.onMoveTask(taskId: taskId, column: columnId)
            }
        }
        onClose()
    }

    private func deleteTask() {
        if case let .editing(taskId) = mode {
            root.store.kanban.onDeleteTask(taskId: taskId)
            onClose()
        }
    }
}
