import Shared
import SwiftUI

struct ProjectTreeSidebar: View {
    @EnvironmentObject var root: KmpRoot
    @Binding var selection: SidebarSelection?
    @State private var expandedRepositoryIds: Set<String> = []
    @State private var isArchivedSectionExpanded: Bool = false

    var body: some View {
        List(selection: $selection) {
            Section("Projects") {
                ForEach(activeRepositories, id: \.id) { repo in
                    DisclosureGroup(isExpanded: expansionBinding(for: repo.id)) {
                        ForEach(repo.worktrees, id: \.path) { worktree in
                            Text(worktree.name)
                                .tag(SidebarSelection.worktree(worktreePath: worktree.path, repositoryId: repo.id) as SidebarSelection?)
                        }
                    } label: {
                        Text(repo.name)
                            .tag(SidebarSelection.repository(repositoryId: repo.id) as SidebarSelection?)
                    }
                }
            }

            Section {
                DisclosureGroup("Archived Projects", isExpanded: $isArchivedSectionExpanded) {
                    ForEach(archivedRepositories, id: \.id) { repo in
                        Text(repo.name)
                            .tag(SidebarSelection.repository(repositoryId: repo.id) as SidebarSelection?)
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
            if expandedRepositoryIds.isEmpty, let selected = root.state.selectedRepositoryId {
                expandedRepositoryIds.insert(selected)
            }
        }
    }

    private var activeRepositories: [MacOSAppStore.RepositoryItem] {
        root.state.repositories.filter { !$0.isArchived }
    }

    private var archivedRepositories: [MacOSAppStore.RepositoryItem] {
        root.state.repositories.filter { $0.isArchived }
    }

    private func expansionBinding(for repositoryId: String) -> Binding<Bool> {
        Binding(
            get: { expandedRepositoryIds.contains(repositoryId) },
            set: { expanded in
                if expanded {
                    expandedRepositoryIds.insert(repositoryId)
                } else {
                    expandedRepositoryIds.remove(repositoryId)
                }
            }
        )
    }
}

#Preview {
    ProjectTreeSidebar(selection: .constant(nil))
        .environmentObject(KmpRoot())
}
