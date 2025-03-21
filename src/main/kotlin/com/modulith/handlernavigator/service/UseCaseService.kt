package com.modulith.handlernavigator.service

import com.intellij.psi.*
import com.modulith.handlernavigator.core.cache.PsiElementCache
import com.modulith.handlernavigator.service.analyzer.MethodAnalyzer

class UseCaseService {
    private val cache = PsiElementCache.getInstance()
    private val methodAnalyzer = MethodAnalyzer()

    fun findUseCaseClass(method: PsiMethod): PsiClass? {
        // Try to get from cache first
        val cacheKey = "usecase:${method.containingClass?.qualifiedName}:${method.name}"
        val cachedClass = cache.getCachedClass(cacheKey, method.project)
        if (cachedClass != null) return cachedClass

        // Find use case classes using the analyzer
        val result = methodAnalyzer.findUseCaseClasses(method).firstOrNull()

        // Cache the result before returning
        cache.cacheClass(cacheKey, method.project, result)

        return result
    }
}