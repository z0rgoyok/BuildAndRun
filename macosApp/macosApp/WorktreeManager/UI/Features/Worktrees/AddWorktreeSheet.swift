import Shared
import SwiftUI

struct AddWorktreeSheet: View {
    @EnvironmentObject var root: KmpRoot
    @Environment(\.dismiss) var dismiss

    @State private var worktreeName = ""
    @State private var branchName = ""
    @State private var createNewBranch = true
    @State private var selectedExistingBranch = ""
    @State private var baseBranch = "main"
    @State private var showBranchConflict = false
    @State private var isPreparing = false

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: 20) {
            Text(labels.createWorktreeTitle)
                .font(.headline)

            Form {
                TextField(labels.createWorktreeName, text: $worktreeName)
                    .textFieldStyle(.roundedBorder)

                Picker(labels.createBranchPicker, selection: $createNewBranch) {
                    Text(labels.createBranchNew).tag(true)
                    Text(labels.createBranchExisting).tag(false)
                }
                .pickerStyle(.segmented)

                branchSection
                locationPreview
            }
            .formStyle(.grouped)

            buttons
        }
        .padding()
        .frame(width: 480)
        .overlay { progressOverlay }
        .onAppear { loadBranches() }
        .onChange(of: branchName) { oldValue, newValue in
            if createNewBranch && (worktreeName.isEmpty || worktreeName == oldValue) {
                worktreeName = newValue
            }
        }
        .onChange(of: root.worktreesState.createWorktree.createdWorktreePath) { _, next in
            if next != nil { dismiss() }
        }
        .sheet(isPresented: $showBranchConflict) {
            branchConflictSheet
        }
    }
}

// MARK: - Subviews

private extension AddWorktreeSheet {
    @ViewBuilder
    var branchSection: some View {
        if createNewBranch {
            TextField(labels.createBranchName, text: $branchName)
                .textFieldStyle(.roundedBorder)
            baseBranchPicker
        } else {
            existingBranchPicker
        }
    }

    @ViewBuilder
    var baseBranchPicker: some View {
        if root.settingsState.branches.isEmpty {
            branchLoadingRow
        } else {
            Picker(labels.createBaseBranch, selection: $baseBranch) {
                ForEach(sortedBranches, id: \.self) { branch in
                    Text(branch).tag(branch)
                }
            }
        }
    }

    @ViewBuilder
    var existingBranchPicker: some View {
        if root.settingsState.branches.isEmpty {
            branchLoadingRow
        } else {
            Picker(labels.createBranchPicker, selection: $selectedExistingBranch) {
                ForEach(allBranches, id: \.self) { branch in
                    Text(branch).tag(branch)
                }
            }
        }
    }

    var branchLoadingRow: some View {
        HStack(spacing: 8) {
            ProgressView().controlSize(.small)
            Text(labels.createLoadingBranches).foregroundStyle(.secondary)
        }
    }

    @ViewBuilder
    var locationPreview: some View {
        if root.selectedRepository != nil, !worktreeName.isEmpty {
            LabeledContent(labels.createLocation) {
                Text(buildWorktreePath())
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
        }
    }

    var buttons: some View {
        HStack {
            Button(labels.createCancel) { dismiss() }
                .keyboardShortcut(.cancelAction)
            Spacer()
            Button(labels.createButton) { attemptCreate() }
                .keyboardShortcut(.defaultAction)
                .disabled(!isValid || root.settingsState.branches.isEmpty || isPreparing || createState.isSubmitting)
        }
    }

    @ViewBuilder
    var progressOverlay: some View {
        if isPreparing {
            BlockingProgressOverlay(title: labels.createPreparing)
        } else if createState.isSubmitting {
            BlockingProgressOverlay(title: labels.createSubmitting)
        }
    }

    var branchConflictSheet: some View {
        BranchConflictSheet(
            branchName: branchName,
            worktreeName: worktreeName,
            onUseExisting: {
                syncToKmpState(createBranch: false)
                root.store.worktrees.onCreateWorktree()
            },
            onRecreate: {
                syncToKmpState(createBranch: true)
                root.store.worktrees.onCreateWorktree()
            }
        )
    }
}

// MARK: - Logic

private extension AddWorktreeSheet {
    var createState: CreateWorktreeState { root.worktreesState.createWorktree }

    var allBranches: [String] { root.settingsState.branches as [String] }

    var sortedBranches: [String] {
        let priorityNames = ["main", "master", "develop", "development"]
        let priority = allBranches.filter { priorityNames.contains($0) }
        let others = allBranches.filter { !priorityNames.contains($0) && !$0.contains("/") }
        return priority + others
    }

    var isValid: Bool {
        let name = worktreeName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty else { return false }
        return createNewBranch
            ? !branchName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            : !selectedExistingBranch.isEmpty
    }

    func buildWorktreePath() -> String {
        guard let repo = root.selectedRepository else { return "" }
        let name = worktreeName.trimmingCharacters(in: .whitespacesAndNewlines)
        return "\(root.settingsState.worktreeBasePath)/\(repo.name)/\(name)"
    }

    func syncToKmpState(createBranch: Bool) {
        let branch = createBranch ? branchName : selectedExistingBranch
        root.store.worktrees.onCreateWorktreeBranchChanged(value: branch)
        root.store.worktrees.onCreateWorktreePathChanged(value: buildWorktreePath())
        root.store.worktrees.onCreateWorktreeBaseBranchChanged(value: createBranch ? baseBranch : "main")
        root.store.worktrees.onCreateWorktreeCreateBranchChanged(value: createBranch)
    }

    func attemptCreate() {
        if createNewBranch && root.store.settings.branchExists(branch: branchName) {
            showBranchConflict = true
            return
        }
        syncToKmpState(createBranch: createNewBranch)
        if createNewBranch {
            root.store.settings.onSetPreferredBaseBranch(branch: baseBranch)
        }
        root.store.worktrees.onCreateWorktree()
    }

    func loadBranches() {
        guard !isPreparing else { return }
        isPreparing = true
        root.store.settings.onLoadBranches()

        Task {
            for _ in 0 ..< 100 {
                if !root.settingsState.branches.isEmpty || root.messagesState.error != nil {
                    break
                }
                try? await Task.sleep(for: .milliseconds(50))
            }
            await MainActor.run {
                if selectedExistingBranch.isEmpty, let first = allBranches.first {
                    selectedExistingBranch = first
                }
                if let preferred = root.store.settings.preferredBaseBranch(),
                   allBranches.contains(preferred) {
                    baseBranch = preferred
                } else if let main = allBranches.first(where: { $0 == "main" || $0 == "master" }) {
                    baseBranch = main
                } else if let first = allBranches.first {
                    baseBranch = first
                }
                isPreparing = false
            }
        }
    }
}

#Preview {
    AddWorktreeSheet()
        .environmentObject(KmpRoot())
}
