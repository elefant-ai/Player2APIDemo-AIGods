package com.elefantai.player2api;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;



// This class will execute code as a string.
public class CodeExecutor {
    public static void executeCode(String sourceCode) throws Exception {
        File sourceFile = File.createTempFile("Dynamic", ".java");
        try (PrintWriter writer = new PrintWriter(sourceFile)) {
            writer.println(sourceCode);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Compiler not available. Make sure you're using JDK, not JRE.");
        }

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(sourceFile));

        File classOutputDir = new File(sourceFile.getParentFile(), "classes");
        classOutputDir.mkdir();

        List<String> options = Arrays.asList("-d", classOutputDir.getAbsolutePath());
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, compilationUnits);

        boolean success = task.call();
        fileManager.close();

        if (!success) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.err.println(diagnostic);
            }
            throw new RuntimeException("Compilation failed");
        }

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classOutputDir.toURI().toURL()});
        Class<?> dynamicClass = Class.forName("com.elefantai.player2api.DynamicClass", true, classLoader);

        Method executeMethod = dynamicClass.getMethod("execute");
        executeMethod.setAccessible(true);  // BYPASSES ACCESS RESTRICTIONS
        executeMethod.invoke(null);

        sourceFile.delete();
        for (File file : classOutputDir.listFiles()) {
            file.delete();
        }
        classOutputDir.delete();
        classLoader.close();
    }
}