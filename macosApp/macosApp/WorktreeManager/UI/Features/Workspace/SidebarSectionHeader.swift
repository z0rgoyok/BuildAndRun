import Shared
import SwiftUI

struct SidebarSectionHeader: View {
    @EnvironmentObject var root: KmpRoot
    let groupId: String
    let groupName: String
    let isDropTargetBefore: Bool
    let isDropTargetAfter: Bool
    @Binding var isExpanded: Bool
    let onDragStart: (String) -> Void
    let onRename: (String) -> Void
    let onDelete: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            if isDropTargetBefore {
                DropIndicator()
                    .padding(.horizontal, DS.Spacing.md)
                    .padding(.bottom, DS.Spacing.xxxs)
            }

            HStack(spacing: DS.Spacing.xs) {
                Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(DS.Colors.textTertiary)
                    .frame(width: DS.Sizes.treeIconSize, height: DS.Sizes.treeIconSize)

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
            .contentShape(Rectangle())
            .onTapGesture {
                withAnimation(DS.Animation.quick) {
                    isExpanded.toggle()
                }
            }

            if isDropTargetAfter {
                DropIndicator()
                    .padding(.horizontal, DS.Spacing.md)
                    .padding(.top, DS.Spacing.xxxs)
            }
        }
        .padding(.horizontal, DS.Spacing.xs)
        .padding(.top, DS.Spacing.xs)
        .onDrag {
            onDragStart(groupId)
            return NSItemProvider(object: groupId as NSString)
        }
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
