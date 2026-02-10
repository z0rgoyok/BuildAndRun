import AppKit
import Shared
import SwiftUI

struct ProjectTreeNode: View {
    @EnvironmentObject var root: KmpRoot
    let repository: RepositoryItem
    @Binding var selection: SidebarSelection?
    @Binding var isExpanded: Bool
    let onCopySettings: () -> Void
    let onNewGroupForRepository: (String) -> Void

    @State private var isHovered = false

    private var isRepoSelected: Bool {
        if case .repository(let selectedId) = selection {
            return selectedId == repository.id
        }
        return false
    }

    var body: some View {
        VStack(spacing: 0) {
            repoHeader

            if !repository.isArchived, isExpanded {
                worktreeList
            }
        }
    }

    private var repoHeader: some View {
        HStack(spacing: DS.Spacing.xs) {
            if !repository.isArchived {
                Button {
                    withAnimation(DS.Animation.quick) {
                        isExpanded.toggle()
                    }
                } label: {
                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundStyle(DS.Colors.textTertiary)
                        .frame(width: DS.Sizes.treeIconSize, height: DS.Sizes.treeIconSize)
                }
                .buttonStyle(.plain)
            } else {
                Image(systemName: "chevron.right")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(DS.Colors.textQuaternary)
                    .frame(width: DS.Sizes.treeIconSize, height: DS.Sizes.treeIconSize)
            }

            Image(systemName: "folder.fill")
                .font(.system(size: 14))
                .foregroundStyle(repository.isArchived ? DS.Colors.textTertiary : .blue)
                .frame(width: DS.Sizes.treeIconSize)

            VStack(alignment: .leading, spacing: 1) {
                Text(repository.name)
                    .font(DS.Typography.treeItem)
                    .foregroundStyle(repository.isArchived ? DS.Colors.textSecondary : DS.Colors.textPrimary)
                    .lineLimit(1)

                Text(repository.path)
                    .font(DS.Typography.treeItemSecondary)
                    .foregroundStyle(repository.isArchived ? DS.Colors.textQuaternary : DS.Colors.textTertiary)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
            .help(repository.path)

            Spacer()

            if !repository.isArchived, !repository.worktrees.isEmpty {
                Text("\(repository.worktrees.count)")
                    .font(DS.Typography.badge)
                    .foregroundStyle(DS.Colors.textSecondary)
                    .padding(.horizontal, DS.Spacing.xs)
                    .padding(.vertical, DS.Spacing.xxxs)
                    .background(DS.Colors.surfaceSecondary)
                    .cornerRadius(DS.Radius.xs)
            }
        }
        .padding(.horizontal, DS.Spacing.md)
        .padding(.vertical, DS.Spacing.sm)
        .frame(height: DS.Sizes.treeRowHeight + 8)
        .background(
            isRepoSelected ? DS.Colors.sidebarSelected :
            isHovered ? DS.Colors.sidebarHover : Color.clear
        )
        .cornerRadius(DS.Radius.sm)
        .padding(.horizontal, DS.Spacing.xs)
        .contentShape(Rectangle())
        .onHover { isHovered = $0 }
        .onTapGesture {
            if !repository.isArchived {
                if isExpanded && isRepoSelected {
                    withAnimation(DS.Animation.quick) {
                        isExpanded = false
                    }
                } else {
                    selection = .repository(repositoryId: repository.id)
                    if !isExpanded {
                        withAnimation(DS.Animation.quick) {
                            isExpanded = true
                        }
                    }
                }
            }
        }
        .contextMenu {
            Button(root.store.sidebarLabels.showInFinder) {
                NSWorkspace.shared.selectFile(nil, inFileViewerRootedAtPath: repository.path)
            }

            Button {
                NSPasteboard.general.clearContents()
                NSPasteboard.general.setString(repository.path, forType: .string)
            } label: {
                Label(root.store.sidebarLabels.copyPath, systemImage: "doc.on.doc")
            }

            Button {
                onCopySettings()
            } label: {
                Label(root.store.sidebarLabels.copyFilesSettings, systemImage: "doc.on.doc")
            }

            if !repository.isArchived {
                Divider()
                groupMenu
            }

            Divider()

            if repository.isArchived {
                Button {
                    root.store.repositories.onRestoreRepository(repositoryId: repository.id)
                } label: {
                    Label(root.store.sidebarLabels.restoreProject, systemImage: "arrow.uturn.left")
                }
            } else {
                Button {
                    root.store.repositories.onArchiveRepository(repositoryId: repository.id)
                } label: {
                    Label(root.store.sidebarLabels.archiveProject, systemImage: "archivebox")
                }
            }

            Divider()

            Button(root.store.sidebarLabels.removeFromList, role: .destructive) {
                root.store.repositories.onRemoveRepository(repositoryId: repository.id)
            }
        }
    }

    @ViewBuilder
    private var groupMenu: some View {
        Menu(root.store.sidebarLabels.moveToGroup) {
            ForEach(root.repositoriesState.repositoryGroups, id: \.id) { group in
                Button(group.name) {
                    root.store.groups.onSetRepositoryGroup(repositoryId: repository.id, groupId: group.id)
                }
                .disabled(repository.groupId == group.id)
            }

            if !root.repositoriesState.repositoryGroups.isEmpty {
                Divider()
            }

            Button(root.store.sidebarLabels.newGroup) {
                onNewGroupForRepository(repository.id)
            }

            if repository.groupId != nil {
                Divider()

                Button(root.store.sidebarLabels.removeFromGroup) {
                    root.store.groups.onSetRepositoryGroup(repositoryId: repository.id, groupId: nil)
                }
            }
        }
    }

    private var worktreeList: some View {
        VStack(spacing: 0) {
            ForEach(sortedWorktrees, id: \.path) { worktree in
                WorktreeTreeRow(
                    worktree: worktree,
                    repositoryId: repository.id,
                    selection: $selection
                )
            }
        }
    }

    private var sortedWorktrees: [WorktreeItem] {
        repository.worktrees.sorted { lhs, rhs in
            if lhs.isMain && !rhs.isMain { return true }
            if !lhs.isMain && rhs.isMain { return false }
            return lhs.name.localizedCaseInsensitiveCompare(rhs.name) == .orderedAscending
        }
    }
}
