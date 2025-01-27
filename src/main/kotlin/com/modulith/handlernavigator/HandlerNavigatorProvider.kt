package com.modulith.handlernavigator


import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.IconManager
import javax.swing.Icon

class HandlerNavigatorProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is PsiAnnotation) return

        val annotationName = element.qualifiedName ?: return
        if (!isTargetAnnotation(annotationName)) return

        val method = element.parent.parent as? PsiMethod ?: return
        val useCaseClass = findUseCaseClass(method) ?: return
        val handlerClass = findHandlerClass(useCaseClass, method.project) ?: return

        val builder = NavigationGutterIconBuilder.create(getNavigationIcon())
            .setTarget(handlerClass)
            .setTooltipText("Navigate to Handler")

        result.add(builder.createLineMarkerInfo(element))
    }

    private fun isTargetAnnotation(annotationName: String): Boolean {
        return annotationName == "org.springframework.web.bind.annotation.PostMapping" ||
                annotationName == "org.springframework.web.bind.annotation.GetMapping"
    }

    private fun findUseCaseClass(method: PsiMethod): PsiClass? {
        val methodBody = method.body ?: return null
        val toUseCaseCall = PsiTreeUtil.findChildrenOfType(methodBody, PsiMethodCallExpression::class.java)
            .find { it.methodExpression.text.endsWith("toUseCase") } ?: return null

        val returnType = toUseCaseCall.type as? PsiClassType ?: return null
        return returnType.resolve()
    }

    private fun findHandlerClass(useCaseClass: PsiClass, project: Project): PsiClass? {
        val scope = GlobalSearchScope.allScope(project)
        val handlerClassName = "${useCaseClass.name}Handler"

        return JavaPsiFacade.getInstance(project)
            .findClasses(handlerClassName, scope)
            .firstOrNull { handler ->
                handler.supers.any { it.name == "UseCaseHandler" }
            }
    }

    private fun getNavigationIcon(): Icon {
        return IconManager.getInstance().getIcon("/icons/navigate.svg", javaClass)
    }
}