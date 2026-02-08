import Shared
import SwiftUI

struct ProjectTreeSidebar: View {
    @EnvironmentObject var root: KmpRoot
    @Binding var selection: SidebarSelection?
    @State private var expandedRepositoryIds: Set<String> = []
    @State private var isArchivedSectionExpanded: Bool = false
    @State private var hoveredRepositoryId: String?
    @State private var hoveredWorktreePath: String?
    @State private var repositoryForCopySettings: RepositoryCopySettingsTarget?

    var body: some View {
        List {
            Section("Projects") {
                ForEach(activeRepositories, id: \.id) { repo in
                    repositoryNode(repository: repo)
                }
            }

            if !archivedRepositories.isEmpty {
                Section {
                    DisclosureGroup("Archived Projects", isExpanded: $isArchivedSectionExpanded) {
                        ForEach(archivedRepositories, id: \.id) { repo in
                            repositoryNode(repository: repo)
                        }
                    }
                }
            }
        }
        .listStyle(.sidebar)
        .toolbar {
            ToolbarItemGroup(placement: .automatic) {
                Button {
                    root.presentSheet(.addRepository)
                } label: {
                    Label("Add Repository", systemImage: "plus")
                }
            }
        }
        .onAppear {
            syncSelectionExpansion()
        }
        .onChange(of: selection) { _, _ in
            syncSelectionExpansion()
        }
        .onChange(of: root.state.selectedRepositoryId) { _, _ in
            syncSelectionExpansion()
        }
        .sheet(item: $repositoryForCopySettings) { target in
            RepositoryCopyPatternsSheet(
                repositoryId: target.id,
                repositoryName: target.name,
            )
            .environmentObject(root)
        }
    }

    private var activeRepositories: [AppStore.RepositoryItem] {
        root.state.repositories.filter { !$0.isArchived }
    }

    private var archivedRepositories: [AppStore.RepositoryItem] {
        root.state.repositories.filter { $0.isArchived }
    }

    @ViewBuilder
    private func repositoryNode(repository: AppStore.RepositoryItem) -> some View {
        let isArchived = repository.isArchived
        let isExpanded = expandedRepositoryIds.contains(repository.id)
        let isSelected =
            if case .repository(let selectedRepositoryId) = selection {
                selectedRepositoryId == repository.id
            } else {
                false
            }

        VStack(spacing: 0) {
            HStack(spacing: DS.Spacing.xs) {
                if !isArchived {
                    Button {
                        withAnimation(DS.Animation.quick) {
                            if isExpanded {
                                expandedRepositoryIds.remove(repository.id)
                            } else {
                                expandedRepositoryIds.insert(repository.id)
                            }
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
                    .foregroundStyle(isArchived ? DS.Colors.textTertiary : .blue)
                    .frame(width: DS.Sizes.treeIconSize)

                VStack(alignment: .leading, spacing: 1) {
                    Text(repository.name)
                        .font(DS.Typography.treeItem)
                        .foregroundStyle(isArchived ? DS.Colors.textSecondary : DS.Colors.textPrimary)
                        .lineLimit(1)

                    Text(repository.path)
                        .font(DS.Typography.treeItemSecondary)
                        .foregroundStyle(isArchived ? DS.Colors.textQuaternary : DS.Colors.textTertiary)
                        .lineLimit(1)
                        .truncationMode(.middle)
                }
                .help(repository.path)

                Spacer()

                if !isArchived, !repository.worktrees.isEmpty {
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
                isSelected ? DS.Colors.sidebarSelected :
                hoveredRepositoryId == repository.id ? DS.Colors.sidebarHover : Color.clear
            )
            .cornerRadius(DS.Radius.sm)
            .padding(.horizontal, DS.Spacing.xs)
            .contentShape(Rectangle())
            .onHover { hovered in
                hoveredRepositoryId = hovered ? repository.id : nil
            }
            .onTapGesture {
                if !isArchived {
                    if isExpanded && isSelected {
                        withAnimation(DS.Animation.quick) {
                            expandedRepositoryIds.remove(repository.id)
                        }
                    } else {
                        withAnimation(DS.Animation.quick) {
                            expandedRepositoryIds.insert(repository.id)
                        }
                    }
                }
                selection = .repository(repositoryId: repository.id)
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
                    repositoryForCopySettings =
                        RepositoryCopySettingsTarget(
                            id: repository.id,
                            name: repository.name,
                        )
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

            if !isArchived, isExpanded {
                VStack(spacing: 0) {
                    ForEach(sortedWorktrees(of: repository), id: \.path) { worktree in
                        worktreeRow(worktree: worktree, repositoryId: repository.id)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func worktreeRow(
        worktree: AppStore.WorktreeItem,
        repositoryId: String,
    ) -> some View {
        let isSelected =
            if case .worktree(let selectedWorktreePath, _) = selection {
                selectedWorktreePath == worktree.path
            } else {
                false
            }

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
            hoveredWorktreePath == worktree.path ? DS.Colors.sidebarHover : Color.clear
        )
        .cornerRadius(DS.Radius.sm)
        .padding(.horizontal, DS.Spacing.xs)
        .contentShape(Rectangle())
        .onHover { hovered in
            hoveredWorktreePath = hovered ? worktree.path : nil
        }
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
        .contextMenu {
            WorktreeMenuItems(root: root, worktreePath: worktree.path, includeNewWorktree: false)
        }
    }

    private func sortedWorktrees(of repository: AppStore.RepositoryItem) -> [AppStore.WorktreeItem] {
        repository.worktrees.sorted { lhs, rhs in
            if lhs.isMain && !rhs.isMain {
                return true
            }
            if !lhs.isMain && rhs.isMain {
                return false
            }
            return lhs.name.localizedCaseInsensitiveCompare(rhs.name) == .orderedAscending
        }
    }

    private func syncSelectionExpansion() {
        guard let currentSelection = selection else {
            return
        }
        let selectedRepositoryId = currentSelection.repositoryId
        if !selectedRepositoryId.isEmpty {
            expandedRepositoryIds.insert(selectedRepositoryId)
            let selectedRepository =
                root.state.repositories.first { $0.id == selectedRepositoryId }
            if selectedRepository?.isArchived == true {
                isArchivedSectionExpanded = true
            }
        }
    }
}

private struct RepositoryCopySettingsTarget: Identifiable {
    let id: String
    let name: String
}

#Preview {
    ProjectTreeSidebar(selection: .constant(nil))
        .environmentObject(KmpRoot())
}
