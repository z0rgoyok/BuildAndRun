import Shared
import SwiftUI

struct AddTaskSheet: View {
    let columnId: KanbanColumnType
    let onAdd: (String, String?) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var description = ""
    @FocusState private var isTitleFocused: Bool

    var body: some View {
        VStack(spacing: DS.Spacing.lg) {
            HStack {
                Image(systemName: iconName)
                    .foregroundStyle(columnColor)
                Text("New Task in \(columnTitle)")
                    .font(.headline)
            }

            VStack(alignment: .leading, spacing: DS.Spacing.sm) {
                TextField("Task title", text: $title)
                    .textFieldStyle(.roundedBorder)
                    .focused($isTitleFocused)

                VStack(alignment: .leading, spacing: DS.Spacing.xxs) {
                    Text("Description (optional)")
                        .font(.caption)
                        .foregroundStyle(DS.Colors.textSecondary)

                    TextEditor(text: $description)
                        .font(.body)
                        .frame(height: 80)
                        .overlay(
                            RoundedRectangle(cornerRadius: DS.Radius.sm)
                                .stroke(DS.Colors.border, lineWidth: 1)
                        )
                }
            }

            HStack {
                Button("Cancel") {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Spacer()

                Button("Add Task") {
                    onAdd(title, description.isEmpty ? nil : description)
                    dismiss()
                }
                .buttonStyle(.borderedProminent)
                .keyboardShortcut(.defaultAction)
                .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
            }
        }
        .padding(DS.Spacing.xl)
        .frame(width: 360)
        .onAppear {
            isTitleFocused = true
        }
    }

    private var iconName: String {
        columnId.icon
    }

    private var columnTitle: String {
        columnId.displayName
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
