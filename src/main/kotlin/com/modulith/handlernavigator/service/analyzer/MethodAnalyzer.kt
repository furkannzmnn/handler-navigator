package com.modulith.handlernavigator.service.analyzer

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.modulith.handlernavigator.service.detector.UseCaseDetector

class MethodAnalyzer(private val useCaseDetector: UseCaseDetector = UseCaseDetector()) {
    fun findUseCaseClasses(method: PsiMethod): List<PsiClass> {
        val methodBody = method.body ?: return emptyList()
        val potentialUseCaseClasses = mutableListOf<PsiClass>()

        findUseCaseFromMethodCalls(methodBody, potentialUseCaseClasses)
        findUseCaseFromParameters(method, potentialUseCaseClasses)
        findUseCaseFromMethodReturnTypes(methodBody, method.project, potentialUseCaseClasses)

        return potentialUseCaseClasses
    }

    private fun findUseCaseFromMethodCalls(methodBody: PsiCodeBlock, potentialUseCaseClasses: MutableList<PsiClass>) {
        val methodCalls = PsiTreeUtil.findChildrenOfType(methodBody, PsiMethodCallExpression::class.java)
        methodCalls.forEach { methodCall ->
            processMethodCallReturnType(methodCall, potentialUseCaseClasses)
            processMethodCallArguments(methodCall, potentialUseCaseClasses)
        }

        processNewExpressions(methodBody, potentialUseCaseClasses)
    }

    private fun processMethodCallReturnType(methodCall: PsiMethodCallExpression, potentialUseCaseClasses: MutableList<PsiClass>) {
        val returnType = methodCall.type
        if (returnType is PsiClassType) {
            val resolvedClass = returnType.resolve()
            if (resolvedClass != null && useCaseDetector.isUseCase(resolvedClass)) {
                potentialUseCaseClasses.add(resolvedClass)
            }
        }
    }

    private fun processMethodCallArguments(methodCall: PsiMethodCallExpression, potentialUseCaseClasses: MutableList<PsiClass>) {
        methodCall.argumentList.expressions.forEach { arg ->
            when (arg) {
                is PsiNewExpression -> processNewExpression(arg, potentialUseCaseClasses)
                is PsiRecordComponent -> processRecordComponent(arg, potentialUseCaseClasses)
                is PsiMethodCallExpression -> processBuilderPattern(arg, potentialUseCaseClasses)
            }
        }
    }

    private fun processNewExpression(newExpr: PsiNewExpression, potentialUseCaseClasses: MutableList<PsiClass>) {
        val resolvedClass = newExpr.classReference?.resolve() as? PsiClass
        if (resolvedClass != null && useCaseDetector.isUseCase(resolvedClass)) {
            potentialUseCaseClasses.add(resolvedClass)
        }
    }

    private fun processRecordComponent(recordComponent: PsiRecordComponent, potentialUseCaseClasses: MutableList<PsiClass>) {
        val recordClass = recordComponent.containingClass
        if (recordClass != null && useCaseDetector.isUseCase(recordClass)) {
            potentialUseCaseClasses.add(recordClass)
        }
    }

    private fun processBuilderPattern(methodCall: PsiMethodCallExpression, potentialUseCaseClasses: MutableList<PsiClass>) {
        if (methodCall.methodExpression.referenceName == "build") {
            val qualifier = methodCall.methodExpression.qualifierExpression
            if (qualifier is PsiMethodCallExpression && qualifier.methodExpression.referenceName == "builder") {
                val resolvedClass = qualifier.methodExpression.resolve() as? PsiMethod
                resolvedClass?.containingClass?.let { builderClass ->
                    if (useCaseDetector.isUseCase(builderClass)) {
                        potentialUseCaseClasses.add(builderClass)
                    }
                }
            }
        }
    }

    private fun processNewExpressions(methodBody: PsiCodeBlock, potentialUseCaseClasses: MutableList<PsiClass>) {
        val newExpressions = PsiTreeUtil.findChildrenOfType(methodBody, PsiNewExpression::class.java)
        newExpressions.forEach { newExpr ->
            processNewExpression(newExpr, potentialUseCaseClasses)
        }
    }

    private fun findUseCaseFromParameters(method: PsiMethod, potentialUseCaseClasses: MutableList<PsiClass>) {
        method.parameterList.parameters.forEach { parameter ->
            val paramType = parameter.type as? PsiClassType
            paramType?.let {
                val resolvedClass = it.resolve()
                if (resolvedClass != null && useCaseDetector.isUseCase(resolvedClass)) {
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }
        }
    }

    private fun findUseCaseFromMethodReturnTypes(methodBody: PsiCodeBlock, project: Project, potentialUseCaseClasses: MutableList<PsiClass>) {
        PsiTreeUtil.findChildrenOfType(methodBody, PsiMethod::class.java).forEach { psiMethod ->
            val returnType = psiMethod.returnType
            returnType?.let {
                val resolvedClass = it.canonicalText.let { canonicalText ->
                    PsiType.getTypeByName(canonicalText, project, psiMethod.resolveScope)
                }.resolve()
                if (resolvedClass != null && useCaseDetector.isUseCase(resolvedClass)) {
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }
        }
    }
}