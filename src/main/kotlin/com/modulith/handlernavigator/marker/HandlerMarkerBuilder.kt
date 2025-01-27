package com.modulith.handlernavigator.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.ui.IconManager
import javax.swing.Icon

class HandlerMarkerBuilder {
    fun buildMarker(
        element: PsiAnnotation,
        handlerClass: PsiClass
    ): RelatedItemLineMarkerInfo<PsiElement> {
        return createNavigationBuilder(handlerClass)
            .createLineMarkerInfo(element)
    }

    private fun createNavigationBuilder(target: PsiClass): NavigationGutterIconBuilder<PsiElement> {
        return NavigationGutterIconBuilder.create(getNavigationIcon())
            .setTarget(target)
            .setTooltipText("Navigate to Handler")
    }

    private fun getNavigationIcon(): Icon {
        return IconManager.getInstance().getIcon("AllIcons.General.Add", javaClass)
    }
}