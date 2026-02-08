import SwiftUI

struct CopyPatternsEditor: View {
    @Binding var patterns: [String]
    var showHeader: Bool = true

    @State private var newPattern = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if showHeader {
                Text("Files to Copy")
                    .font(.headline)
            }

            if patterns.isEmpty {
                Text("No patterns configured")
                    .foregroundStyle(.secondary)
                    .font(.subheadline)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 8)
            } else {
                ForEach(patterns, id: \.self) { pattern in
                    HStack {
                        Image(systemName: pattern.hasSuffix("/") ? "folder" : "doc")
                            .foregroundStyle(.secondary)
                            .frame(width: 20)

                        Text(pattern)
                            .font(.system(.body, design: .monospaced))

                        Spacer()

                        Button {
                            removePattern(pattern)
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(.secondary)
                        }
                        .buttonStyle(.plain)
                        .help("Remove pattern")
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(nsColor: .controlBackgroundColor))
                    .cornerRadius(6)
                }
            }

            HStack {
                TextField("Add pattern (e.g. .env, .venv/)", text: $newPattern)
                    .textFieldStyle(.roundedBorder)
                    .font(.system(.body, design: .monospaced))
                    .onSubmit {
                        addPattern()
                    }

                Button("Add") {
                    addPattern()
                }
                .disabled(newPattern.trimmingCharacters(in: .whitespaces).isEmpty)
            }

            Text("Use trailing / for directories (e.g. .venv/)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }

    private func addPattern() {
        let trimmed = newPattern.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }
        guard !patterns.contains(trimmed) else {
            newPattern = ""
            return
        }
        patterns.append(trimmed)
        newPattern = ""
    }

    private func removePattern(_ pattern: String) {
        patterns.removeAll { $0 == pattern }
    }
}
