import Shared
import SwiftUI

struct KanbanCard: View {
    let task: MacOSAppStore.KanbanTaskItem
    let isDragging: Bool
    let onDelete: () -> Void

    @State private var isHovered = false
    @State private var showActions = false

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
                        Button("Delete", role: .destructive) {
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

            let description = task.description
            if !description.isEmpty {
                Text(description)
                    .font(DS.Typography.cardSubtitle)
                    .foregroundStyle(DS.Colors.textSecondary)
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
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
        .contextMenu {
            Button("Delete", role: .destructive) {
                onDelete()
            }
        }
    }
}
