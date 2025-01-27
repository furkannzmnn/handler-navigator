package com.modulith.handlernavigator

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.IconManager
import javax.swing.Icon


class HandlerNavigatorProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        println("collectNavigationMarkers called")

        if (element !is PsiAnnotation) {
            println("Element is not PsiAnnotation, returning early")
            return
        }

        val annotationName = element.qualifiedName ?: run {
            println("Annotation name is null, returning early")
            return
        }

        if (!isTargetAnnotation(annotationName)) {
            println("Annotation is not a target annotation, returning early")
            return
        }

        val method = element.parent.parent as? PsiMethod ?: run {
            println("Method is null or not a PsiMethod, returning early")
            return
        }

        println("Found method: ${method.name}")

        val useCaseClass = findUseCaseClass(method) ?: run {
            println("No UseCase class found, returning early")
            return
        }

        println("Found UseCase class: ${useCaseClass.name}")

        val handlerClass = findHandlerClass(useCaseClass, method.project) ?: run {
            println("No handler class found, returning early")
            return
        }

        println("Found handler class: ${handlerClass.name}")

        val builder = NavigationGutterIconBuilder.create(getNavigationIcon())
            .setTarget(handlerClass)
            .setTooltipText("Navigate to Handler")

        result.add(builder.createLineMarkerInfo(element))
        println("Added navigation marker")
    }

    private fun isTargetAnnotation(annotationName: String): Boolean {
        val result = annotationName == "org.springframework.web.bind.annotation.PostMapping" ||
                annotationName == "org.springframework.web.bind.annotation.GetMapping"
        println("isTargetAnnotation result: $result")
        return result
    }

    private fun findUseCaseClass(method: PsiMethod): PsiClass? {
        println("findUseCaseClass called for method: ${method.name}")

        val methodBody = method.body ?: run {
            println("Method body is null")
            return null
        }

        val potentialUseCaseClasses = mutableListOf<PsiClass>()
        // Method gövdesindeki tüm method çağrılarını buluyoruz
        val methodCalls = PsiTreeUtil.findChildrenOfType(methodBody, PsiMethodCallExpression::class.java)
        println("Method calls found in method body: ${methodCalls.size}")


        methodCalls.forEach { methodCall ->
            println("Analyzing method call: ${methodCall.text}")

            // Methodun döndüğü türü bul
            val returnType = methodCall.type
            println("Return type of method call: ${returnType?.canonicalText}")

            if (returnType is PsiClassType) {
                val resolvedClass = returnType.resolve()
                if (resolvedClass != null && isUseCase(resolvedClass)) {
                    println("Method call return type ${returnType.canonicalText} is a UseCase")
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }
        }

        method.parameterList.parameters.forEach { parameter ->
            println("Checking parameter: ${parameter.name} with type ${parameter.type}")
            val paramType = parameter.type as? PsiClassType
            paramType?.let {
                // PsiClassType'i çözümlemek için doğru erişimi kullanıyoruz
                val resolvedClass = it.resolve()
                if (resolvedClass != null && isUseCase(resolvedClass)) {
                    println("Parameter type ${it.canonicalText} is a UseCase")
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }
        }

        PsiTreeUtil.findChildrenOfType(methodBody, PsiMethod::class.java).forEach { psiMethod ->
            val returnType = psiMethod.returnType
            println("Checking method: ${psiMethod.name} with return type ${returnType?.canonicalText}")
            returnType?.let {
                // Return type çözümü için doğru erişim
                val resolvedClass =
                    it.canonicalText.let { canonicalText -> PsiType.getTypeByName(canonicalText, method.project, method.resolveScope) }
                        .resolve()
                if (resolvedClass != null && isUseCase(resolvedClass)) {
                    println("Return type ${it.canonicalText} is a UseCase")
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }
        }

        return potentialUseCaseClasses.firstOrNull().also {
            println("Found UseCase class: ${it?.name ?: "None"}")
        }
    }



    private fun isUseCase(psiClass: PsiClass?): Boolean {
        if (psiClass == null) {
            println("PsiClass is null, not a UseCase")
            return false
        }
        val isUseCase = psiClass.interfaces.any { it.name == "UseCase" }
        println("isUseCase result for class ${psiClass.name}: $isUseCase")
        return isUseCase
    }

    private fun findHandlerClass(useCaseClass: PsiClass, project: Project): PsiClass? {
        println("findHandlerClass called for UseCase class: ${useCaseClass.name}")

        val scope = GlobalSearchScope.everythingScope(project)
        val handlerClassName = findHandlerName(useCaseClass)

        println("Searching for handler class: $handlerClassName")

        val classes = PsiShortNamesCache.getInstance(project).getClassesByName(handlerClassName, scope)
        println("Found ${classes.size} classes with name $handlerClassName")

        return classes.firstOrNull().also {
            println("Found handler class: ${it?.name ?: "None"}")
        }
    }

    private fun findHandlerName(useCaseClass: PsiClass): String {
        println("findHandlerName called for UseCase class: ${useCaseClass.name}")
        val handlerSuffix = "Handler"
        val useCaseName = useCaseClass.name ?: return ""
        val handlerName = useCaseName + handlerSuffix
        println("Handler name: $handlerName")
        return handlerName
    }

    private fun getNavigationIcon(): Icon {
        println("Getting navigation icon")
        return IconManager.getInstance().getIcon("AllIcons.General.Add", javaClass)
    }
}
