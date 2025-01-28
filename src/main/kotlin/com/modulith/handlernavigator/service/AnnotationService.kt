package com.modulith.handlernavigator.service

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod

class AnnotationService {
    fun isValidAnnotation(element: PsiAnnotation): Boolean {
        val annotationName = element.qualifiedName ?: return false
        return isTargetAnnotation(annotationName)
    }

    fun getMethodFromAnnotation(element: PsiAnnotation): PsiMethod? {
        return element.parent.parent as? PsiMethod
    }

    private fun isTargetAnnotation(annotationName: String): Boolean {
        return annotationName == "org.springframework.web.bind.annotation.PostMapping" ||
                annotationName == "org.springframework.web.bind.annotation.GetMapping" ||
                annotationName == "org.springframework.web.bind.annotation.PutMapping" ||
                annotationName == "org.springframework.web.bind.annotation.DeleteMapping" ||
                annotationName == "org.springframework.web.bind.annotation.PatchMapping" ||
                annotationName == "org.springframework.web.bind.annotation.RequestMapping" ||
                annotationName == "PostMapping" ||
                annotationName == "GetMapping" ||
                annotationName == "PutMapping" ||
                annotationName == "DeleteMapping" ||
                annotationName == "PatchMapping" ||
                annotationName == "RequestMapping"
    }
}