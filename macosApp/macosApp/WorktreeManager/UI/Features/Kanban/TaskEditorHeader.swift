import Shared
import SwiftUI

struct TaskEditorHeader: View {
    @EnvironmentObject var root: KmpRoot
    let mode: TaskEditorMode
    let columnId: KanbanColumnType
    let isSaveDisabled: Bool
    let onBack: () -> Void
    let onChangeColumn: (KanbanColumnType) -> Void
    let onDelete: () -> Void
    let onSave: () -> Void

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        HStack(spacing: DS.Spacing.md) {
            Button(action: onBack) {
                HStack(spacing: DS.Spacing.xs) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 12, weight: .medium))
                    Text(labels.backToBoard)
                        .font(.system(size: 13))
                }
            }
            .buttonStyle(.plain)
            .foregroundStyle(DS.Colors.textSecondary)

            Spacer()

            columnMenu

            if case .editing = mode {
                Button(role: .destructive, action: onDelete) {
                    Image(systemName: "trash")
                        .font(.system(size: 12))
                }
                .buttonStyle(.bordered)
                .controlSize(.small)
            }

            Button(action: onSave) {
                Text(saveButtonTitle)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.small)
            .disabled(isSaveDisabled)
            .keyboardShortcut(.return, modifiers: [.command])
        }
        .padding(.horizontal, DS.Spacing.lg)
        .padding(.vertical, DS.Spacing.sm)
    }

    private var saveButtonTitle: String {
        switch mode {
        case .creating:
            return labels.createTask
        case .editing:
            return labels.saveAction
        }
    }

    @ViewBuilder
    private var columnMenu: some View {
        Menu {
            ForEach(allColumns, id: \.title) { column in
                Button {
                    onChangeColumn(column.id)
                } label: {
                    HStack {
                        Image(systemName: column.id.icon)
                        Text(column.title)
                        if column.id === columnId {
                            Image(systemName: "checkmark")
                        }
                    }
                }
            }
        } label: {
            HStack(spacing: DS.Spacing.xs) {
                Circle()
                    .fill(statusColor)
                    .frame(width: 8, height: 8)
                Text(columnId.displayName)
                    .font(.system(size: 12, weight: .medium))
                Image(systemName: "chevron.down")
                    .font(.system(size: 9))
            }
            .padding(.horizontal, DS.Spacing.sm)
            .padding(.vertical, DS.Spacing.xxs)
            .background(DS.Colors.surfaceSecondary)
            .cornerRadius(DS.Radius.sm)
        }
        .buttonStyle(.plain)
    }

    private var statusColor: Color {
        if columnId === KanbanColumnType.todo { return DS.Colors.statusTodo }
        if columnId === KanbanColumnType.inProgress { return DS.Colors.statusInProgress }
        if columnId === KanbanColumnType.review { return DS.Colors.statusReview }
        if columnId === KanbanColumnType.done { return DS.Colors.statusDone }
        return DS.Colors.statusTodo
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
