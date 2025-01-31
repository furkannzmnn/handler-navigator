package com.modulith.handlernavigator.service.detector

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier

class UseCaseDetector {
    fun isUseCase(psiClass: PsiClass?): Boolean {
        if (psiClass == null) return false

        return isBaseUseCase(psiClass) ||
                isBuilderPatternUseCase(psiClass) ||
                psiClass.isRecord
    }

    private fun isBaseUseCase(psiClass: PsiClass): Boolean {
        return psiClass.interfaces.any { it.name == "UseCase" } ||
                psiClass.superClass?.name == "Message"
    }

    private fun isBuilderPatternUseCase(psiClass: PsiClass): Boolean {
        val hasBuilder = psiClass.annotations.any { it.qualifiedName?.contains("Builder") == true }
        val hasPrivateFields = psiClass.fields.any { it.hasModifierProperty(PsiModifier.PRIVATE) }
        return hasBuilder && hasPrivateFields
    }
}