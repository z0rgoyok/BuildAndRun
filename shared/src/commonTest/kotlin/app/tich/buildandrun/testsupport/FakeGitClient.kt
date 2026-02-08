package app.tich.buildandrun.testsupport

import app.tich.buildandrun.domain.entities.PRState
import app.tich.buildandrun.domain.entities.PRStatus
import app.tich.buildandrun.domain.entities.Worktree
import app.tich.buildandrun.domain.entities.WorktreeStatus
import app.tich.buildandrun.domain.errors.GitError
import app.tich.buildandrun.domain.ports.GitClient

class FakeGitClient : GitClient {
    private val repositoryRootsByPath = mutableMapOf<String, String>()
    private val branchesByRepository = mutableMapOf<String, MutableSet<String>>()
    private val remoteBranchesByRepository = mutableMapOf<String, MutableSet<String>>()
    private val worktreesByRepository = mutableMapOf<String, MutableList<Worktree>>()

    fun registerRepository(
        path: String,
        rootPath: String = path,
    ) {
        val normalizedRootPath = normalize(path = rootPath)
        repositoryRootsByPath[normalize(path)] = normalizedRootPath
        branchesByRepository.getOrPut(normalizedRootPath) { mutableSetOf("main") }
        remoteBranchesByRepository.getOrPut(normalizedRootPath) { mutableSetOf() }
        worktreesByRepository.getOrPut(normalizedRootPath) { mutableListOf() }
    }

    fun setWorktrees(
        repositoryPath: String,
        worktrees: List<Worktree>,
    ) {
        worktreesByRepository[normalize(repositoryPath)] = worktrees.toMutableList()
    }

    override suspend fun getRepositoryRoot(atPath: String): String =
        repositoryRootsByPath[normalize(atPath)] ?: throw GitError.NotARepository(path = atPath)

    override suspend fun listBranches(atRepoPath: String): List<String> =
        branchesByRepository[normalize(atRepoPath)]?.toList() ?: emptyList()

    override suspend fun branchExists(
        atRepoPath: String,
        branch: String,
    ): Boolean = branchesByRepository[normalize(atRepoPath)]?.contains(branch) == true

    override suspend fun deleteBranch(
        atRepoPath: String,
        branch: String,
        force: Boolean,
    ) {
        val branches = branchesByRepository[normalize(atRepoPath)] ?: return
        if (!branches.remove(branch)) {
            throw GitError.BranchNotFound(name = branch)
        }
    }

    override suspend fun listWorktrees(atRepoPath: String): List<Worktree> =
        worktreesByRepository[normalize(atRepoPath)]?.toList() ?: emptyList()

    override suspend fun createWorktree(
        atRepoPath: String,
        worktreePath: String,
        branch: String,
        createBranch: Boolean,
        baseBranch: String?,
    ) {
        val normalizedRepoPath = normalize(atRepoPath)
        val normalizedWorktreePath = normalize(worktreePath)
        val worktrees = worktreesByRepository.getOrPut(normalizedRepoPath) { mutableListOf() }
        if (worktrees.any { normalize(it.path) == normalizedWorktreePath }) {
            throw GitError.WorktreeAlreadyExists(name = normalizedWorktreePath.substringAfterLast('/'))
        }

        val branches = branchesByRepository.getOrPut(normalizedRepoPath) { mutableSetOf("main") }
        val branchExists = branches.contains(branch)
        if (branchExists && createBranch) {
            throw GitError.BranchAlreadyExists(name = branch)
        }
        if (!branchExists) {
            branches.add(branch)
        }

        worktrees +=
            Worktree(
                path = normalizedWorktreePath,
                branch = branch,
                isMain = false,
                commitHash = null,
                isLocked = false,
                isPrunable = false,
                baseBranch = baseBranch,
            )
    }

    override suspend fun removeWorktree(
        atRepoPath: String,
        worktreePath: String,
        force: Boolean,
    ) {
        val worktrees =
            worktreesByRepository[normalize(atRepoPath)]
                ?: throw GitError.WorktreeNotFound(path = worktreePath)
        val index = worktrees.indexOfFirst { normalize(it.path) == normalize(worktreePath) }
        if (index == -1) {
            throw GitError.WorktreeNotFound(path = worktreePath)
        }
        worktrees.removeAt(index)
    }

    override suspend fun lockWorktree(
        atRepoPath: String,
        worktreePath: String,
        reason: String?,
    ) {
        mutateWorktree(atRepoPath = atRepoPath, worktreePath = worktreePath) { it.copy(isLocked = true) }
    }

    override suspend fun unlockWorktree(
        atRepoPath: String,
        worktreePath: String,
    ) {
        mutateWorktree(atRepoPath = atRepoPath, worktreePath = worktreePath) { it.copy(isLocked = false) }
    }

    override suspend fun pruneWorktrees(atRepoPath: String) {
        val normalizedRepoPath = normalize(atRepoPath)
        worktreesByRepository[normalizedRepoPath] =
            worktreesByRepository[normalizedRepoPath]
                .orEmpty()
                .filterNot { it.isPrunable }
                .toMutableList()
    }

    override suspend fun getWorktreeStatus(atWorktreePath: String): WorktreeStatus {
        val worktreeExists = worktreesByRepository.values.flatten().any { normalize(it.path) == normalize(atWorktreePath) }
        if (!worktreeExists) {
            throw GitError.WorktreeNotFound(path = atWorktreePath)
        }
        return WorktreeStatus(
            isDirty = false,
            hasRemote = true,
            ahead = 0,
            behind = 0,
            prStatus =
                PRStatus(
                    number = 1,
                    state = PRState.OPEN,
                    url = "https://example.test/pr/1",
                    title = null,
                ),
        )
    }

    override suspend fun push(
        atWorktreePath: String,
        setUpstream: Boolean,
    ) = Unit

    override suspend fun pull(atWorktreePath: String) = Unit

    override suspend fun createPR(
        atWorktreePath: String,
        title: String,
        body: String,
        baseBranch: String?,
    ): String = "https://example.test/pr/new"

    override suspend fun mergeBranch(
        atRepoPath: String,
        source: String,
        intoTarget: String,
    ) {
        val branches = branchesByRepository[normalize(atRepoPath)] ?: throw GitError.NotARepository(path = atRepoPath)
        if (!branches.contains(source)) {
            throw GitError.BranchNotFound(name = source)
        }
        if (!branches.contains(intoTarget)) {
            throw GitError.BranchNotFound(name = intoTarget)
        }
    }

    override suspend fun deleteRemoteBranch(
        atRepoPath: String,
        branch: String,
    ) {
        remoteBranchesByRepository[normalize(atRepoPath)]?.remove(branch)
    }

    override suspend fun hasRemoteBranch(
        atRepoPath: String,
        branch: String,
    ): Boolean = remoteBranchesByRepository[normalize(atRepoPath)]?.contains(branch) == true

    private fun mutateWorktree(
        atRepoPath: String,
        worktreePath: String,
        transform: (Worktree) -> Worktree,
    ) {
        val normalizedRepoPath = normalize(atRepoPath)
        val normalizedWorktreePath = normalize(worktreePath)
        val worktrees = worktreesByRepository[normalizedRepoPath] ?: throw GitError.WorktreeNotFound(path = worktreePath)
        val index = worktrees.indexOfFirst { normalize(it.path) == normalizedWorktreePath }
        if (index == -1) {
            throw GitError.WorktreeNotFound(path = worktreePath)
        }
        worktrees[index] = transform(worktrees[index])
    }

    private fun normalize(path: String): String = path.trim().trimEnd('/')
}
