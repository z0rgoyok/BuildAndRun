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

    private var baseBranches: [String] {
        let common = ["main", "master", "develop"]
        let available = root.state.branches.filter { common.contains($0) }
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
                    root.store.onCreatePullRequest(
                        worktreePath: worktreePath,
                        title: title,
                        body: prDescription,
                        baseBranch: baseBranch,
                    )
                    dismiss()
                }
                .keyboardShortcut(.defaultAction)
                .buttonStyle(.borderedProminent)
                .disabled(root.state.branches.isEmpty || isPreparing || isSubmitting)
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
    }

    private func prepare() async {
        guard !isPreparing else { return }
        isPreparing = true
        defer { isPreparing = false }

        if root.state.branches.isEmpty {
            root.store.onLoadBranches()
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
    }
}
