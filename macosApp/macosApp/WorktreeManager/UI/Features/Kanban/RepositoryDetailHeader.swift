import Shared
import SwiftUI

struct RepositoryDetailHeader: View {
    @EnvironmentObject var root: KmpRoot
    let repository: AppStore.RepositoryItem

    private var labels: KanbanLabels { root.store.kanbanLabels }

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: DS.Spacing.sm) {
                    HStack(spacing: DS.Spacing.sm) {
                        Image(systemName: "folder.fill")
                            .font(.title2)
                            .foregroundStyle(.blue)

                        Text(repository.name)
                            .font(.title2)
                            .fontWeight(.semibold)
                    }

                    HStack(spacing: DS.Spacing.lg) {
                        Text(repository.path)
                            .font(.subheadline)
                            .foregroundStyle(DS.Colors.textSecondary)
                            .lineLimit(1)
                            .truncationMode(.middle)

                        Text("\(labels.worktrees): \(repository.worktrees.count)")
                            .font(.caption)
                            .foregroundStyle(DS.Colors.textTertiary)
                    }

                    RepositoryStatusPlaceholderRow()
                }

                Spacer()
            }
            .padding(DS.Spacing.lg)
            .background(.bar)

            Divider()
        }
    }
}
