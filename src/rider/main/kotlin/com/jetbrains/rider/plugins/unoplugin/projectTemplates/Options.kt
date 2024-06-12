package com.jetbrains.rider.plugins.unoplugin.projectTemplates

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.operation
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth
import com.jetbrains.rd.util.reactive.Signal
import com.jetbrains.rider.projectView.projectTemplates.utils.setIfNew

data class UnoOption<T>(val name: String, val property: GraphProperty<T>)

abstract class OptionsBlock(val collapsedName: String? = null) {
    abstract val options: List<UnoOption<*>>
    abstract fun onPresetChanged(newPreset: PresetEnum)
    abstract fun render(): DialogPanel
    val updateUI: Signal<Unit> = Signal()
}

enum class PresetEnum(val presentation: String, val value: String?) {
    Blank("Blank", "blank"),
    Recommended("Recommended", "recommended"),
    Custom("Custom", null);

    override fun toString() = value ?: ""
}
class PresetBlock(propertyGraph: PropertyGraph): OptionsBlock() {
    val presetProperty = propertyGraph.property(PresetEnum.Recommended)
    override val options = listOf(UnoOption("preset", presetProperty))

    override fun onPresetChanged(newPreset: PresetEnum) = Unit
    override fun render(): DialogPanel {
        return panel {
            row("Preset:") {
                segmentedButton(PresetEnum.entries) { text = it.presentation }.bind(presetProperty)
            }
        }
    }
}

enum class FrameworkEnum(val presentation: String, val value: String) {
    Net80(".NET 8.0", "net8.0"),
    Net90(".NET 9.0", "net9.0");

    override fun toString() = value
}
class FrameworkBlock(propertyGraph: PropertyGraph): OptionsBlock() {
    val frameworkProperty = propertyGraph.property(FrameworkEnum.Net80)
    override val options = listOf(UnoOption("tfm", frameworkProperty))

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank, PresetEnum.Recommended -> frameworkProperty.setIfNew(FrameworkEnum.Net80)
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row("Framework:") {
                segmentedButton(FrameworkEnum.entries) { text = it.presentation }.bind(frameworkProperty)
            }
        }
    }
}

data class PlatformsValues(val android: Boolean, val ios: Boolean, val wasm: Boolean, val maccatalyst: Boolean, val windows: Boolean, val desktop: Boolean)
{
    override fun toString(): String {
        var result = ""
        if (android) result += "android|"
        if (ios) result += "ios|"
        if (wasm) result += "wasm|"
        if (maccatalyst) result += "maccatalyst|"
        if (windows) result += "windows|"
        if (desktop) result += "desktop|"
        return result.trim('|')
    }
}
class PlatformsBlock(propertyGraph: PropertyGraph, private val minimumLabelWidth: Int): OptionsBlock() {
    private val initialPlatforms = PlatformsValues(
        android = true,
        ios = true,
        wasm = true,
        maccatalyst = true,
        windows = true,
        desktop = true
    )
    val platformProperty = propertyGraph.property(initialPlatforms)
    override val options = listOf(UnoOption("platforms", platformProperty))

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank, PresetEnum.Recommended -> platformProperty.setIfNew(initialPlatforms)
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row {
                text("Platforms:").align(AlignY.TOP).applyToComponent { minimumWidth = minimumLabelWidth }
                panel {
                    row {
                        text("Mobile:")
                        checkBox("Android").bindSelected(platformProperty.transform({ it.android }, { platformProperty.get().copy(android = it) }))
                        checkBox("iOS").bindSelected(platformProperty.transform({ it.ios }, { platformProperty.get().copy(ios = it) }))
                    }
                    row {
                        text("Web:")
                        checkBox("WebAssembly").bindSelected(platformProperty.transform({ it.wasm }, { platformProperty.get().copy(wasm = it) }))
                    }
                    row {
                        text("Desktop:")
                        checkBox("macOS (Catalyst)").bindSelected(
                            platformProperty.transform({ it.maccatalyst }, { platformProperty.get().copy(maccatalyst = it) }))
                        checkBox("Windows").bindSelected(platformProperty.transform({ it.windows }, { platformProperty.get().copy(windows = it) }))
                        checkBox("Desktop").bindSelected(platformProperty.transform({ it.desktop }, { platformProperty.get().copy(desktop = it) }))
                    }
                }
            }
        }
    }
}

enum class ArchitectureEnum(val presentation: String, val value: String) {
    None("None", "none"),
    MVVM("MVVM", "mvvm"),
    MVUX("MVUX", "mvux");

    override fun toString() = value
}
class ArchitectureBlock(propertyGraph: PropertyGraph, val presetProperty: GraphProperty<PresetEnum>): OptionsBlock() {
    val architectureProperty = propertyGraph.property(ArchitectureEnum.MVUX)
    override val options = listOf(UnoOption("architecture", architectureProperty))

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank -> architectureProperty.setIfNew(ArchitectureEnum.None)
            PresetEnum.Recommended -> architectureProperty.setIfNew(ArchitectureEnum.MVUX)
            PresetEnum.Custom -> {}
        }
        updateUI.fire(Unit)
    }

    override fun render(): DialogPanel {
        return panel {
            row("Presentation:") {
                segmentedButton(ArchitectureEnum.entries) {
                    text = it.presentation
                    enabled = !(it == ArchitectureEnum.None && presetProperty.get() == PresetEnum.Recommended)
                }.bind(architectureProperty)
            }
        }
    }
}

enum class MarkupEnum(val presentation: String, val value: String) {
    XAML("XAML", "xaml"),
    csharp("C# Markup", "csharp");

    override fun toString() = value
}
class MarkupBlock(propertyGraph: PropertyGraph): OptionsBlock() {
    val markupProperty = propertyGraph.property(MarkupEnum.XAML)
    override val options = listOf(UnoOption("markup", markupProperty))

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank, PresetEnum.Recommended -> markupProperty.setIfNew(MarkupEnum.XAML)
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row("Markup:") {
                segmentedButton(MarkupEnum.entries) { text = it.presentation }.bind(markupProperty)
            }
        }
    }
}

enum class ThemeEnum(val presentation: String, val value: String) {
    Material("Material", "material"),
    Fluent("Fluent", "fluent"),
    Cupertino("Cupertino", "cupertino");

    override fun toString() = value
}
class ThemeBlock(propertyGraph: PropertyGraph): OptionsBlock() {
    val themeProperty = propertyGraph.property(ThemeEnum.Material)
    val themeServiceProperty = propertyGraph.property(true)
    val dspProperty = propertyGraph.property(true)
    override val options = listOf(
        UnoOption("appTheme", themeProperty),
        UnoOption("themeService", themeServiceProperty),
        UnoOption("dspGenerator", dspProperty))

    init {
        themeProperty.afterChange {
            when (it) {
                ThemeEnum.Fluent, ThemeEnum.Cupertino -> dspProperty.setIfNew(false)
                ThemeEnum.Material -> {}
            }
        }
    }

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank -> {
                themeProperty.setIfNew(ThemeEnum.Fluent)
                themeServiceProperty.setIfNew(false)
                dspProperty.setIfNew(false)
            }
            PresetEnum.Recommended -> {
                themeProperty.setIfNew(ThemeEnum.Material)
                themeServiceProperty.setIfNew(true)
                dspProperty.setIfNew(true)
            }
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row("Theme:") {
                segmentedButton(ThemeEnum.entries) { text = it.presentation }.bind(themeProperty)
            }
            row("") {
                checkBox("Theme Service").bindSelected(themeServiceProperty)
                checkBox("Import DSP").enabledIf(themeProperty.transform { it == ThemeEnum.Material }).bindSelected(dspProperty)
            }
        }
    }
}

enum class NavigationEnum(val presentation: String, val value: String) {
    Regions("Regions", "regions"),
    Blank("Blank", "blank");

    override fun toString() = value
}
enum class LoggingEnum(val presentation: String, val value: String) {
    Console("Console", "none"),
    Default("Default", "default"),
    Serilog("Serilog", "serilog");

    override fun toString() = value
}
class ExtensionsBlock(propertyGraph: PropertyGraph): OptionsBlock("Extensions") {
    val dependencyInjectionProperty = propertyGraph.property(true)
    val configurationProperty = propertyGraph.property(true)
    val httpProperty = propertyGraph.property(true)
    val localizationProperty = propertyGraph.property(true)
    val navigationProperty = propertyGraph.property(NavigationEnum.Regions)
    val loggingProperty = propertyGraph.property(LoggingEnum.Default)

    override val options: List<UnoOption<*>> = listOf(
        UnoOption("dependencyInjection", dependencyInjectionProperty),
        UnoOption("configuration", configurationProperty),
        UnoOption("http", httpProperty),
        UnoOption("localization", localizationProperty),
        UnoOption("navigation", navigationProperty),
        UnoOption("logging", loggingProperty),
    )

    init {
        dependencyInjectionProperty.afterChange {
            if (!it) {
                configurationProperty.setIfNew(false)
                httpProperty.setIfNew(false)
                localizationProperty.setIfNew(false)
                navigationProperty.setIfNew(NavigationEnum.Blank)
                loggingProperty.setIfNew(LoggingEnum.Console)
            }
            updateUI.fire(Unit)
        }
    }

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank -> {
                dependencyInjectionProperty.setIfNew(false)
            }
            PresetEnum.Recommended -> {
                dependencyInjectionProperty.setIfNew(true)
                configurationProperty.setIfNew(true)
                httpProperty.setIfNew(true)
                localizationProperty.setIfNew(true)
                navigationProperty.setIfNew(NavigationEnum.Regions)
                //btw presetLoggingDefault in template.json setting serilog
                loggingProperty.setIfNew(LoggingEnum.Default)
            }
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row { checkBox("Dependency Injection").bindSelected(dependencyInjectionProperty) }
            row { checkBox("Configuration").enabledIf(dependencyInjectionProperty).bindSelected(configurationProperty) }
            row { checkBox("HTTP").enabledIf(dependencyInjectionProperty).bindSelected(httpProperty) }
            row { checkBox("Localization").enabledIf(dependencyInjectionProperty).bindSelected(localizationProperty) }
            row("Navigation:") {
                segmentedButton(NavigationEnum.entries) {
                    text = it.presentation
                    enabled = dependencyInjectionProperty.get() || it == NavigationEnum.Blank
                }.bind(navigationProperty)
            }
            row("Logging:") {
                segmentedButton(LoggingEnum.entries) {
                    text = it.presentation
                    enabled = dependencyInjectionProperty.get() || it == LoggingEnum.Console
                }.bind(loggingProperty)
            }
        }
    }

}

class FeaturesBlock(
    propertyGraph: PropertyGraph,
    frameworkProperty: GraphProperty<FrameworkEnum>,
    private val platformProperty: GraphProperty<PlatformsValues>
) : OptionsBlock("Features") {
    val toolkitProperty = propertyGraph.property(true)
    val mauiEmbeddingProperty = propertyGraph.property(false)
    val serverProperty = propertyGraph.property(false)
    val wasmMultiThreadingProperty = propertyGraph.property(false)
    val wasmPwaManifestProperty = propertyGraph.property(true)
    val vsCodeDebuggingProperty = propertyGraph.property(true)
    val mediaElementProperty = propertyGraph.property(false)

    val wasmMultiThreadingEnabled = operation(frameworkProperty, platformProperty) { framework, platforms -> framework == FrameworkEnum.Net80 && platforms.wasm}
    init {
        frameworkProperty.afterChange { if (it != FrameworkEnum.Net80) wasmMultiThreadingProperty.setIfNew(false) }
        platformProperty.afterChange {
            if (!it.wasm) {
                wasmMultiThreadingProperty.setIfNew(false)
                wasmPwaManifestProperty.setIfNew(false)
            }
        }
    }

    override val options: List<UnoOption<*>> = listOf(
        UnoOption("toolkit", toolkitProperty),
        UnoOption("mauiEmbedding", mauiEmbeddingProperty),
        UnoOption("server", serverProperty),
        UnoOption("wasmMultiThreading", wasmMultiThreadingProperty),
        UnoOption("wasmPwaManifest", wasmPwaManifestProperty),
        UnoOption("vscode", vsCodeDebuggingProperty),
        UnoOption("mediaElement", mediaElementProperty),
    )

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank -> {
                toolkitProperty.setIfNew(false)
                mauiEmbeddingProperty.setIfNew(false)
                serverProperty.setIfNew(false)
                wasmMultiThreadingProperty.setIfNew(false)
                wasmPwaManifestProperty.setIfNew(true)
                vsCodeDebuggingProperty.setIfNew(true)
                mediaElementProperty.setIfNew(false)
            }
            PresetEnum.Recommended -> {
                toolkitProperty.setIfNew(true)
                mauiEmbeddingProperty.setIfNew(false)
                serverProperty.setIfNew(false)
                wasmMultiThreadingProperty.setIfNew(false)
                wasmPwaManifestProperty.setIfNew(true)
                vsCodeDebuggingProperty.setIfNew(true)
                mediaElementProperty.setIfNew(false)
            }
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row { checkBox("Toolkit").bindSelected(toolkitProperty) }
            row { checkBox(".NET MAUI Embedding").bindSelected(mauiEmbeddingProperty) }
            row { checkBox("Server").bindSelected(serverProperty) }
            row { checkBox("WASM Multi-Threading").enabledIf(wasmMultiThreadingEnabled).bindSelected(wasmMultiThreadingProperty) }
            row { checkBox("PWA Manifest").enabledIf(platformProperty.transform { it.wasm }).bindSelected(wasmPwaManifestProperty) }
            row { checkBox("Visual Studio Code Debugging").bindSelected(vsCodeDebuggingProperty) }
            row { checkBox("Media Element").bindSelected(mediaElementProperty) }
        }
    }

}

enum class AuthEnum(val presentation: String, val value: String) {
    None("None", "none"),
    Custom("Custom", "custom"),
    MSAL("MSAL", "msal"),
    OIDC("OIDC", "oidc"),
    Web("Web", "Web");

    override fun toString() = value
}
class AuthBlock(
    propertyGraph: PropertyGraph,
    private val dependencyInjectionProperty: GraphProperty<Boolean>,
    private val platformProperty: GraphProperty<PlatformsValues>
): OptionsBlock() {
    val authProperty = propertyGraph.property(AuthEnum.None)
    override val options = listOf(UnoOption("authentication", authProperty))

    init {
        dependencyInjectionProperty.afterChange {
            if (!it) authProperty.setIfNew(AuthEnum.None)
            updateUI.fire(Unit)
        }
        platformProperty.afterChange {
            if ((it.maccatalyst || it.desktop) && authProperty.get() == AuthEnum.MSAL) {
                authProperty.set(AuthEnum.None)
            }
            updateUI.fire(Unit)
        }
    }

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank, PresetEnum.Recommended -> authProperty.setIfNew(AuthEnum.None)
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row("Authentication:") {
                segmentedButton(AuthEnum.entries) {
                    text = it.presentation
                    enabled = isEnabled(it)
                }.bind(authProperty)
            }
        }
    }

    private fun isEnabled(auth: AuthEnum): Boolean {
        return when (auth) {
            AuthEnum.None -> true
            AuthEnum.Custom, AuthEnum.OIDC, AuthEnum.Web -> dependencyInjectionProperty.get()
            AuthEnum.MSAL -> dependencyInjectionProperty.get() && !(platformProperty.get().maccatalyst || platformProperty.get().desktop)
        }
    }
}

class ApplicationBlock(propertyGraph: PropertyGraph, solutionNameProperty: GraphProperty<String>): OptionsBlock() {
    val appIdProperty = propertyGraph.property("com.companyname.${solutionNameProperty.get()}")
    val publisherProperty = propertyGraph.property("O=${solutionNameProperty.get()}")
    override val options = listOf(UnoOption("appId", appIdProperty), UnoOption("publisher", publisherProperty))

    init {
        appIdProperty.dependsOn(solutionNameProperty, deleteWhenModified = true) { "com.companyname.${solutionNameProperty.get()}" }
        publisherProperty.dependsOn(solutionNameProperty, deleteWhenModified = true) { "O=${solutionNameProperty.get()}" }
    }

    override fun onPresetChanged(newPreset: PresetEnum) = Unit

    override fun render(): DialogPanel {
        return panel {
            row("Application ID:") { textField().columns(COLUMNS_MEDIUM).bindText(appIdProperty) }
            row("Publisher:") { textField().columns(COLUMNS_MEDIUM).bindText(publisherProperty) }
        }
    }

}

data class TestsValues(val unit: Boolean, val ui: Boolean)
{
    override fun toString(): String {
        var result = ""
        if (unit) result += "unit|"
        if (ui) result += "ui|"
        if (result.isEmpty()) result = "none"
        return result.trim('|')
    }
}
class TestsBlock(propertyGraph: PropertyGraph): OptionsBlock() {
    val testsProperty = propertyGraph.property(TestsValues(true, true))
    override val options = listOf(UnoOption("tests", testsProperty))

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank -> testsProperty.setIfNew(TestsValues(false, false))
            PresetEnum.Recommended -> testsProperty.setIfNew(TestsValues(true, true))
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row("Tests:") {
                cell()
                checkBox("Unit Tests").bindSelected(testsProperty.transform({ it.unit }, { testsProperty.get().copy(unit = it) }))
                checkBox("UI Tests").bindSelected(testsProperty.transform({ it.ui }, { testsProperty.get().copy(ui = it) }))
            }
        }
    }
}

enum class CIEnum(val presentation: String, val value: String) {
    None("None", "none"),
    Azure("Azure Pipelines", "azure"),
    Github("GitHub Actions", "github");

    override fun toString() = value
}
class CIBlock(propertyGraph: PropertyGraph): OptionsBlock() {
    val ciProperty = propertyGraph.property(CIEnum.None)
    override val options = listOf(UnoOption("continuousIntegration", ciProperty))

    override fun onPresetChanged(newPreset: PresetEnum) {
        when (newPreset) {
            PresetEnum.Blank, PresetEnum.Recommended -> ciProperty.setIfNew(CIEnum.None)
            PresetEnum.Custom -> {}
        }
    }

    override fun render(): DialogPanel {
        return panel {
            row("CI Pipeline:") {
                segmentedButton(CIEnum.entries) { text = it.presentation }.bind(ciProperty)
            }
        }
    }
}
