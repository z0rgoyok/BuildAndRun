import Shared
import SwiftUI

struct ProjectTreeSidebar: View {
    @EnvironmentObject var root: KmpRoot
    @Binding var selection: SidebarSelection?

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(activeRepositories, id: \.id) { repo in
                    ProjectTreeNode(
                        repository: repo,
                        selection: $selection,
                        isExpanded: expansionBinding(for: repo),
                        onCopySettings: { root.presentSidebarCopySettings(for: repo) }
                    )
                }

                if !archivedRepositories.isEmpty {
                    archivedSectionHeader

                    if root.isSidebarArchivedSectionExpanded {
                        ForEach(archivedRepositories, id: \.id) { repo in
                            ProjectTreeNode(
                                repository: repo,
                                selection: $selection,
                                isExpanded: expansionBinding(for: repo),
                                onCopySettings: { root.presentSidebarCopySettings(for: repo) }
                            )
                        }
                    }
                }
            }
            .padding(.vertical, DS.Spacing.xs)
        }
        .background(DS.Colors.surfacePrimary)
        .toolbar {
            ToolbarItem(placement: .automatic) {
                Button {
                    root.presentSheet(.addRepository)
                } label: {
                    Label(root.store.sidebarLabels.addRepository, systemImage: "plus")
                }
            }

            ToolbarItem(placement: .primaryAction) {
                Button {
                    withAnimation(DS.Animation.quick) {
                        root.toggleSidebarAllRepositoriesExpansion(selection: selection)
                    }
                } label: {
                    Image(systemName: root.areAllSidebarRepositoriesExpanded ? "arrow.down.right.and.arrow.up.left" : "arrow.up.left.and.arrow.down.right")
                }
                .help(collapseExpandHelpText)
                .disabled(allRepositoryIds.isEmpty)
            }

            ToolbarItem(placement: .primaryAction) {
                Button {
                    root.presentSheet(.help)
                } label: {
                    Image(systemName: "questionmark.circle")
                }
                .help(root.store.sidebarLabels.help)
            }
        }
        .onAppear { root.syncSidebarSelectionExpansion(selection: selection) }
        .onChange(of: selection) { _, _ in root.syncSidebarSelectionExpansion(selection: selection) }
        .onChange(of: root.state.selectedRepositoryId) { _, _ in root.syncSidebarSelectionExpansion(selection: selection) }
        .sheet(item: sidebarCopySettingsTargetBinding) { target in
            RepositoryCopyPatternsSheet(
                repositoryId: target.id,
                repositoryName: target.name
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

    private var allRepositoryIds: Set<String> {
        Set(root.state.repositories.map(\.id))
    }

    private var collapseExpandHelpText: String {
        if root.areAllSidebarRepositoriesExpanded {
            return root.store.sidebarLabels.collapseAll
        }
        return root.store.sidebarLabels.expandAll
    }

    private var sidebarCopySettingsTargetBinding: Binding<SidebarCopySettingsTarget?> {
        Binding(
            get: { root.sidebarCopySettingsTarget },
            set: { root.sidebarCopySettingsTarget = $0 }
        )
    }

    private var archivedSectionHeader: some View {
        Button {
            withAnimation(DS.Animation.quick) {
                root.isSidebarArchivedSectionExpanded.toggle()
            }
        } label: {
            HStack(spacing: DS.Spacing.xs) {
                Image(systemName: root.isSidebarArchivedSectionExpanded ? "chevron.down" : "chevron.right")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(DS.Colors.textTertiary)
                    .frame(width: DS.Sizes.treeIconSize, height: DS.Sizes.treeIconSize)

                Image(systemName: "archivebox")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(DS.Colors.textTertiary)
                    .frame(width: DS.Sizes.treeIconSize)

                Text(root.store.sidebarLabels.archived)
                    .font(DS.Typography.sectionHeader)
                    .foregroundStyle(DS.Colors.textSecondary)

                Spacer()

                Text("\(archivedRepositories.count)")
                    .font(DS.Typography.badge)
                    .foregroundStyle(DS.Colors.textSecondary)
                    .padding(.horizontal, DS.Spacing.xs)
                    .padding(.vertical, DS.Spacing.xxxs)
                    .background(DS.Colors.surfaceSecondary)
                    .cornerRadius(DS.Radius.xs)
            }
            .padding(.horizontal, DS.Spacing.md)
            .padding(.vertical, DS.Spacing.sm)
            .frame(height: DS.Sizes.treeRowHeight)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .padding(.horizontal, DS.Spacing.xs)
        .padding(.top, DS.Spacing.xs)
    }

    private func expansionBinding(for repo: AppStore.RepositoryItem) -> Binding<Bool> {
        Binding(
            get: { root.sidebarExpandedRepositoryIds.contains(repo.id) },
            set: { expanded in
                root.setSidebarRepositoryExpanded(repositoryId: repo.id, expanded: expanded)
            }
        )
    }
}

#Preview {
    ProjectTreeSidebar(selection: .constant(nil))
        .environmentObject(KmpRoot())
}
