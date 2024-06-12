package com.jetbrains.rider.plugins.unoplugin.projectTemplates

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.model.RdProjectTemplate
import com.jetbrains.rider.plugins.unoplugin.UnoPlatformStrings
import com.jetbrains.rider.projectView.projectTemplates.NewProjectDialogContext
import com.jetbrains.rider.projectView.projectTemplates.ProjectTemplatesSharedModel
import com.jetbrains.rider.projectView.projectTemplates.generators.ProjectTemplateGenerator
import com.jetbrains.rider.projectView.projectTemplates.generators.ProjectTemplateGeneratorBase
import com.jetbrains.rider.projectView.projectTemplates.templateTypes.PredefinedProjectTemplateType
import icons.RiderIcons

open class BaseUnoTemplateType(
    override val name: String,
    private val id: String,
    private val defaultName: String,
    private val mustSeeOptions: Set<String> = setOf()
): PredefinedProjectTemplateType() {
    override val group = UnoPlatformStrings.message("projectTemplates.group.name")
    override val icon = RiderIcons.Templates.TemplateCustom
    //to place between the "Other" and "Custom Templates" sections
    override val order = 95
    override val shouldHide: Boolean
        get() = projectTemplates.valueOrNull.isNullOrEmpty()

    override fun acceptableForTemplate(projectTemplate: RdProjectTemplate): Boolean {
        return projectTemplate.id.equals(id, true)
    }

    override fun createGenerator(lifetime: Lifetime, context: NewProjectDialogContext, sharedModel: ProjectTemplatesSharedModel): ProjectTemplateGenerator {
        return object : ProjectTemplateGeneratorBase(lifetime, context, sharedModel, createProject = true) {
            override val defaultName = this@BaseUnoTemplateType.defaultName
            private val blocks = listOf(FrameworkBlock(propertyGraph))
            override val templateSpecificHiddenOptions = super.templateSpecificHiddenOptions + blocks.flatMap { it.options }.map { it.name }
            override val mustSeeOptions = this@BaseUnoTemplateType.mustSeeOptions
            init {
                setTemplateSpecificOptions(blocks.flatMap { it.options }.associate { it.name to it.property })
            }

            override fun tryGetSingleTemplate() = projectTemplates.valueOrNull?.firstOrNull()
            override fun createTemplateSpecificPanel(): DialogPanel {
                return panel {
                    blocks.map { row { cell(it.render()) } }
                }
            }
        }
    }
}
