package com.jetbrains.rider.plugins.unoplugin

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.unoplugin.model.RdCallRequest
import com.jetbrains.rider.plugins.unoplugin.model.rdUnoPluginModel
import com.jetbrains.rider.projectView.solution

@Service(Service.Level.PROJECT)
class ProtocolCaller(private val project: Project) {

    suspend fun doCall(input: String): Int {
        val model = project.solution.rdUnoPluginModel
        val request = RdCallRequest(input)
        val response = model.myCall.startSuspending(request)
        return response.myResult
    }
}
