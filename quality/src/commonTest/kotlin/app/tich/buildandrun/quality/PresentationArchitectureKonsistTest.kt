package app.tich.buildandrun.quality

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import kotlin.test.Test

class PresentationArchitectureKonsistTest {
    private val domainForbiddenImportRegex =
        Regex(
            "^import app\\.tich\\.buildandrun\\.(application|presentation|macos|buildandrun)\\.",
            RegexOption.MULTILINE,
        )
    private val applicationForbiddenImportRegex =
        Regex(
            "^import app\\.tich\\.buildandrun\\.(presentation|macos|buildandrun)\\.",
            RegexOption.MULTILINE,
        )
    private val presentationBridgeImportRegex =
        Regex(
            "^import app\\.tich\\.buildandrun\\.(macos|buildandrun)\\.",
            RegexOption.MULTILINE,
        )
    private val applicationPortRegex = Regex("app\\.tich\\.buildandrun\\.application\\.context\\.[\\w.]*\\.port\\.")
    private val typedUseCaseBindingRegex = Regex("\\b(single|factory)\\s*<[^>]*UseCase>")
    private val useCaseCreationBindingRegex =
        Regex("\\b(single|factory)\\s*(<[^>]+>)?\\s*\\{[\\s\\S]*?\\b[A-Za-z0-9_]+UseCase\\(")

    @Test
    fun domainLayerDoesNotImportOuterLayers() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse { file ->
                file.path.contains("/domain/") &&
                    domainForbiddenImportRegex.containsMatchIn(file.text)
            }
    }

    @Test
    fun applicationLayerDoesNotImportPresentationOrBridgeLayers() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse { file ->
                file.path.contains("/application/") &&
                    applicationForbiddenImportRegex.containsMatchIn(file.text)
            }
    }

    @Test
    fun presentationLayerDoesNotImportBridgeLayer() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse { file ->
                file.path.contains("/presentation/") &&
                    presentationBridgeImportRegex.containsMatchIn(file.text)
            }
    }

    @Test
    fun presentationServicesDoNotDependOnApplicationPorts() {
        Konsist
            .scopeFromProduction()
            .classes()
            .assertFalse { declaration ->
                declaration.resideInPackage("..presentation..context..impl..") &&
                    declaration.hasNameEndingWith("Service") &&
                    applicationPortRegex.containsMatchIn(declaration.text)
            }
    }

    @Test
    fun appRootKoinDoesNotBindUseCasesDirectly() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse { file ->
                file.path.contains("/presentation/root/src/commonMain/kotlin/app/tich/buildandrun/presentation/app/AppRootKoin.kt") &&
                    typedUseCaseBindingRegex.containsMatchIn(file.text)
            }
    }

    @Test
    fun useCaseDeclarationsResideOnlyInApplicationUseCasePackage() {
        Konsist
            .scopeFromProduction()
            .classes()
            .assertFalse { declaration ->
                declaration.hasNameEndingWith("UseCase") &&
                    !declaration.resideInPackage("..application..usecase..")
            }
    }

    @Test
    fun useCaseKoinDefinitionsResideOnlyInApplicationDiPackages() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse { file ->
                !(file.path.contains("/application/") && file.path.contains("/di/")) &&
                    (
                        typedUseCaseBindingRegex.containsMatchIn(file.text) ||
                            useCaseCreationBindingRegex.containsMatchIn(file.text)
                    )
            }
    }
}
