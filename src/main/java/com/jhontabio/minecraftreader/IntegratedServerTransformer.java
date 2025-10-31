package com.jhontabio.minecraftreader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static org.objectweb.asm.Opcodes.ASM9;

public final class IntegratedServerTransformer implements ClassFileTransformer
{
  private final String minecraftserver_name;
  private final String minecraftserver_init;
  private final String minecraftserver_stop;

  public IntegratedServerTransformer(String name, String init, String stop)
  {
    this.minecraftserver_name = name.replace(".", "/");
    this.minecraftserver_init = init;
    this.minecraftserver_stop = stop;
  }

  @Override
  public byte[] transform(Module module, ClassLoader loader, String className, 
                          Class<?> classBeingRedefined, ProtectionDomain pd, byte[] buf)
  {
    try
    {
      // Is class the one we are interested in?
      if(className == null || className.startsWith("com/jhontabio/minecraftreader/") ||!className.equals(minecraftserver_name)) return null;

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

          if(!name.equals(minecraftserver_init) && !name.equals(minecraftserver_stop)) return mv;

          return new AdviceAdapter(ASM9, mv, access, name, desc)
          {
            private final String owner = className;
            private final String mName = name;
            private final String mDesc = desc;

            private Label startLabel = new Label();
            private Label endLabel = new Label();

            @Override
            public void visitCode()
            {
              super.visitCode();
              visitLabel(startLabel);
            }

            @Override
            protected void onMethodEnter()
            {
              Silverfish.SILVERFISH_STREAM.println("MehtodEnter");
              visitLdcInsn(owner);
              visitLdcInsn(mName);
              visitLdcInsn(mDesc);

              visitMethodInsn(INVOKESTATIC, "com/jhontabio/minecraftreader/Silverfish", "initServer", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
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
