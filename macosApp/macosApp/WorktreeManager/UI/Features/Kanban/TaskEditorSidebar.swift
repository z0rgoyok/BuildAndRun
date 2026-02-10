import SwiftUI

struct TaskEditorSidebar: View {
    @State private var linkedWorktree: String? = "feature/MOB-6768"

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DS.Spacing.lg) {
                worktreeSection
                Divider()
                attachmentsSection
                linksSection
                relatedSection
            }
            .padding(DS.Spacing.lg)
        }
    }

    private var worktreeSection: some View {
        VStack(alignment: .leading, spacing: DS.Spacing.sm) {
            sectionHeader(icon: "arrow.triangle.branch", title: "Worktree")

            if let name = linkedWorktree {
                linkedWorktreeCard(name: name)
            } else {
                unlinkedWorktreeCard
            }
        }
    }

    private func linkedWorktreeCard(name: String) -> some View {
        VStack(alignment: .leading, spacing: DS.Spacing.sm) {
            HStack(spacing: DS.Spacing.sm) {
                Image(systemName: "arrow.triangle.branch")
                    .font(.system(size: 12))
                    .foregroundStyle(DS.Colors.statusInProgress)
                    .frame(width: 20)
                VStack(alignment: .leading, spacing: DS.Spacing.xxxs) {
                    Text(name)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(DS.Colors.textPrimary)
                        .lineLimit(1)
                    Text("~/dev/tools/BuildAndRun")
                        .font(.system(size: 10))
                        .foregroundStyle(DS.Colors.textTertiary)
                        .lineLimit(1)
                }
                Spacer()
                Button {
                    linkedWorktree = nil
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 10, weight: .medium))
                        .foregroundStyle(DS.Colors.textTertiary)
                }
                .buttonStyle(.plain)
                .help("Unlink worktree")
            }
            .padding(DS.Spacing.sm)
            .background(DS.Colors.cardBackground)
            .cornerRadius(DS.Radius.sm)
            .overlay(
                RoundedRectangle(cornerRadius: DS.Radius.sm)
                    .stroke(DS.Colors.borderSubtle, lineWidth: 1)
            )
        }
    }

    private var unlinkedWorktreeCard: some View {
        VStack(spacing: DS.Spacing.sm) {
            Menu {
                Button("feature/MOB-6768") { linkedWorktree = "feature/MOB-6768" }
                Button("fix/login-crash") { linkedWorktree = "fix/login-crash" }
                Button("chore/update-deps") { linkedWorktree = "chore/update-deps" }
                Divider()
                Button {} label: {
                    Label("Create new worktree", systemImage: "plus")
                }
            } label: {
                HStack(spacing: DS.Spacing.xs) {
                    Image(systemName: "arrow.triangle.branch")
                        .font(.system(size: 12))
                    Text("Link worktree")
                        .font(.system(size: 12))
                    Spacer()
                    Image(systemName: "chevron.down")
                        .font(.system(size: 9))
                }
                .foregroundStyle(DS.Colors.textSecondary)
                .padding(.horizontal, DS.Spacing.sm)
                .padding(.vertical, DS.Spacing.sm)
                .background(
                    RoundedRectangle(cornerRadius: DS.Radius.sm)
                        .stroke(style: StrokeStyle(lineWidth: 1, dash: [5, 3]))
                        .foregroundStyle(DS.Colors.borderSubtle)
                )
            }
            .buttonStyle(.plain)
        }
    }

    private var attachmentsSection: some View {
        VStack(alignment: .leading, spacing: DS.Spacing.sm) {
            sectionHeader(icon: "paperclip", title: "Attachments")

            Button {} label: {
                HStack(spacing: DS.Spacing.xs) {
                    Image(systemName: "plus.circle")
                        .font(.system(size: 12))
                    Text("Add file")
                        .font(.system(size: 12))
                }
                .foregroundStyle(DS.Colors.textSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, DS.Spacing.md)
                .background(
                    RoundedRectangle(cornerRadius: DS.Radius.sm)
                        .stroke(style: StrokeStyle(lineWidth: 1, dash: [5, 3]))
                        .foregroundStyle(DS.Colors.borderSubtle)
                )
            }
            .buttonStyle(.plain)

            Text("No attachments yet")
                .font(.system(size: 11))
                .foregroundStyle(DS.Colors.textTertiary)
        }
    }

    private var linksSection: some View {
        VStack(alignment: .leading, spacing: DS.Spacing.sm) {
            sectionHeader(icon: "link", title: "Links")

            demoLink(
                icon: "arrow.triangle.branch",
                title: "feature/MOB-6768",
                subtitle: "Git branch"
            )

            demoLink(
                icon: "globe",
                title: "Figma — Task Design",
                subtitle: "figma.com"
            )

            Button {} label: {
                HStack(spacing: DS.Spacing.xs) {
                    Image(systemName: "plus")
                        .font(.system(size: 11, weight: .medium))
                    Text("Add link")
                        .font(.system(size: 12))
                }
                .foregroundStyle(DS.Colors.textSecondary)
            }
            .buttonStyle(.plain)
        }
    }

    private var relatedSection: some View {
        VStack(alignment: .leading, spacing: DS.Spacing.sm) {
            sectionHeader(icon: "square.on.square", title: "Related")

            demoRelatedTask(title: "Setup CI pipeline", status: "Done")
            demoRelatedTask(title: "Write API tests", status: "In Progress")

            Button {} label: {
                HStack(spacing: DS.Spacing.xs) {
                    Image(systemName: "plus")
                        .font(.system(size: 11, weight: .medium))
                    Text("Add relation")
                        .font(.system(size: 12))
                }
                .foregroundStyle(DS.Colors.textSecondary)
            }
            .buttonStyle(.plain)
        }
    }

    private func sectionHeader(icon: String, title: String) -> some View {
        HStack(spacing: DS.Spacing.xs) {
            Image(systemName: icon)
                .font(.system(size: 11, weight: .medium))
                .foregroundStyle(DS.Colors.textSecondary)
            Text(title)
                .font(.system(size: 12, weight: .semibold))
                .foregroundStyle(DS.Colors.textSecondary)
                .textCase(.uppercase)
        }
    }

    private func demoLink(icon: String, title: String, subtitle: String) -> some View {
        HStack(spacing: DS.Spacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundStyle(DS.Colors.accent)
                .frame(width: 20)
            VStack(alignment: .leading, spacing: DS.Spacing.xxxs) {
                Text(title)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(DS.Colors.textPrimary)
                    .lineLimit(1)
                Text(subtitle)
                    .font(.system(size: 10))
                    .foregroundStyle(DS.Colors.textTertiary)
            }
            Spacer()
        }
        .padding(DS.Spacing.sm)
        .background(DS.Colors.cardBackground)
        .cornerRadius(DS.Radius.sm)
        .overlay(
            RoundedRectangle(cornerRadius: DS.Radius.sm)
                .stroke(DS.Colors.borderSubtle, lineWidth: 1)
        )
    }

    private func demoRelatedTask(title: String, status: String) -> some View {
        HStack(spacing: DS.Spacing.sm) {
            Circle()
                .fill(status == "Done" ? DS.Colors.statusDone : DS.Colors.statusInProgress)
                .frame(width: 8, height: 8)
            Text(title)
                .font(.system(size: 12))
                .foregroundStyle(DS.Colors.textPrimary)
                .lineLimit(1)
            Spacer()
            Text(status)
                .font(.system(size: 10, weight: .medium))
                .foregroundStyle(DS.Colors.textTertiary)
        }
        .padding(DS.Spacing.sm)
        .background(DS.Colors.cardBackground)
        .cornerRadius(DS.Radius.sm)
        .overlay(
            RoundedRectangle(cornerRadius: DS.Radius.sm)
                .stroke(DS.Colors.borderSubtle, lineWidth: 1)
        )
    }
}
