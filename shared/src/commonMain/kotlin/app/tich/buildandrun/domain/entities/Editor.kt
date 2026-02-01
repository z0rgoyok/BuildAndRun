package app.tich.buildandrun.domain.entities

data class Editor(
    val id: String,
    val name: String,
    val command: String,
    val icon: String,
    val appName: String?,
) {
    init {
        require(id.isNotBlank()) { "Editor id cannot be blank" }
        require(name.isNotBlank()) { "Editor name cannot be blank" }
        require(command.isNotBlank()) { "Editor command cannot be blank" }
    }

    companion object {
        val builtIn: List<Editor> =
            listOf(
                Editor("vscode", "VS Code", "code", "chevron.left.forwardslash.chevron.right", "Visual Studio Code"),
                Editor("cursor", "Cursor", "cursor", "cursorarrow.rays", "Cursor"),
                Editor("intellij", "IntelliJ IDEA", "idea", "brain", "IntelliJ IDEA"),
                Editor("android-studio", "Android Studio", "studio", "android", "Android Studio"),
                Editor("webstorm", "WebStorm", "webstorm", "globe", "WebStorm"),
                Editor("pycharm", "PyCharm", "pycharm", "bolt.fill", "PyCharm"),
                Editor("goland", "GoLand", "goland", "hare", "GoLand"),
                Editor("sublime", "Sublime Text", "subl", "doc.text", "Sublime Text"),
                Editor("zed", "Zed", "zed", "bolt.horizontal", "Zed"),
                Editor("xcode", "Xcode", "xed", "hammer", "Xcode"),
                Editor("finder", "Finder", "open", "folder", null),
                Editor("terminal", "Terminal", "open -a Terminal", "terminal", "Terminal"),
            )

        fun findById(id: String): Editor? = builtIn.find { it.id == id }
    }
}
