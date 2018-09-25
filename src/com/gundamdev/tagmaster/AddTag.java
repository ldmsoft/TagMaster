package com.gundamdev.tagmaster;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * TAG MASTER
 *
 * @author GundamDev 2018年9月22日
 */
public class AddTag extends AnAction {

    public static final String TAG_TEMPLATE = "private final static String TAG = %s.class.getSimpleName();";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        String className = psiFile.getName().split("\\.")[0];
        String fileRawName = String.format(TAG_TEMPLATE, className);

        Editor editor = anActionEvent.getData(DataKeys.EDITOR);
        TagCreator tagCreator = new TagCreator(fileRawName, psiFile.getProject(),
                getTargetClass(editor, psiFile), psiFile);
        tagCreator.execute();
    }

    public static class TagCreator extends WriteCommandAction.Simple {
        private PsiElementFactory mFactory;
        private PsiClass mClass;
        private Project mProject;
        private PsiFile mFile;
        private String mCommand;

        protected TagCreator(String command, Project project, PsiClass psiClass, PsiFile... files) {
            super(project, files);
            mProject = project;
            mFile = files[0];
            mFactory = JavaPsiFacade.getElementFactory(mProject);
            mClass = psiClass;
            mCommand = command;
        }


        @Override
        protected void run() throws Throwable {
            PsiField fieldFromText = mFactory.createFieldFromText(mCommand, mClass);
            mClass.add(fieldFromText);
        }

    }

    /**
     * 根据当前文件获取对应的class文件
     *
     * @param editor
     * @param file
     * @return
     */
    private PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }
}

