package com.jhontabio.minecraftreader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import org.objectweb.asm.commons.AdviceAdapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM9;

public final class MethodEnterTransformer implements ClassFileTransformer
{
  private final String className;
  private final String methodName;

  private final Method methodInject;

  public MethodEnterTransformer(String className, String methodName, Method methodInject)
  {
    this.className = className;
    this.methodName = methodName;
    this.methodInject = methodInject;
  }

  @Override
  public byte[] transform(Module module, ClassLoader loader, String className, 
                          Class<?> classBeingRedefined, ProtectionDomain pd, byte[] buf)
  {
    try
    {
      // Is class the one we are interested in?
      if(className == null || className.startsWith("com/jhontabio/minecraftreader/") ||!className.equals(this.className)) return null;

      if(loader == null) return null;

      ClassReader cr = new ClassReader(buf);
      ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
      ClassVisitor cv = new ClassVisitor(ASM9, cw)
      {
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
        {
          MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

          if((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) return mv;

          if(!name.equals(methodName)) return mv;

          return new AdviceAdapter(ASM9, mv, access, name, desc)
          {
            private final String owner = className;
            private final String mName = name;
            private final String mDesc = desc;

            @Override
            protected void onMethodEnter()
            {
              visitLdcInsn(owner);
              visitLdcInsn(mName);
              visitLdcInsn(mDesc);

              visitMethodInsn(INVOKESTATIC, Type.getInternalName(methodInject.getDeclaringClass()), methodInject.getName(), Type.getMethodDescriptor(methodInject), false);
            }
          };
        }
      };

      cr.accept(cv, ClassReader.EXPAND_FRAMES);
      return cw.toByteArray();
    }
    catch(Throwable t){ return buf; }
  }
}
