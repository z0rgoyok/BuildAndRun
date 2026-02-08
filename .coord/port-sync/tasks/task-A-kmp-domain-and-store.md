Owner: Worker A
Files ownership:
- shared/src/commonMain/kotlin/app/tich/buildandrun/domain/**
- shared/src/commonMain/kotlin/app/tich/buildandrun/presentation/**
- shared/src/macosMain/kotlin/app/tich/buildandrun/macos/**
Goal:
- Replace Swift non-UI app/domain/presentation logic with KMP equivalents.
- Ensure actions/state cover full app parity: repositories, worktrees, status, PR workflow, settings, editor actions, help-related state where needed.
Constraints:
- Do not edit Swift UI files.
- You are not alone in the codebase; ignore concurrent edits by others without touching them.
Deliverables:
- Code changes in owned files.
- Write summary and TODOs to .coord/port-sync/results/task-A.md
