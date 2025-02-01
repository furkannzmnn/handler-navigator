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
    private val searchService = PsiSearchService.getInstance()
    private val cache = PsiElementCache.getInstance()

    fun findHandlerClass(useCaseClass: PsiClass, project: Project): PsiClass? {
        val scope = GlobalSearchScope.projectScope(project)
        val useCaseName = useCaseClass.name ?: return null
        val qualifiedName = useCaseClass.qualifiedName ?: return null

        // Try to get from cache first
        val cachedHandler = cache.getCachedClass("handler:$qualifiedName", project)
        if (cachedHandler != null) return cachedHandler

        val foundClasses = mutableSetOf<PsiClass>()
        val elements = searchService.findElementsByWord(useCaseName, project, scope)

        val searchClass = foundClasses
                .filter { it.name?.startsWith("Fake") == false }
                .firstOrNull { it.superTypes.any { superType -> superType.canonicalText.contains("MessageHandler") } }

        val otherOption = findHandlerClassOtherOption(useCaseClass, project)
        return searchClass ?: otherOption

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

        return this.implementsOrExtends(useCaseQualifiedName) ||
                this.referencesInGenerics(useCaseQualifiedName) ||
                this.referencesInConstructors(useCaseQualifiedName) ||
                this.referencesAsInstanceVariable(useCaseQualifiedName)
    }

    //  Constructor içinde `UseCase` var mı?
    private fun PsiClass.referencesInConstructors(useCaseQualifiedName: String): Boolean {
        return this.constructors.any { constructor ->
            constructor.parameterList.parameters.any { param ->
                param.type.canonicalText.contains(useCaseQualifiedName)
            }
        }
    }

    // Sınıf içinde değişken olarak `UseCase` var mı?
    private fun PsiClass.referencesAsInstanceVariable(useCaseQualifiedName: String): Boolean {
        return this.fields.any { field -> field.type.canonicalText.contains(useCaseQualifiedName) }
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

        val possibleNames = listOf(
                findHandlerName(useCaseClass, "Handler"),
                findHandlerName(useCaseClass, "UseCaseHandler"),
                findHandlerName(useCaseClass, "CommandHandler"),
                findHandlerName(useCaseClass, "MessageHandler"),
        )

        val psiShortNamesCache = PsiShortNamesCache.getInstance(project)
        return  possibleNames
                .mapNotNull { psiShortNamesCache.getClassesByName(it, scope).firstOrNull() }
                .firstOrNull()
    }

    private fun findHandlerName(useCaseClass: PsiClass, handlerSuffix: String ): String {
        val useCaseName = useCaseClass.name ?: return ""
        return useCaseName + handlerSuffix
    }
}