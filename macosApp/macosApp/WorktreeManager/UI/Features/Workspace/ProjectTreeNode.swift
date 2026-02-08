import AppKit
import Shared
import SwiftUI

struct ProjectTreeNode: View {
    @EnvironmentObject var root: KmpRoot
    let repository: AppStore.RepositoryItem
    @Binding var selection: SidebarSelection?
    @Binding var isExpanded: Bool
    let onCopySettings: () -> Void

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
            Button("Show in Finder") {
                NSWorkspace.shared.selectFile(nil, inFileViewerRootedAtPath: repository.path)
            }

            Button {
                NSPasteboard.general.clearContents()
                NSPasteboard.general.setString(repository.path, forType: .string)
            } label: {
                Label("Copy Path", systemImage: "doc.on.doc")
            }

            Button {
                onCopySettings()
            } label: {
                Label("Copy Files Settings...", systemImage: "doc.on.doc")
            }

            Divider()

            if repository.isArchived {
                Button {
                    root.store.onRestoreRepository(repositoryId: repository.id)
                } label: {
                    Label("Restore Project", systemImage: "arrow.uturn.left")
                }
            } else {
                Button {
                    root.store.onArchiveRepository(repositoryId: repository.id)
                } label: {
                    Label("Archive Project", systemImage: "archivebox")
                }
            }

            Divider()

            Button("Remove from List", role: .destructive) {
                root.store.onRemoveRepository(repositoryId: repository.id)
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

    private var sortedWorktrees: [AppStore.WorktreeItem] {
        repository.worktrees.sorted { lhs, rhs in
            if lhs.isMain && !rhs.isMain { return true }
            if !lhs.isMain && rhs.isMain { return false }
            return lhs.name.localizedCaseInsensitiveCompare(rhs.name) == .orderedAscending
        }
    }
}
