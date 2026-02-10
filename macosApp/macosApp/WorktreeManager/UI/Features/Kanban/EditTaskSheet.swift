import Shared
import SwiftUI

struct EditTaskSheet: View {
    @EnvironmentObject var root: KmpRoot
    let taskId: String
    let columnId: KanbanColumnType
    let onSave: (String, String, String?) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var title: String
    @State private var description: String
    @FocusState private var isTitleFocused: Bool

    init(
        task: AppStore.KanbanTaskItem,
        onSave: @escaping (String, String, String?) -> Void,
    ) {
        taskId = task.id
        columnId = task.columnId
        self.onSave = onSave
        _title = State(initialValue: task.title)
        _description = State(initialValue: task.description_ ?? "")
    }

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: DS.Spacing.lg) {
            HStack(spacing: DS.Spacing.sm) {
                Image(systemName: columnId.icon)
                    .foregroundStyle(columnColor)
                Text(labels.editTaskAction)
                    .font(.headline)
            }

            VStack(alignment: .leading, spacing: DS.Spacing.sm) {
                TextField(labels.taskTitle, text: $title)
                    .textFieldStyle(.roundedBorder)
                    .focused($isTitleFocused)

                VStack(alignment: .leading, spacing: DS.Spacing.xxs) {
                    Text(labels.descriptionOptional)
                        .font(.caption)
                        .foregroundStyle(DS.Colors.textSecondary)

                    TaskMarkdownEditor(
                        text: $description,
                        labels: labels
                    )
                }
            }

            HStack {
                Button(labels.createCancel) {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Spacer()

                Button(labels.saveAction) {
                    onSave(taskId, title, description.isEmpty ? nil : description)
                    dismiss()
                }
                .buttonStyle(.borderedProminent)
                .keyboardShortcut(.return, modifiers: [.command])
                .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
            }
        }
        .padding(DS.Spacing.xl)
        .frame(width: 560)
        .onAppear {
            isTitleFocused = true
        }
    }

    private var columnColor: Color {
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
