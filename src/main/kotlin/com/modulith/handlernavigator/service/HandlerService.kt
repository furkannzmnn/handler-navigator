package com.modulith.handlernavigator.service

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache

class HandlerService {
    fun findHandlerClass(useCaseClass: PsiClass, project: Project): PsiClass? {
        val scope = GlobalSearchScope.everythingScope(project)
        val handlerClassName = findHandlerName(useCaseClass)

        val classes = PsiShortNamesCache.getInstance(project).getClassesByName(handlerClassName, scope)
        return classes.firstOrNull()
    }

    private fun findHandlerName(useCaseClass: PsiClass): String {
        val handlerSuffix = "Handler"
        val useCaseName = useCaseClass.name ?: return ""
        return useCaseName + handlerSuffix
    }
}