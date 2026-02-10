import Shared
import SwiftUI

struct WorktreeTreeRow: View {
    @EnvironmentObject var root: KmpRoot
    let worktree: WorktreeItem
    let repositoryId: String
    @Binding var selection: SidebarSelection?

    @State private var isHovered = false

    private var isSelected: Bool {
        if case .worktree(let selectedPath, _) = selection {
            return selectedPath == worktree.path
        }
        return false
    }

    var body: some View {
        HStack(spacing: DS.Spacing.xs) {
            Color.clear
                .frame(width: DS.Sizes.treeIndent + DS.Sizes.treeIconSize)

            Image(systemName: worktree.isMain ? "house.fill" : "arrow.triangle.branch")
                .font(.system(size: 12))
                .foregroundStyle(worktree.isMain ? .orange : DS.Colors.textSecondary)
                .frame(width: DS.Sizes.treeIconSize)

            VStack(alignment: .leading, spacing: 1) {
                HStack(spacing: DS.Spacing.xs) {
                    Text(worktree.name)
                        .font(DS.Typography.treeItem)
                        .foregroundStyle(DS.Colors.textPrimary)
                        .lineLimit(1)

                    if worktree.isMain {
                        StatusBadge(text: worktree.branch, color: .blue)
                    }

                    if worktree.isLocked {
                        Image(systemName: "lock.fill")
                            .font(.system(size: 9))
                            .foregroundStyle(.orange)
                    }
                }

                HStack(spacing: DS.Spacing.xs) {
                    Text(worktree.branch)
                        .font(DS.Typography.treeItemSecondary)
                        .foregroundStyle(DS.Colors.textTertiary)
                        .lineLimit(1)

                    if worktree.isStatusLoading {
                        ProgressView()
                            .controlSize(.mini)
                            .transition(.opacity)
                    } else if let status = worktree.status {
                        WorktreeStatusIndicators(status: status)
                    }
                }
            }
            .help(worktree.path)

            Spacer()
        }
        .padding(.horizontal, DS.Spacing.md)
        .padding(.vertical, DS.Spacing.sm)
        .frame(minHeight: DS.Sizes.treeRowHeight)
        .background(
            isSelected ? DS.Colors.sidebarSelected :
            isHovered ? DS.Colors.sidebarHover : Color.clear
        )
        .cornerRadius(DS.Radius.sm)
        .padding(.horizontal, DS.Spacing.xs)
        .contentShape(Rectangle())
        .onHover { isHovered = $0 }
        .overlay {
            RightClickHandlerView(
                onLeftClick: {
                    selection = .worktree(worktreePath: worktree.path, repositoryId: repositoryId)
                },
                onRightClick: {
                    selection = .worktree(worktreePath: worktree.path, repositoryId: repositoryId)
                }
            )
            .allowsHitTesting(true)
            .accessibilityHidden(true)
        }
        .animation(DS.Animation.quick, value: worktree.isStatusLoading)
        .contextMenu {
            WorktreeMenuItems(root: root, worktreePath: worktree.path, includeNewWorktree: false)
        }
    }
}
