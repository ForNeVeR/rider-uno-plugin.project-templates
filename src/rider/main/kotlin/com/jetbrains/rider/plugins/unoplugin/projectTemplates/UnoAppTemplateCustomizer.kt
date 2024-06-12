package com.jetbrains.rider.plugins.unoplugin.projectTemplates

import com.jetbrains.rider.projectView.projectTemplates.providers.ProjectTemplateCustomizer
import com.jetbrains.rider.projectView.projectTemplates.templateTypes.PredefinedProjectTemplateType

class UnoAppTemplateCustomizer : ProjectTemplateCustomizer {
    override fun getCustomProjectTemplateTypes(): Set<PredefinedProjectTemplateType> {
        return setOf(
            UnoAppTemplateType(),
            BaseUnoTemplateType(
                "Cross-Platform Library",
                "Uno.Platform.UnoLib",
                "UnoLib"),
            BaseUnoTemplateType(
                "Cross-Platform UI Tests Library",
                "Uno.Platform.UITestLibrary",
                "UnoApp.UITests"),
            BaseUnoTemplateType(
                "Multi-Platform App (.NET 8, UWP)",
                "Uno.Platform.UnoApp.UWP.netcoremobile",
                "UnoApp",
                setOf("WebAssembly", "Mobile", "skia-wpf", "skia-gtk", "skia-linux-fb", "wasm-pwa-manifest")),
            BaseUnoTemplateType(
                "Maui Embedding Class Library",
                "Uno.Platform.UnoApp.WinUI.mauiembeddingclasslibrary.CSharp",
                "UnoMauiLibrary",
                setOf("useAndroid", "useIOS", "useMacCatalyst", "useWinAppSdk")),
        )
    }
}
