Owner: Worker B
Files ownership:
- macosApp/macosApp/WorktreeManager/UI/**
- macosApp/macosApp/WorktreeManager/App/**
Goal:
- Keep Swift only in UI layer.
- Replace dependencies on Swift non-UI types (RootComponent/AppStore/Domain models) with KMP bridge types from Shared framework.
- Preserve current UI appearance and behavior.
Constraints:
- Do not edit shared Kotlin files.
- You are not alone in the codebase; ignore concurrent edits by others without touching them.
Deliverables:
- Code changes in owned files.
- Write summary and blocking points to .coord/port-sync/results/task-B.md
