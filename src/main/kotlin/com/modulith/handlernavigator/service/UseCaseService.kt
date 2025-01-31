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
        return psiClass.interfaces.any { it.name == "UseCase" || it.name == "Message" }
    }
}