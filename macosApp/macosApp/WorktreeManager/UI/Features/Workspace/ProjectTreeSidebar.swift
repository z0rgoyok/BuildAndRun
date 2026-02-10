import Shared
import SwiftUI

struct ProjectTreeSidebar: View {
    @EnvironmentObject var root: KmpRoot
    @Binding var selection: SidebarSelection?

    @State private var isNewGroupAlertPresented = false
    @State private var newGroupName = ""
    @State private var newGroupRepositoryId: String?

    @State private var isRenameGroupAlertPresented = false
    @State private var renameGroupId: String?
    @State private var renameGroupName = ""

    @State private var draggedGroupId: String?
    @State private var activeDropTargetGroupId: String?
    @State private var activeDropPlacement: GroupSectionDropDelegate.DropPlacement = .before

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(sidebarSectionsWithStableIds, id: \.id) { item in
                    sidebarSection(item.section)
                }

                if !archivedRepositories.isEmpty {
                    archivedSectionHeader

                    if root.isSidebarArchivedSectionExpanded {
                        ForEach(archivedRepositories, id: \.id) { repo in
                            ProjectTreeNode(
                                repository: repo,
                                selection: $selection,
                                isExpanded: expansionBinding(for: repo),
                                onCopySettings: { root.presentSidebarCopySettings(for: repo) },
                                onNewGroupForRepository: { presentNewGroupAlert(forRepositoryId: $0) }
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
                        root.toggleVisibleSidebarRepositoriesExpansion(selection: selection)
                    }
                } label: {
                    Image(systemName: root.areVisibleSidebarRepositoriesExpanded() ? "arrow.down.right.and.arrow.up.left" : "arrow.up.left.and.arrow.down.right")
                }
                .help(collapseExpandHelpText)
                .disabled(!root.hasVisibleSidebarRepositories())
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
        .alert(root.store.sidebarLabels.newGroup, isPresented: $isNewGroupAlertPresented) {
            TextField(root.store.sidebarLabels.groupNamePrompt, text: $newGroupName)
            Button(role: .cancel) { resetNewGroupState() } label: { Text("Cancel") }
            Button("OK") { submitNewGroup() }
        }
        .alert(root.store.sidebarLabels.renameGroup, isPresented: $isRenameGroupAlertPresented) {
            TextField(root.store.sidebarLabels.groupNamePrompt, text: $renameGroupName)
            Button(role: .cancel) { resetRenameGroupState() } label: { Text("Cancel") }
            Button("OK") { submitRenameGroup() }
        }
    }

    @ViewBuilder
    private func sidebarSection(_ section: AppStore.SidebarSection) -> some View {
        if let groupName = section.groupName, let groupId = section.groupId {
            SidebarSectionHeader(
                groupId: groupId,
                groupName: groupName,
                isDropTargetBefore: activeDropTargetGroupId == groupId && activeDropPlacement == .before,
                isDropTargetAfter: activeDropTargetGroupId == groupId && activeDropPlacement == .after,
                isExpanded: groupExpansionBinding(for: groupId),
                onDragStart: { draggedId in
                    draggedGroupId = draggedId
                    activeDropTargetGroupId = nil
                },
                onRename: { id in presentRenameGroupAlert(groupId: id, currentName: groupName) },
                onDelete: { root.store.groups.onDeleteRepositoryGroup(groupId: groupId) }
            )
            .onDrop(
                of: [.text],
                delegate: GroupSectionDropDelegate(
                    targetGroupId: groupId,
                    draggedGroupId: $draggedGroupId,
                    activeTargetGroupId: $activeDropTargetGroupId,
                    activeDropPlacement: $activeDropPlacement,
                    onReorder: { draggedId, targetId, placement in
                        reorderGroup(draggedId: draggedId, targetId: targetId, placement: placement)
                    }
                )
            )

            if !root.isSidebarGroupCollapsed(groupId: groupId) {
                ForEach(section.repositories, id: \.id) { repo in
                    ProjectTreeNode(
                        repository: repo,
                        selection: $selection,
                        isExpanded: expansionBinding(for: repo),
                        onCopySettings: { root.presentSidebarCopySettings(for: repo) },
                        onNewGroupForRepository: { presentNewGroupAlert(forRepositoryId: $0) }
                    )
                }
            }
        } else {
            ForEach(section.repositories, id: \.id) { repo in
                ProjectTreeNode(
                    repository: repo,
                    selection: $selection,
                    isExpanded: expansionBinding(for: repo),
                    onCopySettings: { root.presentSidebarCopySettings(for: repo) },
                    onNewGroupForRepository: { presentNewGroupAlert(forRepositoryId: $0) }
                )
            }
        }
    }

    private var archivedRepositories: [AppStore.RepositoryItem] {
        root.state.repositories.filter { $0.isArchived }
    }

    private var sidebarSectionsWithStableIds: [(id: String, section: AppStore.SidebarSection)] {
        root.state.sidebarSections.map { section in
            let id = section.groupId.map { "group:\($0)" } ?? "ungrouped"
            return (id: id, section: section)
        }
    }

    private var collapseExpandHelpText: String {
        if root.areVisibleSidebarRepositoriesExpanded() {
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

    private func groupExpansionBinding(for groupId: String) -> Binding<Bool> {
        Binding(
            get: { !root.isSidebarGroupCollapsed(groupId: groupId) },
            set: { expanded in
                root.setSidebarGroupCollapsed(groupId: groupId, collapsed: !expanded)
            }
        )
    }

    private func reorderGroup(
        draggedId: String,
        targetId: String,
        placement: GroupSectionDropDelegate.DropPlacement,
    ) {
        var orderedGroupIds = root.state.sidebarSections.compactMap(\.groupId)
        guard let fromIndex = orderedGroupIds.firstIndex(of: draggedId),
              let targetIndex = orderedGroupIds.firstIndex(of: targetId) else { return }

        orderedGroupIds.remove(at: fromIndex)

        var insertionIndex = targetIndex
        if fromIndex < targetIndex {
            insertionIndex -= 1
        }
        if placement == .after {
            insertionIndex += 1
        }

        insertionIndex = min(max(insertionIndex, 0), orderedGroupIds.count)
        orderedGroupIds.insert(draggedId, at: insertionIndex)
        root.store.groups.onReorderRepositoryGroups(orderedGroupIds: orderedGroupIds)
    }

    private func expansionBinding(for repo: AppStore.RepositoryItem) -> Binding<Bool> {
        Binding(
            get: { root.isSidebarRepositoryExpanded(repositoryId: repo.id) },
            set: { expanded in
                root.setSidebarRepositoryExpanded(repositoryId: repo.id, expanded: expanded)
            }
        )
    }

    private func presentNewGroupAlert(forRepositoryId repositoryId: String?) {
        newGroupName = ""
        newGroupRepositoryId = repositoryId
        isNewGroupAlertPresented = true
    }

    private func resetNewGroupState() {
        newGroupName = ""
        newGroupRepositoryId = nil
    }

    private func submitNewGroup() {
        let name = newGroupName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty else { return }
        if let repositoryId = newGroupRepositoryId {
            root.store.groups.onCreateGroupAndAssignRepository(name: name, repositoryId: repositoryId)
        } else {
            root.store.groups.onCreateRepositoryGroup(name: name)
        }
        resetNewGroupState()
    }

    private func presentRenameGroupAlert(groupId: String, currentName: String) {
        renameGroupId = groupId
        renameGroupName = currentName
        isRenameGroupAlertPresented = true
    }

    private func resetRenameGroupState() {
        renameGroupId = nil
        renameGroupName = ""
    }

    private func submitRenameGroup() {
        let name = renameGroupName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty, let groupId = renameGroupId else { return }
        root.store.groups.onRenameRepositoryGroup(groupId: groupId, newName: name)
        resetRenameGroupState()
    }
}

#Preview {
    ProjectTreeSidebar(selection: .constant(nil))
        .environmentObject(KmpRoot())
}
