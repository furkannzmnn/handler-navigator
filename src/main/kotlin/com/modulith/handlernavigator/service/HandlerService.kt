package com.modulith.handlernavigator.service

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.UsageSearchContext

class HandlerService {
    fun findHandlerClass(useCaseClass: PsiClass, project: Project): PsiClass? {
        val scope = GlobalSearchScope.projectScope(project)
        val psiSearchHelper = PsiSearchHelper.getInstance(project)
        val useCaseName = useCaseClass.name ?: return null

        val foundClasses = mutableSetOf<PsiClass>()

        psiSearchHelper.processElementsWithWord(
            { element, _ ->
                val psiClass = findContainingPsiClass(element) ?: return@processElementsWithWord true
                if (psiClass.referencesUseCase(useCaseClass)) {
                    foundClasses.add(psiClass)
                }
                true
            },
            scope,
            useCaseClass.qualifiedName ?: return null,
            UsageSearchContext.IN_CODE,
            true
        )

        return foundClasses.firstOrNull() ?: findHandlerClassOtherOption(useCaseClass, project)
    }

    private fun findContainingPsiClass(element: PsiElement): PsiClass? {
        var context = element.context
        while (context != null) {
            if (context is PsiClass) {
                return context
            }
            context = context.context
        }
        return null
    }


    private fun PsiClass.referencesUseCase(useCaseClass: PsiClass): Boolean {
        val useCaseQualifiedName = useCaseClass.qualifiedName ?: return false
        return this.implementsOrExtends(useCaseQualifiedName) || this.referencesInGenerics(useCaseQualifiedName)
    }

    private fun PsiClass.implementsOrExtends(useCaseQualifiedName: String): Boolean {
        return this.superTypes.any { it.canonicalText.contains(useCaseQualifiedName) }
    }

    private fun PsiClass.referencesInGenerics(useCaseQualifiedName: String): Boolean {
        return this.fields.any { field -> field.type.canonicalText.contains(useCaseQualifiedName) } ||
                this.methods.any { method ->
                    method.parameterList.parameters.any { param ->
                        param.type.canonicalText.contains(useCaseQualifiedName)
                    }
                }
    }

    fun findHandlerClassOtherOption(useCaseClass: PsiClass, project: Project): PsiClass? {
        val scope = GlobalSearchScope.everythingScope(project)
        val handlerSuffix = findHandlerName(useCaseClass, "Handler")
        val useCaseHandlerSuffix = findHandlerName(useCaseClass, "UseCaseHandler")

        val psiShortNamesCache = PsiShortNamesCache.getInstance(project)
        val handlerClass = psiShortNamesCache.getClassesByName(handlerSuffix, scope).firstOrNull()
        val useCaseHandlerClass = psiShortNamesCache.getClassesByName(useCaseHandlerSuffix, scope).firstOrNull()

        return handlerClass ?: useCaseHandlerClass
    }

    private fun findHandlerName(useCaseClass: PsiClass, handlerSuffix: String ): String {
        val useCaseName = useCaseClass.name ?: return ""
        return useCaseName + handlerSuffix
    }
}