package com.modulith.handlernavigator.listener

import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.PsiClass
import com.modulith.handlernavigator.core.cache.PsiElementCache

class PsiTreeChangeListener : PsiTreeChangeListener {
    private val cache = PsiElementCache.getInstance()

    override fun beforeChildAddition(event: PsiTreeChangeEvent) {}
    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {}
    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {}
    override fun beforeChildMovement(event: PsiTreeChangeEvent) {}
    override fun beforeChildrenChange(event: PsiTreeChangeEvent) {}
    override fun beforePropertyChange(event: PsiTreeChangeEvent) {}

    override fun childAdded(event: PsiTreeChangeEvent) {
        handlePsiClassChange(event)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        handlePsiClassChange(event)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        handlePsiClassChange(event)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        handlePsiClassChange(event)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        handlePsiClassChange(event)
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        handlePsiClassChange(event)
    }

    private fun handlePsiClassChange(event: PsiTreeChangeEvent) {
        val element = event.child
        if (element is PsiClass) {
            // Clear cache when a class is modified to ensure fresh lookups
            cache.clearCache()
        }
    }
}