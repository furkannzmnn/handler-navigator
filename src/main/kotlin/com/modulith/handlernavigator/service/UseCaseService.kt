package com.modulith.handlernavigator.service

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class UseCaseService {

    fun findUseCaseClass(method: PsiMethod): PsiClass? {
        val methodBody = method.body ?: return null
        val potentialUseCaseClasses = mutableListOf<PsiClass>()

        findUseCaseFromMethodCalls(methodBody, potentialUseCaseClasses)
        findUseCaseFromParameters(method, potentialUseCaseClasses)
        findUseCaseFromMethodReturnTypes(methodBody, method.project, potentialUseCaseClasses)

        return potentialUseCaseClasses.firstOrNull()
    }

    private fun findUseCaseFromMethodCalls(methodBody: PsiCodeBlock, potentialUseCaseClasses: MutableList<PsiClass>) {
        val methodCalls = PsiTreeUtil.findChildrenOfType(methodBody, PsiMethodCallExpression::class.java)
        methodCalls.forEach { methodCall ->
            val returnType = methodCall.type
            if (returnType is PsiClassType) {
                val resolvedClass = returnType.resolve()
                if (resolvedClass != null && isUseCase(resolvedClass)) {
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }


            methodCall.argumentList.expressions.forEach { arg ->
                if (arg is PsiNewExpression) {
                    val resolvedClass = arg.classReference?.resolve() as? PsiClass
                    if (resolvedClass != null && isUseCase(resolvedClass)) {
                        potentialUseCaseClasses.add(resolvedClass)
                    }
                }

                if (arg is PsiRecordComponent) {
                    val recordClass = arg.containingClass
                    if (recordClass != null && isUseCase(recordClass)) {
                        potentialUseCaseClasses.add(recordClass)
                    }
                }
                

                // Eğer argüman bir method çağrısıysa (builder().build() gibi)
                if (arg is PsiMethodCallExpression) {
                    val methodRef = arg.methodExpression.referenceName
                    if (methodRef == "build") { // builder().build() çağrısını yakala
                        val qualifier = arg.methodExpression.qualifierExpression
                        if (qualifier is PsiMethodCallExpression) {
                            val builderMethodRef = qualifier.methodExpression.referenceName
                            if (builderMethodRef == "builder") { // builder() çağrısını analiz et
                                val resolvedClass = qualifier.methodExpression.resolve() as? PsiMethod
                                resolvedClass?.containingClass?.let { builderClass ->
                                    if (isUseCase(builderClass)) {
                                        potentialUseCaseClasses.add(builderClass)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val newExpressions = PsiTreeUtil.findChildrenOfType(methodBody, PsiNewExpression::class.java)
        newExpressions.forEach { newExpr ->
            val resolvedClass = newExpr.classReference?.resolve() as? PsiClass
            if (resolvedClass != null && isUseCase(resolvedClass)) {
                potentialUseCaseClasses.add(resolvedClass)
            }
        }
    }

    private fun findUseCaseFromParameters(method: PsiMethod, potentialUseCaseClasses: MutableList<PsiClass>) {
        method.parameterList.parameters.forEach { parameter ->
            val paramType = parameter.type as? PsiClassType
            paramType?.let {
                val resolvedClass = it.resolve()
                if (resolvedClass != null && isUseCase(resolvedClass)) {
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
                if (resolvedClass != null && isUseCase(resolvedClass)) {
                    potentialUseCaseClasses.add(resolvedClass)
                }
            }
        }
    }

    private fun isUseCase(psiClass: PsiClass?): Boolean {
        if (psiClass == null) return false

        val isBaseUseCase = psiClass.interfaces.any { it.name == "UseCase" } || psiClass.superClass?.name == "Message"

        val hasBuilder = psiClass.annotations.any { it.qualifiedName?.contains("Builder") == true }
        val hasPrivateFields = psiClass.fields.any { it.hasModifierProperty(PsiModifier.PRIVATE) }


        val isRecordClass = psiClass.isRecord
        return isBaseUseCase || (hasBuilder && hasPrivateFields) || isRecordClass
    }
}