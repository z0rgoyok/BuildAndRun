import AppKit
import Shared
import SwiftUI

struct ContentView: View {
    @EnvironmentObject var root: KmpRoot
    @State private var presentedAlert: PresentedAlert?

    var body: some View {
        splitView
            .frame(minWidth: 900, minHeight: 550)
            .navigationTitle(navigationTitle)
            .toolbar { toolbarContent }
            .sheet(item: sheetBinding) { sheet in
                SheetContent(sheet: sheet)
                    .environmentObject(root)
            }
            .alert(item: $presentedAlert) { alert in
                Alert(
                    title: Text(alert.title),
                    message: Text(alert.message),
                    dismissButton: .default(Text("OK")) {
                        if alert.isError {
                            root.store.onDismissError()
                        } else {
                            root.store.onDismissSuccess()
                        }
                    }
                )
            }
            .onChange(of: root.state.error) { _, next in
                guard let next else { return }
                presentedAlert =
                    PresentedAlert(
                        isError: true,
                        title: next.message,
                        message: next.details ?? ""
                    )
            }
            .onChange(of: root.state.success) { _, next in
                guard let next else { return }
                presentedAlert =
                    PresentedAlert(
                        isError: false,
                        title: next.message,
                        message: ""
                    )
            }
    }

    private var splitView: some View {
        NavigationSplitView {
            ProjectTreeSidebar(selection: sidebarSelectionBinding)
                .frame(minWidth: DS.Sizes.sidebarMinWidth)
                .navigationSplitViewColumnWidth(
                    min: DS.Sizes.sidebarMinWidth,
                    ideal: DS.Sizes.sidebarIdealWidth,
                    max: DS.Sizes.sidebarMaxWidth
                )
        } detail: {
            KanbanBoard(selection: currentSelection)
                .frame(minWidth: 600)
                .environmentObject(root)
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItemGroup(placement: .primaryAction) {
            if root.selectedRepository != nil {
                Button {
                    root.presentSheet(.addWorktree)
                } label: {
                    Label("New Worktree", systemImage: "plus.square.on.square")
                }
                .help(root.store.kanbanLabels.toolbarNewWorktree)

                if let selectedWorktreePath = root.state.selectedWorktreePath {
                    OpenEditorMenu(root: root, worktreePath: selectedWorktreePath)

                    Button {
                        root.store.onOpenInFinder(worktreePath: selectedWorktreePath)
                    } label: {
                        Label("Finder", systemImage: "folder")
                    }
                    .help(root.store.kanbanLabels.toolbarFinder)

                    Button {
                        root.store.onOpenInTerminal(worktreePath: selectedWorktreePath)
                    } label: {
                        Label("Terminal", systemImage: "terminal")
                    }
                    .help(root.store.kanbanLabels.toolbarTerminal)
                }

                Button {
                    root.store.onRefreshSelectedRepository()
                } label: {
                    Label("Refresh", systemImage: "arrow.clockwise")
                }
                .help(root.store.kanbanLabels.toolbarRefresh)
            }
        }

        ToolbarItem(placement: .status) {
            if root.state.isLoading {
                HStack(spacing: 6) {
                    ProgressView()
                        .controlSize(.small)
                    Text("Loading…")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
        }
    }

    private var navigationTitle: String {
        if let worktree = root.selectedWorktree {
            return worktree.name
        }
        if let repo = root.selectedRepository {
            return repo.name
        }
        return "Worktree Manager"
    }

    private var currentSelection: SidebarSelection? {
        guard let repoId = root.state.selectedRepositoryId else { return nil }
        if let worktreePath = root.state.selectedWorktreePath {
            return .worktree(worktreePath: worktreePath, repositoryId: repoId)
        }
        return .repository(repositoryId: repoId)
    }

    private var sidebarSelectionBinding: Binding<SidebarSelection?> {
        Binding(
            get: { currentSelection },
            set: { newSelection in
                applySelection(newSelection)
            }
        )
    }

    private var sheetBinding: Binding<KmpRoot.Sheet?> {
        Binding(
            get: { root.sheet },
            set: { newValue in
                if let newValue {
                    root.presentSheet(newValue)
                } else {
                    root.dismissSheet()
                }
            }
        )
    }

    private func applySelection(_ selection: SidebarSelection?) {
        guard let selection else {
            root.store.onSelectWorktree(worktreePath: nil)
            return
        }

        switch selection {
        case .repository(let repositoryId):
            root.store.onSelectRepository(repositoryId: repositoryId)
        case .worktree(let worktreePath, let repositoryId):
            root.store.onSelectRepository(repositoryId: repositoryId)
            root.store.onSelectWorktree(worktreePath: worktreePath)
        }
    }

    private struct SheetContent: View {
        let sheet: KmpRoot.Sheet

        @EnvironmentObject private var root: KmpRoot

        var body: some View {
            switch sheet {
            case .addRepository:
                AddRepositorySheet()
                    .environmentObject(root)
            case .addWorktree:
                AddWorktreeSheet()
                    .environmentObject(root)
            case .createPR(let worktreePath):
                CreatePRSheet(worktreePath: worktreePath)
                    .environmentObject(root)
            case .completeWorktree(let worktreePath):
                CompleteWorktreeSheet(worktreePath: worktreePath)
                    .environmentObject(root)
            case .configureEditors:
                ConfigureEditorsSheet()
                    .environmentObject(root)
            case .help:
                HelpView()
            }
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(KmpRoot())
}

private struct PresentedAlert: Identifiable {
    let id = UUID()
    let isError: Bool
    let title: String
    let message: String
}
