import Shared
import SwiftUI

struct CreatePRSheet: View {
    @EnvironmentObject var root: KmpRoot
    @Environment(\.dismiss) var dismiss

    let worktreePath: String

    @State private var title: String = ""
    @State private var prDescription: String = ""
    @State private var baseBranch: String = "main"
    @State private var isPreparing = false
    @State private var isSubmitting = false
    @State private var isSubmissionRequested = false

    private var baseBranches: [String] {
        let common = ["main", "master", "develop"]
        let available = root.settingsState.branches.filter { common.contains($0) }
        return available.isEmpty ? common : available
    }

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Image(systemName: "arrow.triangle.pull")
                    .foregroundStyle(.green)
                Text("Create Pull Request")
                    .font(.headline)
            }

            Form {
                TextField("Title", text: $title)
                    .textFieldStyle(.roundedBorder)

                VStack(alignment: .leading, spacing: 4) {
                    Text("Description")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    TextEditor(text: $prDescription)
                        .frame(height: 80)
                        .font(.body)
                        .overlay(
                            RoundedRectangle(cornerRadius: 4)
                                .stroke(Color(nsColor: .separatorColor), lineWidth: 1)
                        )
                }

                Picker("Base branch", selection: $baseBranch) {
                    ForEach(baseBranches, id: \.self) { branch in
                        Text(branch).tag(branch)
                    }
                }
            }
            .formStyle(.grouped)

            HStack(spacing: 12) {
                Button("Cancel") {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Button("Create PR") {
                    submit()
                }
                .keyboardShortcut(.defaultAction)
                .buttonStyle(.borderedProminent)
                .disabled(root.settingsState.branches.isEmpty || isPreparing || isSubmitting)
            }
        }
        .padding(24)
        .frame(width: 420)
        .overlay {
            if isPreparing {
                BlockingProgressOverlay(title: "Preparing…")
            } else if isSubmitting {
                BlockingProgressOverlay(title: "Creating pull request…")
            }
        }
        .task {
            await prepare()
        }
        .onChange(of: root.messagesState.success?.message) { _, next in
            guard isSubmissionRequested, next != nil else { return }
            isSubmitting = false
            isSubmissionRequested = false
            dismiss()
        }
        .onChange(of: root.messagesState.error?.message) { _, next in
            guard isSubmissionRequested, next != nil else { return }
            isSubmitting = false
            isSubmissionRequested = false
        }
    }

    private func submit() {
        guard !isPreparing, !isSubmitting else { return }
        isSubmitting = true
        isSubmissionRequested = true
        root.store.gitActions.onCreatePullRequest(
            worktreePath: worktreePath,
            title: title,
            body: prDescription,
            baseBranch: baseBranch,
        )
    }

    private func prepare() async {
        guard !isPreparing else { return }
        isPreparing = true

        if root.settingsState.branches.isEmpty {
            root.store.settings.onLoadBranches()
            for _ in 0 ..< 100 {
                if !root.settingsState.branches.isEmpty || root.messagesState.error != nil {
                    break
                }
                try? await Task.sleep(for: .milliseconds(50))
            }
        }

        let branchName =
            root.selectedRepository?.worktrees
                .first(where: { $0.path == worktreePath })?
                .branch
                ?? ""
        title = branchName
        if let main = baseBranches.first {
            baseBranch = main
        }
        isPreparing = false
    }
}
