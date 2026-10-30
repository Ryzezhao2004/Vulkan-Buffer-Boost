package com.vulkanbufferboost.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class StagingBufferSizeTransformerVerifier {

    private static final int SOURCE = 102_760_448;
    private static final int TARGET = 134_217_728;
    private static int failures = 0;

    public static void main(final String[] args) {
        check("替换两处且不动其它常量", () -> {
            final byte[] patched = StagingBufferSizeTransformer.patch(makeDummy(), SOURCE, TARGET);
            assertTrue(patched != null, "应当检测到常量并完成改写");
            assertEquals(0, countLdc(patched, SOURCE), "原始常量应被完全替换");
            assertEquals(2, countLdc(patched, TARGET), "应恰好改写 2 处");
            assertEquals(1, countLdc(patched, 33_554_432), "不相关常量不应被改动");
        });

        check("幂等（已替换后再次运行返回 null）", () -> {
            final byte[] once = StagingBufferSizeTransformer.patch(makeDummy(), SOURCE, TARGET);
            final byte[] twice = StagingBufferSizeTransformer.patch(once, SOURCE, TARGET);
            assertTrue(twice == null, "已无可替换常量时应返回 null");
        });

        check("无匹配时返回 null", () -> {
            final byte[] bytes = makeWithoutSource();
            assertTrue(StagingBufferSizeTransformer.patch(bytes, SOURCE, TARGET) == null, "无匹配应返回 null");
        });

        if (failures == 0) {
            System.out.println("[VERIFY] PASS —— ASM 常量替换逻辑正确");
            System.exit(0);
        } else {
            System.err.println("[VERIFY] FAIL —— " + failures + " 项未通过");
            System.exit(1);
        }
    }

    private static void check(final String name, final Runnable r) {
        try {
            r.run();
            System.out.println("[VERIFY] ok  - " + name);
        } catch (final AssertionError e) {
            failures++;
            System.err.println("[VERIFY] FAIL - " + name + " : " + e.getMessage());
        }
    }

    private static void assertTrue(final boolean cond, final String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    private static void assertEquals(final int expected, final int actual, final String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " (期望 " + expected + " 实际 " + actual + ")");
        }
    }

    private static byte[] makeDummy() {
        final ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V25, Opcodes.ACC_PUBLIC, "com/vulkanbufferboost/agent/Dummy", null, "java/lang/Object", null);
        final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "m", "()V", null, null);
        mv.visitCode();
        mv.visitLdcInsn(SOURCE);
        mv.visitLdcInsn(SOURCE);
        mv.visitLdcInsn(33_554_432);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static byte[] makeWithoutSource() {
        final ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V25, Opcodes.ACC_PUBLIC, "com/vulkanbufferboost/agent/Empty", null, "java/lang/Object", null);
        final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "m", "()V", null, null);
        mv.visitCode();
        mv.visitLdcInsn(123);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static int countLdc(final byte[] bytes, final int value) {
        final int[] count = {0};
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(final int a, final String n, final String d, final String s, final String[] e) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitLdcInsn(final Object v) {
                        if (v instanceof Integer i && i == value) {
                            count[0]++;
                        }
                    }
                };
            }
        }, 0);
        return count[0];
    }
}
