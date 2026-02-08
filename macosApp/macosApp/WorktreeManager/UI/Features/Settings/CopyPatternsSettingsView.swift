import SwiftUI

struct CopyPatternsSettingsView: View {
    @Binding var patterns: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Default Copy Patterns")
                .font(.headline)

            Text("These files and directories are copied from main worktree when creating a new worktree.")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            CopyPatternsEditor(patterns: $patterns, showHeader: false)

            Spacer()
        }
        .padding()
    }
}
