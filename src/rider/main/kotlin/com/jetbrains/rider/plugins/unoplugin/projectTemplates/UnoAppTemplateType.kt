package com.jetbrains.rider.plugins.unoplugin.projectTemplates

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Placeholder
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.model.RdProjectTemplate
import com.jetbrains.rider.plugins.unoplugin.UnoPlatformStrings
import com.jetbrains.rider.projectView.projectTemplates.NewProjectDialogContext
import com.jetbrains.rider.projectView.projectTemplates.ProjectTemplatesSharedModel
import com.jetbrains.rider.projectView.projectTemplates.generators.ProjectTemplateGenerator
import com.jetbrains.rider.projectView.projectTemplates.generators.ProjectTemplateGeneratorBase
import com.jetbrains.rider.projectView.projectTemplates.utils.ForbidNextAdvisesCookie
import com.jetbrains.rider.projectView.projectTemplates.utils.setIfNew

private const val templateDefaultName = "UnoApp1"

class UnoAppTemplateType: BaseUnoTemplateType(UnoPlatformStrings.message("projectTemplates.template.uno.app.name"), "Uno.Platform.UnoApp.WinUI.netcoremobile.CSharp", templateDefaultName) {
    override fun createGenerator(lifetime: Lifetime, context: NewProjectDialogContext, sharedModel: ProjectTemplatesSharedModel): ProjectTemplateGenerator {
        return object : ProjectTemplateGeneratorBase(lifetime, context, sharedModel, createProject = true) {
            override val defaultName = templateDefaultName

            val preset = PresetBlock(propertyGraph)
            val framework = FrameworkBlock(propertyGraph)
            val platforms = PlatformsBlock(propertyGraph, minimumLabelWidth)
            val architecture = ArchitectureBlock(propertyGraph, preset.presetProperty)
            val markup = MarkupBlock(propertyGraph)
            val theme = ThemeBlock(propertyGraph)
            val extensions = ExtensionsBlock(propertyGraph)
            val features = FeaturesBlock(propertyGraph, framework.frameworkProperty, platforms.platformProperty)
            val auth = AuthBlock(propertyGraph, extensions.dependencyInjectionProperty, platforms.platformProperty)
            val application = ApplicationBlock(propertyGraph, solutionNameProperty)
            val tests = TestsBlock(propertyGraph)
            val ci = CIBlock(propertyGraph)

            private val blocks = listOf(preset, framework, platforms, architecture, markup, theme, extensions, features, auth, application, tests, ci)
            override val templateSpecificHiddenOptions = super.templateSpecificHiddenOptions + blocks.flatMap { it.options }.map { it.name }

            val placeholders: MutableMap<OptionsBlock, Placeholder> = mutableMapOf()

            init {
                preset.presetProperty.afterChange { newPreset ->
                    ForbidNextAdvisesCookie.forbidNextAdvise { blocks.forEach { it.onPresetChanged(newPreset) } }
                }
                blocks.forEach { block ->
                    block.updateUI.advise(lifetime) {
                        placeholders[block]?.component = block.render()
                        updateMinimumWidthForAllRowLabels()
                    }
                    if (block in setOf(preset, application)) return@forEach
                    block.options.forEach {option ->
                        option.property.afterChange {
                            if (ForbidNextAdvisesCookie.isForbidden()) return@afterChange
                            preset.presetProperty.setIfNew(PresetEnum.Custom)
                        }
                    }
                }

                setTemplateSpecificOptions(blocks.flatMap { it.options }.associate { it.name to it.property })
            }

            override fun tryGetSingleTemplate(): RdProjectTemplate? {
                // there shouldn't be more or less than 1.
                return projectTemplates.valueOrNull?.firstOrNull()
            }

            override fun createTemplateSpecificPanel(): DialogPanel {
                return panel {
                    blocks.map {
                        if (it.collapsedName != null) {
                            collapsibleGroup(it.collapsedName) {
                                renderBlock(it)
                            }
                        } else {
                            renderBlock(it)
                        }
                    }
                }
            }
            private fun Panel.renderBlock(block: OptionsBlock) {
                row {
                    val placeholder = placeholder()
                    placeholders[block] = placeholder
                    placeholder.component = block.render()
                }
            }
        }
    }
}
