package com.rainstar.plugin;

import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.rainstar.util.LogUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ASMTransform extends Transform {

    /**
     * Transform 的名称
     *
     * @return
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 输入源，class文件
     *
     * @return
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 文件范围，整个工程
     *
     * @return
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 是否增量编译，可用于编译优化
     *
     * @return
     */
    @Override
    public boolean isIncremental() {
        return false;
    }

    /**
     * 核心方法
     *
     * @param transformInvocation the invocation object containing the transform inputs.
     * @throws TransformException
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        LogUtil.log("ASMTransform transform start.");
        if (!transformInvocation.isIncremental()) {
            // 不是增量编译删除所有的outputProvider
            transformInvocation.getOutputProvider().deleteAll();
        }

        // 获取输入源(包含项目中的Class文件与依赖的JAR包/AAR包中的Class文件)
        Collection<TransformInput> allInputs = transformInvocation.getInputs();
        // 遍历输入源
        LogUtil.log("ASMTransform transform allInputs.forEach.");
        allInputs.forEach(transformInput -> {
            // 获取Class文件
            Collection<DirectoryInput> directoryInputs = transformInput.getDirectoryInputs();
            LogUtil.log("ASMTransform transform directoryInputs.forEach.");
            directoryInputs.forEach(new Consumer<DirectoryInput>() {
                @Override
                public void accept(DirectoryInput directoryInput) {
                    LogUtil.log("ASMTransform transform directoryInputs.forEach.accept.");
                    // 处理输入源
                    handleDirectoryInput(directoryInput);
                    // 获取output目录
                    File dest = transformInvocation.getOutputProvider().getContentLocation(
                            directoryInput.getName(),
                            directoryInput.getContentTypes(),
                            directoryInput.getScopes(),
                            Format.DIRECTORY);
                    // 这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                    try {
                        FileUtils.copyDirectory(directoryInput.getFile(), dest);
                    } catch (IOException e) {
                        System.out.println("output copy error:" + e.getMessage());
                    }
                }
            });

            // 获取依赖的JAR包/AAR包中的Class文件
            Collection<JarInput> jarInputs = transformInput.getJarInputs();

        });
        LogUtil.log("ASMTransform transform end.");
    }

    /**
     * 处理项目中的Class文件
     *
     * @param directoryInput
     */
    private void handleDirectoryInput(DirectoryInput directoryInput) {
        LogUtil.log("handleDirectoryInput start");
        try {
            // 列出目录中所有文件（包含子文件夹，子文件夹内文件）
            List<File> files = new ArrayList<>();
            listFiles(directoryInput.getFile(), files);
            for (File file : files) {
                LogUtil.log(file.getAbsolutePath());
                ClassReader classReader = new ClassReader(new FileInputStream(file.getAbsolutePath()));
                // 接收Flag参数，用于设置方法的操作数栈的深度。COMPUTE_MAXS可以自动帮我们计算stackSize
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                // 接收api与ClassVisitor。 Opcodes.ASM4~Opcodes.ASM9标识了ASM的版本信息
                ASMTestClassVisitor asmTestClassVisitor = new ASMTestClassVisitor(Opcodes.ASM5, classWriter);
                // 接收ClassVisitor与parsingOptions参数。
                // parsingOptions用来决定解析Class的方式，SKIP_FRAMES代表跳过MethodVisitor.visitFrame方法
                classReader.accept(asmTestClassVisitor, ClassReader.SKIP_FRAMES);

                byte[] code = classWriter.toByteArray();
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                fos.write(code);
                fos.close();
            }
        } catch (IOException e) {
            LogUtil.log("handleDirectoryInput error:" + e.getMessage());
        }
        LogUtil.log("handleDirectoryInput end");
    }

    /**
     * 列出目录中所有文件（包含子文件夹，子文件夹内文件）
     *
     * @param file
     * @param fileList
     * @return
     */
    private List<File> listFiles(File file, List<File> fileList) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File childFile : files) {
                listFiles(childFile, fileList);
            }
        } else {
            if (checkClassFile(file.getName())) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * 检查class文件是否需要处理
     *
     * @param fileName
     * @return
     */
    private boolean checkClassFile(String fileName) {
        if (!fileName.endsWith(".class") || fileName.equals("BuildConfig.class")
                || fileName.equals("R.class") || fileName.startsWith("R$")) {
            // 排除非class文件、R.class文件、BuildConfig.class文件及资源文件(R$layout.class)
            return false;
        }
        LogUtil.log("\"" + fileName + "\".endsWith(\"TestASMActivity.calss\") || \"" + fileName + "\".endsWith(\"TestASMActivity.calss\")");
        // 只处理需要的class文件
        return fileName.endsWith("TestASMActivity.class")
                || fileName.endsWith("TestASMActivity$1.class");
    }
}
