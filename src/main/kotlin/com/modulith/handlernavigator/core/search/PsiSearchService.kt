package com.modulith.handlernavigator.core.search

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.modulith.handlernavigator.core.cache.PsiElementCache

/**
 * Service responsible for searching PSI elements efficiently using caching.
 */
class PsiSearchService {
    private val cache = PsiElementCache.getInstance()

    fun findClassByQualifiedName(qualifiedName: String, project: Project): PsiClass? {
        return cache.getCachedClass(qualifiedName, project) ?: findAndCacheClass(qualifiedName, project)
    }

    fun findElementsByWord(word: String, project: Project, scope: GlobalSearchScope): Set<PsiElement> {
        val searchHelper = PsiSearchHelper.getInstance(project)
        val foundElements = mutableSetOf<PsiElement>()

        searchHelper.processElementsWithWord(
            { element, _ ->
                foundElements.add(element)
                true
            },
            scope,
            word,
            UsageSearchContext.IN_CODE,
            true
        )

        return foundElements
    }

    private fun findAndCacheClass(qualifiedName: String, project: Project): PsiClass? {
        val foundClass = findClassInProject(qualifiedName, project)
        cache.cacheClass(qualifiedName, project, foundClass)
        return foundClass
    }

    private fun findClassInProject(qualifiedName: String, project: Project): PsiClass? {
        val scope = GlobalSearchScope.projectScope(project)
        return findElementsByWord(qualifiedName, project, scope)
            .filterIsInstance<PsiClass>()
            .firstOrNull { it.qualifiedName == qualifiedName }
    }

    companion object {
        @Volatile
        private var instance: PsiSearchService? = null

        fun getInstance(): PsiSearchService {
            return instance ?: synchronized(this) {
                instance ?: PsiSearchService().also { instance = it }
            }
        }
    }
}