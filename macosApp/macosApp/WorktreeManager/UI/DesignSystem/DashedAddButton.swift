import SwiftUI

struct DashedAddButton: View {
    let title: String
    let action: () -> Void

    @State private var isHovered = false

    var body: some View {
        Button(action: action) {
            HStack(spacing: DS.Spacing.xs) {
                Image(systemName: "plus")
                    .font(.system(size: 11, weight: .medium))
                Text(title)
                    .font(.system(size: 12))
            }
            .foregroundStyle(DS.Colors.textSecondary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, DS.Spacing.sm)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .background(
            RoundedRectangle(cornerRadius: DS.Radius.md)
                .fill(isHovered ? DS.Colors.surfaceSecondary : Color.clear)
                .overlay(
                    RoundedRectangle(cornerRadius: DS.Radius.md)
                        .stroke(style: StrokeStyle(lineWidth: 1, dash: [5, 3]))
                        .foregroundStyle(DS.Colors.borderSubtle)
                )
        )
        .onHover { isHovered = $0 }
    }
}
