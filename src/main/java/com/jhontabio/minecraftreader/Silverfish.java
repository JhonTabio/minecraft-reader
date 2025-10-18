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
  private static final String CLIENT_GET_INTEGRATED_SERVER_INSTANCE = "ab";
  private static final String MINECRAFTSERVER_NAME = "net.minecraft.server.MinecraftServer";
  private static final String MINECRAFTSERVER_GET_COMMANDS_METHOD = "aJ";
  private static final String COMMAND_CLASS_NAME = "ek";
  private static final String COMMAND_PREFIX_NAME = "a";
  private static final String COMMAND_GET_DISPATCHER_NAME = "a";
  private static final String COMMAND_DISPATCHER_CLASS_NAME = "ej";
  private static final String COMMAND_DISPATCHER_NAME = "j";
  private static final String COMMAND_GET_SOURCE_STACK = "aK";
  // TEMP OBF NAMES

  // DEBUG USE
  private static final PrintStream SILVERFISH_STREAM = createStream();
  // DEBUG USE

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

    // Run a new thread as to not hinder the game process
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

      int clientClsMods = clientCls.getModifiers();
      if(!Modifier.isPublic(clientClsMods))
      {
        print(String.format("ERROR: Class '%s' is not public", CLIENT_NAME));
        return;
      }

      Method getClientInstance = clientCls.getMethod(CLIENT_GET_INSTANCE_NAME); // getInstance() -> Minecraft Method

      int getClientInstanceMods = getClientInstance.getModifiers();
      if(!Modifier.isPublic(getClientInstanceMods) || !Modifier.isStatic(getClientInstanceMods))
      {
        print(String.format("ERROR: Method '%s' is not public", CLIENT_NAME));
        return;
      }

      Object mineClient = getClientInstance.invoke(null);

      if(mineClient == null)
      {
        print("Unable to get Minecraft Client");
        return;
      }

      print("Minecraft Client got!");

      Method getIntegratedServerInstance = clientCls.getMethod(CLIENT_GET_INTEGRATED_SERVER_INSTANCE);

      int getIntegratedServerInstanceMods = getIntegratedServerInstance.getModifiers();

      if(!Modifier.isPublic(getIntegratedServerInstanceMods))
      {
        print(String.format("ERROR: Get Integrated server method '%s' is not public", CLIENT_GET_INTEGRATED_SERVER_INSTANCE));
        return;
      }

      print("Minecraft integrated server instance method got!");
      Object integratedServer = getIntegratedServerInstance.invoke(mineClient);

      if(integratedServer == null)
      {
        print("Single player integrated server is not running...");
        return;
      }
      print("Minecraft integrated server got!");

      Class<?> serverCls = Class.forName(MINECRAFTSERVER_NAME, false, clsLoader);
      print("Got server class: " + serverCls);

      Method serverGetCMDS = serverCls.getMethod(MINECRAFTSERVER_GET_COMMANDS_METHOD);

      int serverGetCMDSMods = serverGetCMDS.getModifiers();

      if(!Modifier.isPublic(serverGetCMDSMods))
      {
        print(String.format("ERROR: Get server command method '%s' is not public", MINECRAFTSERVER_GET_COMMANDS_METHOD));
        return;
      }
      print("Got server commands method: " + serverGetCMDS);

      Object serverCMDS = serverGetCMDS.invoke(integratedServer);
      print("Got server commands: " + serverCMDS);

      if(serverCMDS == null)
      {
        print("Could not retrieve server commands");
        return;
      }

      Class<?> cmdCls = Class.forName(COMMAND_CLASS_NAME, false, clsLoader);
      Field cmdPrefixField = cmdCls.getField(COMMAND_PREFIX_NAME); // This field is a public static field
      print("Got server commands field: " + cmdPrefixField);

      Object cmdPrefixObj = cmdPrefixField.get(serverCMDS);
      print("Got server commands object: " + cmdPrefixObj);

      // Ensuring we acces what we expect
      int cmdFieldMods = cmdPrefixField.getModifiers();
      if(!Modifier.isStatic(cmdFieldMods) || !Modifier.isFinal(cmdFieldMods))
      {
        print(String.format("ERROR: Command field '%s' is not static final", COMMAND_PREFIX_NAME));
        return;
      }

      final String COMMAND_PREFIX = cmdPrefixObj != null ? (String) cmdPrefixObj : null;

      print("Command prefix got: '" + COMMAND_PREFIX + "'");

      Method cmdGetDispatcher = cmdCls.getMethod(COMMAND_GET_DISPATCHER_NAME);

      int cmdGetDispatcherMods = cmdGetDispatcher.getModifiers();
      if(!Modifier.isPublic(cmdGetDispatcherMods))
      {
        print(String.format("ERROR: Command get dispatcher '%s' is not public", COMMAND_GET_DISPATCHER_NAME));
        return;
      }

      Object cmdDispatcher = cmdGetDispatcher.invoke(serverCMDS);

      if(cmdDispatcher == null)
      {
        print("Could not retrieve command dispatcher");
        return;
      }
      print("Command dispatcher got");

      Method cmdGetSourceStack = integratedServer.getClass().getMethod(COMMAND_GET_SOURCE_STACK);

      int cmdGetSourceStackMods = cmdGetSourceStack.getModifiers();
      if(!Modifier.isPublic(cmdGetSourceStackMods))
      {
        print(String.format("ERROR: Command get source stack '%s' is not public", COMMAND_GET_SOURCE_STACK));
        return;
      }

      Object cmdSourceStack = cmdGetSourceStack.invoke(integratedServer);

      if(cmdSourceStack == null)
      {
        print("Could not retrieve command source stack");
        return;
      }
      print("Command Source Stack got");

      Class<?> cmdDispatcherCls = cmdDispatcher.getClass();

      //list(cmdDispatcherCls);

      Class<?> cmdNodeCls = Class.forName("com.mojang.brigadier.tree.CommandNode");
      Method cmdDispatcherGetRoot = cmdDispatcherCls.getMethod("getRoot");
      Object cmdRoot = cmdDispatcherGetRoot.invoke(cmdDispatcher);
      Method cmdDispatcherGetAllUsage = cmdDispatcherCls.getMethod("getAllUsage", cmdNodeCls, Object.class, boolean.class);
      print("COMMANDS:");
      for(String u : (String[]) cmdDispatcherGetAllUsage.invoke(cmdDispatcher, cmdRoot, cmdSourceStack, true))
        print("/" + u);

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
