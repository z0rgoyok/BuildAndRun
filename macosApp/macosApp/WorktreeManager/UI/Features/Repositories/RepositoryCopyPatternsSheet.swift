import Shared
import SwiftUI

struct RepositoryCopyPatternsSheet: View {
    @EnvironmentObject var root: KmpRoot
    @Environment(\.dismiss) var dismiss

    let repositoryId: String
    let repositoryName: String

    @State private var useCustomPatterns = false
    @State private var patterns: [String] = []

    var body: some View {
        VStack(spacing: 20) {
            HStack {
                Text("Copy Files Settings")
                    .font(.headline)
                Spacer()
                Text(repositoryName)
                    .foregroundStyle(.secondary)
            }

            Toggle("Use custom patterns for this repository", isOn: $useCustomPatterns)
                .onChange(of: useCustomPatterns) { _, newValue in
                    if !newValue {
                        patterns = root.state.defaultCopyPatterns
                    }
                }

            if useCustomPatterns {
                CopyPatternsEditor(patterns: $patterns, showHeader: false)
            } else {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Using global defaults:")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    if root.state.defaultCopyPatterns.isEmpty {
                        Text("No default patterns configured")
                            .foregroundStyle(.tertiary)
                            .font(.subheadline)
                    } else {
                        ForEach(root.state.defaultCopyPatterns, id: \.self) { pattern in
                            HStack {
                                Image(systemName: pattern.hasSuffix("/") ? "folder" : "doc")
                                    .foregroundStyle(.secondary)
                                Text(pattern)
                                    .font(.system(.body, design: .monospaced))
                            }
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                .background(Color(nsColor: .controlBackgroundColor))
                .cornerRadius(8)
            }

            Spacer()

            HStack {
                Button("Cancel") {
                    dismiss()
                }
                .keyboardShortcut(.cancelAction)

                Spacer()

                Button("Save") {
                    save()
                    dismiss()
                }
                .keyboardShortcut(.defaultAction)
            }
        }
        .padding()
        .frame(width: 420, height: 420)
        .onAppear {
            load()
        }
    }

    private func load() {
        guard root.state.selectedRepositoryId == repositoryId else {
            useCustomPatterns = false
            patterns = root.state.defaultCopyPatterns
            return
        }
        patterns = root.state.selectedRepositoryEffectiveCopyPatterns
        useCustomPatterns = root.state.selectedRepositoryCustomCopyPatterns != nil
    }

    private func save() {
        guard root.state.selectedRepositoryId == repositoryId else {
            return
        }
        if useCustomPatterns {
            root.store.onSetRepositoryCopyPatterns(patterns: patterns)
        } else {
            root.store.onSetRepositoryCopyPatterns(patterns: nil)
        }
    }
}
