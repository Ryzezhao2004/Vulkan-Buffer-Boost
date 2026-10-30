package com.vulkanbufferboost.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class StagingBufferSizeTransformer {

    private StagingBufferSizeTransformer() {
    }

    public static byte[] patch(final byte[] original, final int sourceSize, final int targetSize) {
        final ClassReader reader = new ClassReader(original);
        final ClassWriter writer = new ClassWriter(reader, 0);
        final int[] hits = {0};

        final ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(final int access,
                                            final String name,
                                            final String descriptor,
                                            final String signature,
                                            final String[] exceptions) {
                final MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (delegate == null) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM9, delegate) {
                    @Override
                    public void visitLdcInsn(final Object value) {
                        if (value instanceof Integer constant && constant == sourceSize) {
                            hits[0]++;
                            super.visitLdcInsn(targetSize);
                        } else {
                            super.visitLdcInsn(value);
                        }
                    }
                };
            }
        };

        reader.accept(visitor, 0);

        if (hits[0] == 0) {
            return null;
        }
        return writer.toByteArray();
    }
}
