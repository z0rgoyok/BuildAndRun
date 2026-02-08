import Shared
import SwiftUI

struct ProjectTreeSidebar: View {
    @EnvironmentObject var root: KmpRoot
    @Binding var selection: SidebarSelection?
    @State private var expandedRepositoryIds: Set<String> = []
    @State private var isArchivedSectionExpanded: Bool = false
    @State private var repositoryForCopySettings: RepositoryCopySettingsTarget?

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(activeRepositories, id: \.id) { repo in
                    ProjectTreeNode(
                        repository: repo,
                        selection: $selection,
                        isExpanded: expansionBinding(for: repo),
                        onCopySettings: { showCopySettings(for: repo) }
                    )
                }

                if !archivedRepositories.isEmpty {
                    archivedSectionHeader

                    if isArchivedSectionExpanded {
                        ForEach(archivedRepositories, id: \.id) { repo in
                            ProjectTreeNode(
                                repository: repo,
                                selection: $selection,
                                isExpanded: expansionBinding(for: repo),
                                onCopySettings: { showCopySettings(for: repo) }
                            )
                        }
                    }
                }
            }
            .padding(.vertical, DS.Spacing.xs)
        }
        .background(DS.Colors.surfacePrimary)
        .toolbar {
            ToolbarItemGroup(placement: .automatic) {
                Button {
                    root.presentSheet(.addRepository)
                } label: {
                    Label("Add Repository", systemImage: "plus")
                }
            }
        }
        .onAppear { syncSelectionExpansion() }
        .onChange(of: selection) { _, _ in syncSelectionExpansion() }
        .onChange(of: root.state.selectedRepositoryId) { _, _ in syncSelectionExpansion() }
        .sheet(item: $repositoryForCopySettings) { target in
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

    private var archivedSectionHeader: some View {
        Button {
            withAnimation(DS.Animation.quick) {
                isArchivedSectionExpanded.toggle()
            }
        } label: {
            HStack(spacing: DS.Spacing.xs) {
                Image(systemName: isArchivedSectionExpanded ? "chevron.down" : "chevron.right")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(DS.Colors.textTertiary)
                    .frame(width: DS.Sizes.treeIconSize, height: DS.Sizes.treeIconSize)

                Image(systemName: "archivebox")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(DS.Colors.textTertiary)
                    .frame(width: DS.Sizes.treeIconSize)

                Text("Archived")
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
            get: { expandedRepositoryIds.contains(repo.id) },
            set: { expanded in
                if expanded {
                    expandedRepositoryIds.insert(repo.id)
                } else {
                    expandedRepositoryIds.remove(repo.id)
                }
            }
        )
    }

    private func showCopySettings(for repo: AppStore.RepositoryItem) {
        repositoryForCopySettings = RepositoryCopySettingsTarget(
            id: repo.id,
            name: repo.name
        )
    }

    private func syncSelectionExpansion() {
        guard let currentSelection = selection else { return }
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
