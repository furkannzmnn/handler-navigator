package com.modulith.handlernavigator.listener

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.modulith.handlernavigator.core.cache.PsiElementCache

class ProjectListener : ProjectManagerListener {
    override fun projectClosing(project: Project) {
        // Clear the cache when project is closing
        PsiElementCache.getInstance().clearCache()
    }
}