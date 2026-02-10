import AppKit
import Shared
import SwiftUI

struct WorktreeDetailHeader: View {
    @EnvironmentObject var root: KmpRoot
    let worktree: WorktreeItem

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: DS.Spacing.lg) {
                VStack(alignment: .leading, spacing: DS.Spacing.sm) {
                    nameBadgesRow
                    branchPathRow
                    statusRow
                }

                Spacer()
            }
            .padding(DS.Spacing.lg)
            .background(.bar)

            Divider()
        }
    }

    private var nameBadgesRow: some View {
        HStack(spacing: DS.Spacing.sm) {
            Text(worktree.name)
                .font(.title2)
                .fontWeight(.semibold)

            if worktree.isMain {
                StatusBadge(text: labels.badgeMain, color: .blue)
            }
            if worktree.isLocked {
                StatusBadge(text: labels.badgeLocked, color: .orange)
            }
        }
    }

    private var branchPathRow: some View {
        HStack(spacing: DS.Spacing.lg) {
            HStack(spacing: DS.Spacing.xs) {
                Image(systemName: "arrow.triangle.branch")
                    .font(.caption)
                Text(worktree.branch)
                    .font(.subheadline)
            }
            .foregroundStyle(DS.Colors.textSecondary)

            HStack(spacing: DS.Spacing.xs) {
                Image(systemName: "folder")
                    .font(.caption)
                Text(worktree.path)
                    .font(.caption)
                    .lineLimit(1)
                    .truncationMode(.middle)

                Button {
                    NSPasteboard.general.clearContents()
                    NSPasteboard.general.setString(worktree.path, forType: .string)
                } label: {
                    Image(systemName: "doc.on.doc")
                        .font(.caption)
                }
                .buttonStyle(.plain)
                .help(labels.copyPath)
            }
            .foregroundStyle(DS.Colors.textTertiary)
        }
    }

    @ViewBuilder
    private var statusRow: some View {
        if !worktree.isPrunable {
            WorktreeStatusRow(status: worktree.status)
        }
    }
}
