package com.jhontabio.minecraftreader;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
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

  // TEMP OBF NAMES
  private static final String CLIENT_NAME = "fzz";
  private static final String CLIENT_GET_INSTANCE_NAME = "W";
  private static final String COMMAND_CLASS_NAME = "ek";
  private static final String COMMAND_NAME = "a";
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
      ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
      if(clsLoader == null) clsLoader = ClassLoader.getSystemClassLoader();

      // Fetch these from the Mojang provided mappings
      Class<?> clientCls = Class.forName(CLIENT_NAME, false, clsLoader); // net.minecraft.client.Minecraft Class
      Method getClientInstance = clientCls.getDeclaredMethod(CLIENT_GET_INSTANCE_NAME); // getInstance() -> Minecraft Method
      getClientInstance.setAccessible(true);
      Object mineClient = getClientInstance.invoke(null);

      if(mineClient == null)
      {
        print("Unable to get Minecraft Client");
        return;
      }

      print("Minecraft Client got!");

      Class<?> commandCls = Class.forName(COMMAND_CLASS_NAME, false, clsLoader);
      Field cmdField = commandCls.getField(COMMAND_NAME); // This field is a public static field

      Object commandPrefixObj = cmdField.get(null);

      // Ensuring we acces what we expect
      int cmdFieldMods = cmdField.getModifiers();
      if(!Modifier.isStatic(cmdFieldMods) || !Modifier.isFinal(cmdFieldMods))
      {
        print("ERROR: Command field 'a' is not static final");
        commandPrefixObj = null;
      }
      else commandPrefixObj = cmdField.get(null);

      final String COMMAND_PREFIX = commandPrefixObj != null ? (String) commandPrefixObj : null;

      print("Command prefix got: '" + COMMAND_PREFIX + "'");
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
