import Shared
import SwiftUI

struct SidebarSectionHeader: View {
    @EnvironmentObject var root: KmpRoot
    let groupId: String
    let groupName: String
    let onRename: (String) -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: DS.Spacing.xs) {
            Text(groupName.uppercased())
                .font(DS.Typography.sectionHeader)
                .foregroundStyle(DS.Colors.textTertiary)
                .tracking(0.5)
                .lineLimit(1)

            Spacer()
        }
        .padding(.horizontal, DS.Spacing.md)
        .padding(.vertical, DS.Spacing.sm)
        .frame(height: DS.Sizes.treeRowHeight)
        .padding(.horizontal, DS.Spacing.xs)
        .padding(.top, DS.Spacing.xs)
        .contextMenu {
            Button(root.store.sidebarLabels.renameGroup) {
                onRename(groupId)
            }

            Button(root.store.sidebarLabels.deleteGroup, role: .destructive) {
                onDelete()
            }
        }
    }
}
