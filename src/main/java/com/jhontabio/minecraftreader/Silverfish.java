package com.jhontabio.minecraftreader;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
// Debug Terminal imports
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
// Debug Terminal imports

public class Silverfish
{
  private static Instrumentation instrumentation = null;
  // DEBUG USE
  private static final PrintStream SILVERFISH_STREAM = createStream();

  public static void premain(String arg, Instrumentation inst)
  {
    //displayLoggerToConsole();
    instrumentation = inst;
    print("Hello before anything else from " + arg);
    spawn();
  }

  public static void agentmain(String arg)
  {
    print("Hello after main from " + arg);
  }

  private static void spawn()
  {
    print("Starting infestation to the game");

    Executors.newSingleThreadScheduledExecutor(s -> {
      Thread t = new Thread(s, "Silverfish");
      t.setDaemon(true);
      return t;
    }).scheduleAtFixedRate(Silverfish::infestGame, 3, 2, TimeUnit.SECONDS);
  }

  private static void infestGame()
  {
    print("Burrowing in to the game");

    try
    {
      // Fetch these from the Mojang provided mappings
      Class<?> clientCls = Class.forName("fzz", false, Thread.currentThread().getContextClassLoader()); // net.minecraft.client.Minecraft Class
      Method getClientInstance = clientCls.getDeclaredMethod("W"); // getInstance() -> Minecraft Method
      getClientInstance.setAccessible(true);
      Object mineClient = getClientInstance.invoke(null);

      if(mineClient == null)
      {
        print("Unable to get Minecraft Client");
        return;
      }
    }
    catch(ClassNotFoundException e)
    {
      print("Class not found: " + e);
      return;
    }
    catch(Exception e)
    {
      print("Error: " + e);
      return;
    }

    print("Minecraft Client got!");
  }

  // DEBUG USE
  // Windows only solution to create a new PrintStream using Windows' special device name for the active console's output
  private static PrintStream createStream()
  {
    if(!System.getProperty("os.name").contains("Win")) return System.out;
    try{ return new PrintStream(new FileOutputStream("CONOUT$"), true, StandardCharsets.UTF_8); }
    catch(Exception e){ return System.out; }
  }

  private static void print(String str)
  {
    SILVERFISH_STREAM.println("[Silverfish] " + str);
  }

  // Windows only solution to display our contents to the terminal
  private static void displayLoggerToConsole()
  {
    if(!System.getProperty("os.name").contains("Win")) return;
    try
    {
      // Bind System.out/err to the active console buffer explicitly
      // Minecraft seems to have their stdout/stderr created as NUL / points it to the log file
      PrintStream out = new PrintStream(new FileOutputStream("CONOUT$"), true, StandardCharsets.UTF_8);
      System.setOut(out);
      System.setErr(out);
      System.out.println("[Silverfish] Bound out/err to console buffer");
    }
    catch(FileNotFoundException e)
    {
      // No console buffer exists
      System.err.println("[Silverfish] No console buffer is available.");
    }
    catch(Exception e) { e.printStackTrace(); }
  }
}
