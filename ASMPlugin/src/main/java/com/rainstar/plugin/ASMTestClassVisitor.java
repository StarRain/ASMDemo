package com.rainstar.plugin;

import com.rainstar.util.LogUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ASMTestClassVisitor extends ClassVisitor {
    private String mClassName;

    public ASMTestClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mClassName = name;
        LogUtil.log("ASMTestClassVisitor visit ClassName -> " + mClassName);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        // 处理ASMTestActivity中的testAsmAddToast
        LogUtil.log("methodName -> " + name);
        if ("com/rainstar/asm/TestASMActivity".equals(mClassName)) {
            LogUtil.log("ASMTestClassVisitor visitMethod find ASMTestActivity.");
            if ("testAsmAddToast".equals(name)) {
                LogUtil.log("ASMTestClassVisitor visitMethod find ASMTestActivity$testAsmAddToast.");
                return new ASMTestMethodVisitor(methodVisitor, access, name, descriptor);
            }
        }
        return methodVisitor;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
