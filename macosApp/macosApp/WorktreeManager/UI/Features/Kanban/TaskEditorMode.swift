import Shared

enum TaskEditorMode: Equatable {
    case creating(columnId: KanbanColumnType)
    case editing(taskId: String)

    static func == (lhs: TaskEditorMode, rhs: TaskEditorMode) -> Bool {
        switch (lhs, rhs) {
        case let (.creating(lCol), .creating(rCol)):
            return lCol === rCol
        case let (.editing(lId), .editing(rId)):
            return lId == rId
        default:
            return false
        }
    }
}
