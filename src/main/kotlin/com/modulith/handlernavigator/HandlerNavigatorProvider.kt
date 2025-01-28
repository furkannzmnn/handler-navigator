package com.modulith.handlernavigator

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiClass
import com.modulith.handlernavigator.marker.HandlerMarkerBuilder
import com.modulith.handlernavigator.service.AnnotationService
import com.modulith.handlernavigator.service.HandlerService
import com.modulith.handlernavigator.service.UseCaseService

class HandlerNavigatorProvider : RelatedItemLineMarkerProvider() {

    private val annotationService = AnnotationService()
    private val useCaseService = UseCaseService()
    private val handlerService = HandlerService()
    private val markerBuilder = HandlerMarkerBuilder()

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (!isValidAnnotationElement(element)) return

        val annotation = element as PsiAnnotation
        val navigationChain = buildNavigationChain(annotation) ?: return

        addNavigationMarker(annotation, navigationChain.handlerClass, result)
    }

    private fun isValidAnnotationElement(element: PsiElement): Boolean {
        if (element !is PsiAnnotation) return false
        return annotationService.isValidAnnotation(element)
    }


        private data class NavigationChain(val method: PsiMethod, val useCaseClass: PsiClass, val handlerClass: PsiClass)

    private fun buildNavigationChain(annotation: PsiAnnotation): NavigationChain? {
        val method = getMethodFromAnnotation(annotation) ?: return null
        val useCaseClass = findUseCaseClass(method) ?: return null
        val handlerClass = findHandlerClass(useCaseClass, method.project) ?: return null

        return NavigationChain(method, useCaseClass, handlerClass)
    }

    private fun getMethodFromAnnotation(annotation: PsiAnnotation): PsiMethod? {
        return annotationService.getMethodFromAnnotation(annotation)
    }

    private fun findUseCaseClass(method: PsiMethod): PsiClass? {
        return useCaseService.findUseCaseClass(method)
    }

    private fun findHandlerClass(useCaseClass: PsiClass, project: Project): PsiClass? {
        return handlerService.findHandlerClass(useCaseClass, project)
    }

    private fun addNavigationMarker(
        element: PsiAnnotation,
        handlerClass: PsiClass,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val marker = markerBuilder.buildMarker(element, handlerClass)
        result.add(marker)
    }
}
