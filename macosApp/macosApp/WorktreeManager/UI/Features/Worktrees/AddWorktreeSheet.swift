import Shared
import SwiftUI

struct AddWorktreeSheet: View {
    @EnvironmentObject var root: KmpRoot
    @Environment(\.dismiss) var dismiss

    var body: some View {
        VStack(spacing: 20) {
            Text("Create New Worktree")
                .font(.headline)

            Form {
                TextField("Branch", text: branchBinding)
                    .textFieldStyle(.roundedBorder)

                TextField("Worktree Path", text: pathBinding)
                    .textFieldStyle(.roundedBorder)

                Toggle("Create Branch", isOn: createBranchBinding)

                TextField("Base Branch", text: baseBranchBinding)
                    .textFieldStyle(.roundedBorder)
                    .disabled(!createState.createBranch)
            }
            .formStyle(.grouped)

            HStack {
                Button("Cancel") {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Spacer()

                Button("Create") {
                    root.store.onCreateWorktree()
                }
                .keyboardShortcut(.defaultAction)
                .disabled(!canSubmit)
            }
        }
        .padding()
        .frame(width: 420)
        .overlay {
            if createState.isSubmitting {
                BlockingProgressOverlay(title: "Creating worktree…")
            }
        }
        .onAppear {
            if createState.baseBranchInput.isEmpty {
                root.store.onCreateWorktreeBaseBranchChanged(value: "main")
            }
        }
        .onChange(of: root.state.createWorktree.createdWorktreePath) { _, next in
            if next != nil {
                dismiss()
            }
        }
    }

    private var createState: MacOSAppStore.CreateWorktreeState {
        root.state.createWorktree
    }

    private var branchBinding: Binding<String> {
        Binding(
            get: { createState.branchInput },
            set: { root.store.onCreateWorktreeBranchChanged(value: $0) }
        )
    }

    private var pathBinding: Binding<String> {
        Binding(
            get: { createState.worktreePathInput },
            set: { root.store.onCreateWorktreePathChanged(value: $0) }
        )
    }

    private var baseBranchBinding: Binding<String> {
        Binding(
            get: { createState.baseBranchInput },
            set: { root.store.onCreateWorktreeBaseBranchChanged(value: $0) }
        )
    }

    private var createBranchBinding: Binding<Bool> {
        Binding(
            get: { createState.createBranch },
            set: { root.store.onCreateWorktreeCreateBranchChanged(value: $0) }
        )
    }

    private var canSubmit: Bool {
        let branch = createState.branchInput.trimmingCharacters(in: .whitespacesAndNewlines)
        let path = createState.worktreePathInput.trimmingCharacters(in: .whitespacesAndNewlines)
        return !createState.isSubmitting && !branch.isEmpty && !path.isEmpty
    }
}

#Preview {
    AddWorktreeSheet()
        .environmentObject(KmpRoot())
}
