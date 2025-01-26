package com.modulith.handlernavigator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ide.highlighter.JavaFileType



class ClassFinderAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.project ?: return

        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        val classNames = mutableListOf<String>()

        for (file: VirtualFile in javaFiles) {
            val psiFile: PsiFile? = PsiManager.getInstance(project).findFile(file)
            if (psiFile != null) {
                val classes = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
                for (psiClass in classes) {
                    psiClass.name?.let { classNames.add(it) }
                }
            }
        }

        if (classNames.isEmpty()) {
            Messages.showInfoMessage("No Java classes found.", "Result")
        } else {
            Messages.showInfoMessage(classNames.joinToString("\n"), "Found Java Classes")
        }
    }

}