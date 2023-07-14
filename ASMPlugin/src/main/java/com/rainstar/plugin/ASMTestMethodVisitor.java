package com.rainstar.plugin;

import com.rainstar.util.LogUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;


public class ASMTestMethodVisitor extends AdviceAdapter {

    public ASMTestMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(Opcodes.ASM4, methodVisitor, access, name, descriptor);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        // 方法执行开始的位置，插入Toast提示
        LogUtil.log("ASMTestMethodVisitor onMethodEnter -> execute insert toast code start.");
        Label label = new Label();
        visitLabel(label);
        visitLineNumber(46, label);
        visitVarInsn(ALOAD, 0);
        visitLdcInsn("ASM \u4fee\u6539\u6210");
        visitInsn(ICONST_0);
        visitMethodInsn(INVOKESTATIC, "android/widget/Toast", "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;", false);
        visitMethodInsn(INVOKEVIRTUAL, "android/widget/Toast", "show", "()V", false);
        LogUtil.log("ASMTestMethodVisitor onMethodEnter -> execute insert toast code end.");
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        // 方法执行完毕，准备退出
    }
}
