import AppKit
import Shared
import SwiftUI

struct ContentView: View {
    @StateObject private var store = AppStore()
    @State private var expandedRepositoryIds: Set<String> = []
    @State private var activeSheet: ActiveSheet?

    var body: some View {
        NavigationSplitView {
            sidebar
        } detail: {
            detail
        }
        .frame(minWidth: 980, minHeight: 620)
        .toolbar { toolbar }
        .sheet(item: $activeSheet) { sheet in
            switch sheet {
            case .addRepository:
                AddRepositorySheet(
                    initialPath: store.addRepositoryPathInput,
                    title: store.text("sheet.add_repository.title"),
                    placeholder: store.text("sheet.add_repository.placeholder"),
                    addTitle: store.text("sheet.add_repository.submit"),
                    cancelTitle: store.text("common.cancel"),
                    onSubmit: { path in
                        store.onAddRepository(path: path)
                        activeSheet = nil
                    },
                    onCancel: {
                        activeSheet = nil
                    }
                )
            case .createWorktree:
                CreateWorktreeSheet(
                    form: store.createWorktreeForm,
                    title: store.text("sheet.create_worktree.title"),
                    branchTitle: store.text("sheet.create_worktree.branch"),
                    pathTitle: store.text("sheet.create_worktree.path"),
                    baseBranchTitle: store.text("sheet.create_worktree.base_branch"),
                    createBranchTitle: store.text("sheet.create_worktree.create_branch"),
                    submitTitle: store.text("sheet.create_worktree.submit"),
                    cancelTitle: store.text("common.cancel"),
                    successTitle: store.text("sheet.create_worktree.success"),
                    onBranchChanged: store.onCreateWorktreeBranchChanged,
                    onPathChanged: store.onCreateWorktreePathChanged,
                    onBaseBranchChanged: store.onCreateWorktreeBaseBranchChanged,
                    onCreateBranchChanged: store.onCreateWorktreeCreateBranchChanged,
                    onSubmit: store.onCreateWorktree,
                    onCancel: {
                        activeSheet = nil
                    }
                )
            case .addTask:
                AddTaskSheet(
                    title: store.text("sheet.add_task.title"),
                    taskTitle: store.text("sheet.add_task.task_title"),
                    taskDescription: store.text("sheet.add_task.task_description"),
                    submitTitle: store.text("sheet.add_task.submit"),
                    cancelTitle: store.text("common.cancel"),
                    defaultColumnId: "TODO",
                    columns: boardColumns,
                    onSubmit: { title, description, columnId in
                        store.onAddTask(title: title, description: description, columnId: columnId)
                        activeSheet = nil
                    },
                    onCancel: {
                        activeSheet = nil
                    }
                )
            }
        }
        .onChange(of: store.repositories) { _, repositories in
            let existingIds = Set(repositories.map { $0.id })
            expandedRepositoryIds = expandedRepositoryIds.intersection(existingIds)
            if let selectedRepositoryId = store.selectedRepositoryId {
                expandedRepositoryIds.insert(selectedRepositoryId)
            }
        }
    }

    private var sidebar: some View {
        VStack(spacing: 0) {
            HStack {
                Text(store.text("sidebar.title"))
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundStyle(Color.secondary)
                    .textCase(.uppercase)
                Spacer()
                Button {
                    activeSheet = .addRepository
                } label: {
                    Image(systemName: "plus")
                }
                .buttonStyle(.plain)
                .help(store.text("sidebar.add_repository"))
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)

            Divider()

            if store.repositories.isEmpty {
                VStack(spacing: 10) {
                    Spacer()
                    Image(systemName: "folder")
                        .font(.system(size: 34))
                        .foregroundStyle(Color.secondary)
                    Text(store.text("sidebar.empty"))
                        .foregroundStyle(Color.secondary)
                    Spacer()
                }
                .frame(maxWidth: .infinity)
            } else {
                ScrollView {
                    VStack(spacing: 0) {
                        ForEach(store.repositories) { repository in
                            RepositoryNodeView(
                                repository: repository,
                                isExpanded: expandedRepositoryIds.contains(repository.id),
                                selectedRepositoryId: store.selectedRepositoryId,
                                selectedWorktreePath: store.selectedWorktreePath,
                                onToggleExpanded: {
                                    if expandedRepositoryIds.contains(repository.id) {
                                        expandedRepositoryIds.remove(repository.id)
                                    } else {
                                        expandedRepositoryIds.insert(repository.id)
                                    }
                                },
                                onSelectRepository: {
                                    store.onSelectRepository(repository.id)
                                },
                                onSelectWorktree: { path in
                                    store.onSelectWorktree(path)
                                }
                            )
                        }
                    }
                    .padding(.vertical, 8)
                }
            }
        }
        .background(Color(nsColor: .windowBackgroundColor))
    }

    private var detail: some View {
        VStack(spacing: 0) {
            if let repository = store.selectedRepository {
                DetailHeaderView(
                    title: repository.name,
                    subtitle: repository.path,
                    selectedWorktree: store.selectedWorktree,
                    loading: store.isLoading,
                    errorBanner: store.errorBanner,
                    successBanner: store.successBanner,
                    dismissErrorTitle: store.text("common.dismiss"),
                    dismissSuccessTitle: store.text("common.dismiss"),
                    onDismissError: store.onDismissError,
                    onDismissSuccess: store.onDismissSuccess
                )

                HStack {
                    Text(store.text("board.title"))
                        .font(.system(size: 13, weight: .semibold))
                    Spacer()
                    Button {
                        activeSheet = .addTask
                    } label: {
                        Label(store.text("board.add_task"), systemImage: "plus")
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.small)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(nsColor: .windowBackgroundColor))

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(alignment: .top, spacing: 12) {
                        ForEach(boardColumns) { column in
                            BoardColumnView(
                                column: column,
                                tasks: store.tasks.filter { $0.columnId == column.id },
                                emptyText: store.text("board.empty_column"),
                                moveTitle: store.text("board.move"),
                                deleteTitle: store.text("board.delete"),
                                onMove: { taskId, nextColumnId in
                                    store.onMoveTask(taskId: taskId, columnId: nextColumnId)
                                },
                                onDelete: { taskId in
                                    store.onDeleteTask(taskId: taskId)
                                }
                            )
                        }
                    }
                    .padding(16)
                }
                .background(Color(nsColor: .underPageBackgroundColor))
            } else {
                VStack(spacing: 12) {
                    Image(systemName: "rectangle.stack.badge.plus")
                        .font(.system(size: 38))
                        .foregroundStyle(Color.secondary)
                    Text(store.text("board.empty_state"))
                        .font(.headline)
                        .foregroundStyle(Color.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(nsColor: .underPageBackgroundColor))
            }
        }
    }

    @ToolbarContentBuilder
    private var toolbar: some ToolbarContent {
        ToolbarItemGroup(placement: .primaryAction) {
            if store.selectedRepository != nil {
                Button {
                    activeSheet = .createWorktree
                } label: {
                    Label(store.text("toolbar.new_worktree"), systemImage: "plus.square.on.square")
                }

                if let selectedWorktree = store.selectedWorktree {
                    Button {
                        NSWorkspace.shared.selectFile(nil, inFileViewerRootedAtPath: selectedWorktree.path)
                    } label: {
                        Label(store.text("toolbar.finder"), systemImage: "folder")
                    }
                }

                Button {
                    store.onRefreshSelectedRepository()
                } label: {
                    Label(store.text("toolbar.refresh"), systemImage: "arrow.clockwise")
                }
            }
        }
    }
}

#Preview {
    ContentView()
}

private struct RepositoryNodeView: View {
    let repository: RepositoryViewState
    let isExpanded: Bool
    let selectedRepositoryId: String?
    let selectedWorktreePath: String?
    let onToggleExpanded: () -> Void
    let onSelectRepository: () -> Void
    let onSelectWorktree: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 8) {
                Button {
                    onToggleExpanded()
                } label: {
                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundStyle(Color.secondary)
                        .frame(width: 14, height: 14)
                }
                .buttonStyle(.plain)

                Image(systemName: "folder.fill")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(Color.blue)
                    .frame(width: 16, height: 16)

                VStack(alignment: .leading, spacing: 2) {
                    Text(repository.name)
                        .font(.system(size: 13, weight: .medium))
                        .lineLimit(1)
                    Text(repository.path)
                        .font(.system(size: 11))
                        .foregroundStyle(Color.secondary)
                        .lineLimit(1)
                        .truncationMode(.middle)
                }

                Spacer()

                if repository.worktrees.isEmpty == false {
                    Text("\(repository.worktrees.count)")
                        .font(.system(size: 10, weight: .medium))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(nsColor: .controlBackgroundColor))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(
                selectedRepositoryId == repository.id && selectedWorktreePath == nil
                    ? Color(nsColor: .selectedContentBackgroundColor)
                    : Color.clear
            )
            .clipShape(RoundedRectangle(cornerRadius: 6))
            .padding(.horizontal, 6)
            .contentShape(Rectangle())
            .onTapGesture {
                onSelectRepository()
            }

            if isExpanded {
                ForEach(repository.worktrees) { worktree in
                    HStack(spacing: 8) {
                        Color.clear
                            .frame(width: 18, height: 14)
                        Image(systemName: worktree.isMain ? "circle.fill" : "arrow.triangle.branch")
                            .font(.system(size: 10))
                            .foregroundStyle(worktree.isMain ? Color.green : Color.secondary)
                            .frame(width: 16, height: 16)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(worktree.name)
                                .font(.system(size: 12, weight: .regular))
                            Text(worktree.branch)
                                .font(.system(size: 10))
                                .foregroundStyle(Color.secondary)
                        }
                        Spacer()
                    }
                    .padding(.leading, 18)
                    .padding(.trailing, 10)
                    .padding(.vertical, 6)
                    .background(
                        selectedWorktreePath == worktree.path
                            ? Color(nsColor: .selectedContentBackgroundColor)
                            : Color.clear
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .padding(.horizontal, 6)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        onSelectRepository()
                        onSelectWorktree(worktree.path)
                    }
                }
            }
        }
    }
}

private struct DetailHeaderView: View {
    let title: String
    let subtitle: String
    let selectedWorktree: WorktreeViewState?
    let loading: Bool
    let errorBanner: BannerViewState?
    let successBanner: BannerViewState?
    let dismissErrorTitle: String
    let dismissSuccessTitle: String
    let onDismissError: () -> Void
    let onDismissSuccess: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.system(size: 24, weight: .semibold))
                    Text(subtitle)
                        .font(.system(size: 12))
                        .foregroundStyle(Color.secondary)
                        .lineLimit(1)
                        .truncationMode(.middle)
                    if let selectedWorktree {
                        Text("\(selectedWorktree.name) · \(selectedWorktree.branch)")
                            .font(.system(size: 12))
                            .foregroundStyle(Color.secondary)
                    }
                }
                Spacer()
                if loading {
                    ProgressView()
                        .controlSize(.small)
                }
            }

            if let errorBanner {
                InlineBanner(
                    message: errorBanner.message,
                    dismissTitle: dismissErrorTitle,
                    accent: Color.red.opacity(0.18),
                    icon: "exclamationmark.triangle.fill",
                    onDismiss: onDismissError
                )
            }

            if let successBanner {
                InlineBanner(
                    message: successBanner.message,
                    dismissTitle: dismissSuccessTitle,
                    accent: Color.green.opacity(0.18),
                    icon: "checkmark.circle.fill",
                    onDismiss: onDismissSuccess
                )
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color(nsColor: .windowBackgroundColor))
    }
}

private struct InlineBanner: View {
    let message: String
    let dismissTitle: String
    let accent: Color
    let icon: String
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .foregroundStyle(Color.primary)
            Text(message)
                .font(.system(size: 12))
                .lineLimit(3)
            Spacer()
            Button(dismissTitle, action: onDismiss)
                .buttonStyle(.bordered)
                .controlSize(.small)
        }
        .padding(10)
        .background(accent)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct BoardColumnView: View {
    let column: BoardColumn
    let tasks: [TaskViewState]
    let emptyText: String
    let moveTitle: String
    let deleteTitle: String
    let onMove: (String, String) -> Void
    let onDelete: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 8) {
                Image(systemName: column.icon)
                    .font(.system(size: 11))
                    .foregroundStyle(column.color)
                Text(column.title)
                    .font(.system(size: 12, weight: .semibold))
                Spacer()
                Text("\(tasks.count)")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(Color.secondary)
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 10)

            Divider()

            ScrollView {
                VStack(spacing: 8) {
                    if tasks.isEmpty {
                        Text(emptyText)
                            .font(.system(size: 12))
                            .foregroundStyle(Color.secondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 8)
                    } else {
                        ForEach(tasks) { task in
                            VStack(alignment: .leading, spacing: 6) {
                                Text(task.title)
                                    .font(.system(size: 13, weight: .medium))
                                if let description = task.description {
                                    Text(description)
                                        .font(.system(size: 11))
                                        .foregroundStyle(Color.secondary)
                                }
                                Menu {
                                    ForEach(boardColumns.filter { $0.id != column.id }) { nextColumn in
                                        Button("\(moveTitle): \(nextColumn.title)") {
                                            onMove(task.id, nextColumn.id)
                                        }
                                    }
                                    Divider()
                                    Button(deleteTitle, role: .destructive) {
                                        onDelete(task.id)
                                    }
                                } label: {
                                    HStack(spacing: 6) {
                                        Image(systemName: "ellipsis.circle")
                                        Text(moveTitle)
                                    }
                                    .font(.system(size: 11))
                                    .foregroundStyle(Color.secondary)
                                }
                                .menuStyle(.borderlessButton)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(10)
                            .background(Color(nsColor: .controlBackgroundColor))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color(nsColor: .separatorColor).opacity(0.5), lineWidth: 1)
                            )
                        }
                    }
                }
                .padding(10)
            }
        }
        .frame(minWidth: 280, maxWidth: 340)
        .background(Color(nsColor: .controlBackgroundColor).opacity(0.45))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct AddRepositorySheet: View {
    @State private var path: String
    let title: String
    let placeholder: String
    let addTitle: String
    let cancelTitle: String
    let onSubmit: (String) -> Void
    let onCancel: () -> Void

    init(
        initialPath: String,
        title: String,
        placeholder: String,
        addTitle: String,
        cancelTitle: String,
        onSubmit: @escaping (String) -> Void,
        onCancel: @escaping () -> Void,
    ) {
        _path = State(initialValue: initialPath)
        self.title = title
        self.placeholder = placeholder
        self.addTitle = addTitle
        self.cancelTitle = cancelTitle
        self.onSubmit = onSubmit
        self.onCancel = onCancel
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(title)
                .font(.headline)

            TextField(placeholder, text: $path)
                .textFieldStyle(.roundedBorder)

            HStack {
                Button(cancelTitle, action: onCancel)
                Spacer()
                Button(addTitle) {
                    onSubmit(path)
                }
                .disabled(path.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                .keyboardShortcut(.defaultAction)
            }
        }
        .padding(18)
        .frame(width: 420)
    }
}

private struct CreateWorktreeSheet: View {
    let form: CreateWorktreeFormState
    let title: String
    let branchTitle: String
    let pathTitle: String
    let baseBranchTitle: String
    let createBranchTitle: String
    let submitTitle: String
    let cancelTitle: String
    let successTitle: String
    let onBranchChanged: (String) -> Void
    let onPathChanged: (String) -> Void
    let onBaseBranchChanged: (String) -> Void
    let onCreateBranchChanged: (Bool) -> Void
    let onSubmit: () -> Void
    let onCancel: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(title)
                .font(.headline)

            TextField(branchTitle, text: Binding(get: { form.branchInput }, set: onBranchChanged))
                .textFieldStyle(.roundedBorder)
                .disabled(form.isSubmitting)

            TextField(pathTitle, text: Binding(get: { form.worktreePathInput }, set: onPathChanged))
                .textFieldStyle(.roundedBorder)
                .disabled(form.isSubmitting)

            TextField(baseBranchTitle, text: Binding(get: { form.baseBranchInput }, set: onBaseBranchChanged))
                .textFieldStyle(.roundedBorder)
                .disabled(form.isSubmitting)

            Toggle(createBranchTitle, isOn: Binding(get: { form.createBranch }, set: onCreateBranchChanged))
                .disabled(form.isSubmitting)

            if let createdWorktreePath = form.createdWorktreePath {
                VStack(alignment: .leading, spacing: 4) {
                    Text(successTitle)
                        .font(.system(size: 12, weight: .semibold))
                    Text(createdWorktreePath)
                        .font(.system(size: 11))
                        .foregroundStyle(Color.secondary)
                }
            }

            HStack {
                Button(cancelTitle, action: onCancel)
                Spacer()
                if form.isSubmitting {
                    ProgressView()
                        .controlSize(.small)
                }
                Button(submitTitle, action: onSubmit)
                    .disabled(
                        form.isSubmitting ||
                        form.branchInput.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
                        form.worktreePathInput.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    )
                    .keyboardShortcut(.defaultAction)
            }
        }
        .padding(18)
        .frame(width: 460)
    }
}

private struct AddTaskSheet: View {
    @State private var titleValue: String = ""
    @State private var descriptionValue: String = ""
    @State private var selectedColumnId: String
    let title: String
    let taskTitle: String
    let taskDescription: String
    let submitTitle: String
    let cancelTitle: String
    let columns: [BoardColumn]
    let onSubmit: (String, String?, String) -> Void
    let onCancel: () -> Void

    init(
        title: String,
        taskTitle: String,
        taskDescription: String,
        submitTitle: String,
        cancelTitle: String,
        defaultColumnId: String,
        columns: [BoardColumn],
        onSubmit: @escaping (String, String?, String) -> Void,
        onCancel: @escaping () -> Void,
    ) {
        _selectedColumnId = State(initialValue: defaultColumnId)
        self.title = title
        self.taskTitle = taskTitle
        self.taskDescription = taskDescription
        self.submitTitle = submitTitle
        self.cancelTitle = cancelTitle
        self.columns = columns
        self.onSubmit = onSubmit
        self.onCancel = onCancel
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(title)
                .font(.headline)

            TextField(taskTitle, text: $titleValue)
                .textFieldStyle(.roundedBorder)

            TextField(taskDescription, text: $descriptionValue)
                .textFieldStyle(.roundedBorder)

            Picker("", selection: $selectedColumnId) {
                ForEach(columns) { column in
                    Text(column.title).tag(column.id)
                }
            }
            .pickerStyle(.segmented)

            HStack {
                Button(cancelTitle, action: onCancel)
                Spacer()
                Button(submitTitle) {
                    onSubmit(
                        titleValue,
                        descriptionValue.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? nil : descriptionValue,
                        selectedColumnId
                    )
                }
                .disabled(titleValue.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                .keyboardShortcut(.defaultAction)
            }
        }
        .padding(18)
        .frame(width: 420)
    }
}

private final class AppStore: ObservableObject {
    @Published private(set) var repositories: [RepositoryViewState] = []
    @Published private(set) var selectedRepositoryId: String?
    @Published private(set) var selectedWorktreePath: String?
    @Published private(set) var createWorktreeForm = CreateWorktreeFormState()
    @Published private(set) var tasks: [TaskViewState] = []
    @Published private(set) var isLoading: Bool = false
    @Published private(set) var errorBanner: BannerViewState?
    @Published private(set) var successBanner: BannerViewState?
    @Published private(set) var addRepositoryPathInput: String = ""

    private let backend = MacOSAppStore()
    private let localizer = Localizer()
    private var stateCancellation: DecomposeCancellation?

    init() {
        stateCancellation =
            backend.state.subscribe { [weak self] state in
                self?.runOnMain {
                    self?.apply(state)
                }
            }
        apply(backend.state.value)
    }

    deinit {
        stateCancellation?.cancel()
        backend.destroy()
    }

    var selectedRepository: RepositoryViewState? {
        repositories.first { $0.id == selectedRepositoryId }
    }

    var selectedWorktree: WorktreeViewState? {
        selectedRepository?.worktrees.first { $0.path == selectedWorktreePath }
    }

    func text(_ key: String, args: [String] = []) -> String {
        localizer.string(key, args: args)
    }

    func onAddRepository(path: String) {
        backend.onAddRepositoryPathChanged(value: path)
        backend.onAddRepository()
    }

    func onSelectRepository(_ id: String) {
        backend.onSelectRepository(repositoryId: id)
    }

    func onSelectWorktree(_ path: String?) {
        backend.onSelectWorktree(worktreePath: path)
    }

    func onRefreshSelectedRepository() {
        backend.onRefreshSelectedRepository()
    }

    func onCreateWorktreeBranchChanged(_ value: String) {
        backend.onCreateWorktreeBranchChanged(value: value)
    }

    func onCreateWorktreePathChanged(_ value: String) {
        backend.onCreateWorktreePathChanged(value: value)
    }

    func onCreateWorktreeBaseBranchChanged(_ value: String) {
        backend.onCreateWorktreeBaseBranchChanged(value: value)
    }

    func onCreateWorktreeCreateBranchChanged(_ value: Bool) {
        backend.onCreateWorktreeCreateBranchChanged(value: value)
    }

    func onCreateWorktree() {
        backend.onCreateWorktree()
    }

    func onAddTask(title: String, description: String?, columnId: String) {
        backend.onAddTask(title: title, description: description, columnId: columnId)
    }

    func onMoveTask(taskId: String, columnId: String) {
        backend.onMoveTask(taskId: taskId, columnId: columnId)
    }

    func onDeleteTask(taskId: String) {
        backend.onDeleteTask(taskId: taskId)
    }

    func onDismissError() {
        backend.onDismissError()
    }

    func onDismissSuccess() {
        backend.onDismissSuccess()
    }

    private func apply(_ state: MacOSAppStore.State) {
        repositories =
            state.repositories.map {
                RepositoryViewState(
                    id: $0.id,
                    name: $0.name,
                    path: $0.path,
                    worktrees:
                        $0.worktrees.map {
                            WorktreeViewState(
                                path: $0.path,
                                name: $0.name,
                                branch: $0.branch,
                                isMain: $0.isMain,
                                isLocked: $0.isLocked,
                                isPrunable: $0.isPrunable
                            )
                        }
                )
            }
        selectedRepositoryId = state.selectedRepositoryId
        selectedWorktreePath = state.selectedWorktreePath
        addRepositoryPathInput = state.addRepositoryPathInput
        createWorktreeForm =
            CreateWorktreeFormState(
                repositoryPath: state.createWorktree.repositoryPath,
                branchInput: state.createWorktree.branchInput,
                worktreePathInput: state.createWorktree.worktreePathInput,
                baseBranchInput: state.createWorktree.baseBranchInput,
                createBranch: state.createWorktree.createBranch,
                isSubmitting: state.createWorktree.isSubmitting,
                createdWorktreePath: state.createWorktree.createdWorktreePath
            )
        tasks =
            state.kanbanTasks.map {
                TaskViewState(
                    id: $0.id,
                    title: $0.title,
                    description: $0.description,
                    columnId: $0.columnId,
                    order: Int($0.order)
                )
            }
        isLoading = state.isLoading
        errorBanner = state.error.map {
            BannerViewState(
                code: $0.code,
                message: resolveMessage($0.message, details: $0.details)
            )
        }
        successBanner = state.success.map {
            BannerViewState(
                code: "success",
                message: localizer.resolve($0.message)
            )
        }
    }

    private func resolveMessage(_ message: MacOSAppStore.MessageText, details: MacOSAppStore.MessageText?) -> String {
        let main = localizer.resolve(message)
        guard let details else {
            return main
        }
        return "\(main)\n\(localizer.resolve(details))"
    }

    private func runOnMain(_ action: @escaping () -> Void) {
        if Thread.isMainThread {
            action()
            return
        }
        DispatchQueue.main.async(execute: action)
    }
}

private struct Localizer {
    private let language: Language

    init() {
        let localeCode = Locale.preferredLanguages.first ?? "en"
        if localeCode.lowercased().hasPrefix("ru") {
            language = .ru
        } else if localeCode.lowercased().hasPrefix("uk") {
            language = .uk
        } else {
            language = .en
        }
    }

    func string(_ key: String, args: [String] = []) -> String {
        let template =
            dictionary[language]?[key] ??
            dictionary[.en]?[key] ??
            key
        return interpolate(template: template, args: args)
    }

    func resolve(_ text: MacOSAppStore.MessageText) -> String {
        string(text.key, args: text.args)
    }

    private func interpolate(template: String, args: [String]) -> String {
        var value = template
        for (index, argument) in args.enumerated() {
            value = value.replacingOccurrences(of: "{\(index)}", with: argument)
        }
        if value == template && args.isEmpty == false {
            return "\(template): \(args.joined(separator: ", "))"
        }
        return value
    }

    private enum Language {
        case en
        case ru
        case uk
    }

    private let dictionary: [Language: [String: String]] = [
        .en: [
            "sidebar.title": "Projects",
            "sidebar.empty": "Add a repository to get started",
            "sidebar.add_repository": "Add Repository",
            "toolbar.new_worktree": "New Worktree",
            "toolbar.finder": "Finder",
            "toolbar.refresh": "Refresh",
            "board.title": "Tasks",
            "board.add_task": "Add Task",
            "board.empty_state": "Select a repository to open workspace",
            "board.empty_column": "No tasks",
            "board.move": "Move",
            "board.delete": "Delete",
            "sheet.add_repository.title": "Add Repository",
            "sheet.add_repository.placeholder": "Path to git repository",
            "sheet.add_repository.submit": "Add",
            "sheet.create_worktree.title": "Create Worktree",
            "sheet.create_worktree.branch": "Branch name",
            "sheet.create_worktree.path": "Worktree path",
            "sheet.create_worktree.base_branch": "Base branch",
            "sheet.create_worktree.create_branch": "Create branch",
            "sheet.create_worktree.submit": "Create",
            "sheet.create_worktree.success": "Worktree created",
            "sheet.add_task.title": "Add Task",
            "sheet.add_task.task_title": "Task title",
            "sheet.add_task.task_description": "Task description",
            "sheet.add_task.submit": "Create Task",
            "common.cancel": "Cancel",
            "common.dismiss": "Dismiss",
            "screen.repositories.repository_added": "Repository added: {0}",
            "screen.create_worktree.success": "Created: {0}",
            "app.validation.task_title_blank": "Task title is required.",
            "app.validation.repository_path_blank": "Repository path is required.",
            "app.validation.branch_blank": "Branch name is required.",
            "app.validation.worktree_path_blank": "Worktree path is required.",
            "app.repository_already_added": "Repository is already added.",
            "git.not_a_repository": "Path is not a git repository: {0}.",
            "git.worktree_already_exists": "Worktree already exists: {0}.",
            "git.branch_already_exists": "Branch already exists: {0}.",
            "git.branch_not_found": "Branch not found: {0}.",
            "git.invalid_path": "Invalid path: {0}.",
            "git.command_failed": "Git command failed.",
            "error.details": "Details: {0}",
            "app.unknown": "Unexpected error."
        ],
        .ru: [
            "sidebar.title": "Projects",
            "sidebar.empty": "Добавьте репозиторий для начала",
            "sidebar.add_repository": "Добавить репозиторий",
            "toolbar.new_worktree": "Новый worktree",
            "toolbar.finder": "Finder",
            "toolbar.refresh": "Обновить",
            "board.title": "Tasks",
            "board.add_task": "Add Task",
            "board.empty_state": "Выберите репозиторий, чтобы открыть рабочее пространство",
            "board.empty_column": "Нет задач",
            "board.move": "Переместить",
            "board.delete": "Удалить",
            "sheet.add_repository.title": "Добавить репозиторий",
            "sheet.add_repository.placeholder": "Путь к git-репозиторию",
            "sheet.add_repository.submit": "Добавить",
            "sheet.create_worktree.title": "Создать worktree",
            "sheet.create_worktree.branch": "Имя ветки",
            "sheet.create_worktree.path": "Путь к worktree",
            "sheet.create_worktree.base_branch": "Базовая ветка",
            "sheet.create_worktree.create_branch": "Создать ветку",
            "sheet.create_worktree.submit": "Создать",
            "sheet.create_worktree.success": "Worktree создан",
            "sheet.add_task.title": "Добавить задачу",
            "sheet.add_task.task_title": "Название задачи",
            "sheet.add_task.task_description": "Описание задачи",
            "sheet.add_task.submit": "Создать задачу",
            "common.cancel": "Отмена",
            "common.dismiss": "Закрыть",
            "screen.repositories.repository_added": "Репозиторий добавлен: {0}",
            "screen.create_worktree.success": "Создано: {0}",
            "app.validation.task_title_blank": "Укажите название задачи.",
            "app.validation.repository_path_blank": "Укажите путь к репозиторию.",
            "app.validation.branch_blank": "Укажите имя ветки.",
            "app.validation.worktree_path_blank": "Укажите путь к worktree.",
            "app.repository_already_added": "Репозиторий уже добавлен.",
            "git.not_a_repository": "Путь не является git-репозиторием: {0}.",
            "git.worktree_already_exists": "Worktree уже существует: {0}.",
            "git.branch_already_exists": "Ветка уже существует: {0}.",
            "git.branch_not_found": "Ветка не найдена: {0}.",
            "git.invalid_path": "Некорректный путь: {0}.",
            "git.command_failed": "Ошибка выполнения команды git.",
            "error.details": "Детали: {0}",
            "app.unknown": "Неожиданная ошибка."
        ],
        .uk: [
            "sidebar.title": "Projects",
            "sidebar.empty": "Додайте репозиторій для початку",
            "sidebar.add_repository": "Додати репозиторій",
            "toolbar.new_worktree": "Новий worktree",
            "toolbar.finder": "Finder",
            "toolbar.refresh": "Оновити",
            "board.title": "Tasks",
            "board.add_task": "Add Task",
            "board.empty_state": "Оберіть репозиторій, щоб відкрити робочий простір",
            "board.empty_column": "Немає задач",
            "board.move": "Перемістити",
            "board.delete": "Видалити",
            "sheet.add_repository.title": "Додати репозиторій",
            "sheet.add_repository.placeholder": "Шлях до git-репозиторію",
            "sheet.add_repository.submit": "Додати",
            "sheet.create_worktree.title": "Створити worktree",
            "sheet.create_worktree.branch": "Назва гілки",
            "sheet.create_worktree.path": "Шлях до worktree",
            "sheet.create_worktree.base_branch": "Базова гілка",
            "sheet.create_worktree.create_branch": "Створити гілку",
            "sheet.create_worktree.submit": "Створити",
            "sheet.create_worktree.success": "Worktree створено",
            "sheet.add_task.title": "Додати задачу",
            "sheet.add_task.task_title": "Назва задачі",
            "sheet.add_task.task_description": "Опис задачі",
            "sheet.add_task.submit": "Створити задачу",
            "common.cancel": "Скасувати",
            "common.dismiss": "Закрити",
            "screen.repositories.repository_added": "Репозиторій додано: {0}",
            "screen.create_worktree.success": "Створено: {0}",
            "app.validation.task_title_blank": "Вкажіть назву задачі.",
            "app.validation.repository_path_blank": "Вкажіть шлях до репозиторію.",
            "app.validation.branch_blank": "Вкажіть назву гілки.",
            "app.validation.worktree_path_blank": "Вкажіть шлях до worktree.",
            "app.repository_already_added": "Репозиторій уже додано.",
            "git.not_a_repository": "Шлях не є git-репозиторієм: {0}.",
            "git.worktree_already_exists": "Worktree вже існує: {0}.",
            "git.branch_already_exists": "Гілка вже існує: {0}.",
            "git.branch_not_found": "Гілку не знайдено: {0}.",
            "git.invalid_path": "Некоректний шлях: {0}.",
            "git.command_failed": "Помилка виконання команди git.",
            "error.details": "Деталі: {0}",
            "app.unknown": "Неочікувана помилка."
        ]
    ]
}

private struct RepositoryViewState: Identifiable, Equatable {
    let id: String
    let name: String
    let path: String
    let worktrees: [WorktreeViewState]
}

private struct WorktreeViewState: Identifiable, Equatable {
    var id: String { path }
    let path: String
    let name: String
    let branch: String
    let isMain: Bool
    let isLocked: Bool
    let isPrunable: Bool
}

private struct TaskViewState: Identifiable, Equatable {
    let id: String
    let title: String
    let description: String?
    let columnId: String
    let order: Int
}

private struct CreateWorktreeFormState {
    let repositoryPath: String
    let branchInput: String
    let worktreePathInput: String
    let baseBranchInput: String
    let createBranch: Bool
    let isSubmitting: Bool
    let createdWorktreePath: String?

    init(
        repositoryPath: String = "",
        branchInput: String = "",
        worktreePathInput: String = "",
        baseBranchInput: String = "",
        createBranch: Bool = true,
        isSubmitting: Bool = false,
        createdWorktreePath: String? = nil
    ) {
        self.repositoryPath = repositoryPath
        self.branchInput = branchInput
        self.worktreePathInput = worktreePathInput
        self.baseBranchInput = baseBranchInput
        self.createBranch = createBranch
        self.isSubmitting = isSubmitting
        self.createdWorktreePath = createdWorktreePath
    }
}

private struct BannerViewState {
    let code: String
    let message: String
}

private struct BoardColumn: Identifiable {
    let id: String
    let title: String
    let icon: String
    let color: Color
}

private let boardColumns: [BoardColumn] = [
    BoardColumn(id: "TODO", title: "To Do", icon: "circle", color: .gray),
    BoardColumn(id: "IN_PROGRESS", title: "In Progress", icon: "play.circle.fill", color: .blue),
    BoardColumn(id: "REVIEW", title: "Review", icon: "eye", color: .orange),
    BoardColumn(id: "DONE", title: "Done", icon: "checkmark.circle.fill", color: .green)
]

private enum ActiveSheet: Identifiable {
    case addRepository
    case createWorktree
    case addTask

    var id: String {
        switch self {
        case .addRepository:
            return "add_repository"
        case .createWorktree:
            return "create_worktree"
        case .addTask:
            return "add_task"
        }
    }
}
