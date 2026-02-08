Owner: Worker C
Files ownership:
- macosApp/macosApp/WorktreeManager/Application/**
- macosApp/macosApp/WorktreeManager/Domain/**
- macosApp/macosApp/WorktreeManager/Infrastructure/**
- macosApp/macosApp/WorktreeManager/Presentation/**
- macosApp/macosApp/WorktreeManager/DecomposeKit/**
Goal:
- Remove non-UI Swift code from build path while keeping repository history copy in imported/ if needed.
- Ensure project still builds after UI bridge to KMP is ready.
Constraints:
- Do not edit UI files.
- You are not alone in the codebase; ignore concurrent edits by others without touching them.
Deliverables:
- Code changes in owned files.
- Write summary to .coord/port-sync/results/task-C.md
