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
                        patterns = root.settingsState.defaultCopyPatterns
                    }
                }

            if useCustomPatterns {
                CopyPatternsEditor(patterns: $patterns, showHeader: false)
            } else {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Using global defaults:")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    if root.settingsState.defaultCopyPatterns.isEmpty {
                        Text("No default patterns configured")
                            .foregroundStyle(.tertiary)
                            .font(.subheadline)
                    } else {
                        ForEach(root.settingsState.defaultCopyPatterns, id: \.self) { pattern in
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
                    if save() {
                        dismiss()
                    }
                }
                .keyboardShortcut(.defaultAction)
            }
        }
        .padding()
        .frame(width: 420, height: 420)
        .onAppear {
            load()
        }
        .onChange(of: root.repositoriesState.selectedRepositoryId) { _, _ in
            load()
        }
    }

    private func load() {
        if root.repositoriesState.selectedRepositoryId != repositoryId {
            root.store.repositories.onSelectRepository(repositoryId: repositoryId)
            return
        }
        patterns = root.settingsState.selectedRepositoryEffectiveCopyPatterns
        useCustomPatterns = root.settingsState.selectedRepositoryCustomCopyPatterns != nil
    }

    private func save() -> Bool {
        guard root.repositoriesState.selectedRepositoryId == repositoryId else {
            root.store.repositories.onSelectRepository(repositoryId: repositoryId)
            return false
        }
        if useCustomPatterns {
            root.store.settings.onSetRepositoryCopyPatterns(patterns: patterns)
        } else {
            root.store.settings.onSetRepositoryCopyPatterns(patterns: nil)
        }
        return true
    }
}
