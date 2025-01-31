package com.modulith.handlernavigator.core.cache

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe cache for PSI elements to improve performance by reducing repeated searches
 * and PSI tree traversals.
 */
class PsiElementCache {
    private val classCache = ConcurrentHashMap<String, PsiClass?>()
    private val elementCache = ConcurrentHashMap<String, PsiElement?>()

    fun getCachedClass(qualifiedName: String, project: Project): PsiClass? {
        return classCache.getOrDefault(getCacheKey(qualifiedName, project), null)
    }

    fun cacheClass(qualifiedName: String, project: Project, psiClass: PsiClass?) {
        classCache[getCacheKey(qualifiedName, project)] = psiClass
    }

    fun getCachedElement(elementId: String, project: Project): PsiElement? {
        return elementCache.getOrDefault(getCacheKey(elementId, project), null)
    }

    fun cacheElement(elementId: String, project: Project, element: PsiElement?) {
        elementCache[getCacheKey(elementId, project)] = element
    }

    fun clearCache() {
        classCache.clear()
        elementCache.clear()
    }

    private fun getCacheKey(identifier: String, project: Project): String {
        return "${project.name}:$identifier"
    }

    companion object {
        @Volatile
        private var instance: PsiElementCache? = null

        fun getInstance(): PsiElementCache {
            return instance ?: synchronized(this) {
                instance ?: PsiElementCache().also { instance = it }
            }
        }
    }
}