package com.jhontabio.minecraftreader;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
  private static Instrumentation instrumentation;

  private static ScheduledExecutorService exec;

  private static ClassLoader clsLoader;

  private static Class<?> clientCls;
  private static Class<?> serverCls;
  private static Class<?> cmdCls;
  private static Class<?> cmdDispatcherCls;
  private static Class<?> cmdNodeCls;

  private static Field cmdPrefixField;
  
  private static Method getIntegratedServerInstance;
  private static Method serverGetCMDS;
  private static Method serverInit;
  private static Method cmdGetDispatcher;
  private static Method cmdGetSourceStack;
  private static Method cmdDispatcherGetRoot;
  private static Method cmdDispatcherGetAllUsage;

  private static Object mineClient;
  private static Object integratedServer;
  private static Object serverCMDS;
  private static Object cmdDispatcher;
  private static Object cmdSourceStack;
  private static Object cmdRoot;

  private static String COMMAND_PREFIX;

  // TEMP OBF NAMES
  private static final String CLIENT_NAME = "fzz";
  private static final String CLIENT_GET_INSTANCE_NAME = "W";
  private static final String CLIENT_GET_INTEGRATED_SERVER_INSTANCE = "ab";

  private static final String MINECRAFTSERVER_NAME = "igy";
  private static final String MINECRAFTSERVER_GET_COMMANDS_METHOD = "aJ";
  private static final String MINECRAFTSERVER_INIT_METHOD = "e";
  private static final String MINECRAFTSERVER_STOP_METHOD = "z";

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
    setup(inst);
  }

  public static void agentmain(String arg, Instrumentation inst)
  {
    setup(inst);
  }

  private static void setup(Instrumentation inst)
  {
    print("Setting up entity[type=Silverfish]");

    instrumentation = inst;

    try
    {
      print("Adding method injectors");
      instrumentation.addTransformer(new MethodEnterTransformer(MINECRAFTSERVER_NAME.replace(".", "/"), MINECRAFTSERVER_INIT_METHOD, Silverfish.class.getDeclaredMethod("initServer", String.class, String.class, String.class)), true);
      instrumentation.addTransformer(new MethodEnterTransformer(MINECRAFTSERVER_NAME.replace(".", "/"), MINECRAFTSERVER_STOP_METHOD, Silverfish.class.getDeclaredMethod("stopServer", String.class, String.class, String.class)), true);
    }
    catch(NoSuchMethodException e)
    {
      print(String.format("Error: Could not find method '%s'", e.getMessage()));
      return;
    }

    // Run a new thread as to not hinder the game process
    new Thread(() -> {
      spawn();
    }, "Silverfish"){{setDaemon(true);}}.start();
  }

  public static void initServer(String owner, String name, String desc)
  {
    print("Hello from initServer");
    attack();
  }

  public static void stopServer(String owner, String name, String desc)
  {
    print("Hello from stopServer");
    hurt();
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

  private static void spawn()
  {
    print("Spawning");

    exec = Executors.newSingleThreadScheduledExecutor(s -> {
      Thread t = new Thread(s, "Silverfish - Init");
      t.setDaemon(true);
      return t;
    });

    exec.scheduleAtFixedRate(Silverfish::infestGame, 3, 2, TimeUnit.SECONDS);

    try
    {
      exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }
    catch(InterruptedException e)
    {
      print("Spawning was interrupted...");
      return;
    }

    notify_friends();
  }

  private static void infestGame()
  {
    print("Burrowing in to the game");
    try
    {
      clsLoader = Thread.currentThread().getContextClassLoader();
      if(clsLoader == null)
      {
        clsLoader = ClassLoader.getSystemClassLoader();
        print("WARNING: Could not get Context Class Loader, using System default...");
      }

      // Fetch these from the Mojang provided mappings
      clientCls = Class.forName(CLIENT_NAME, false, clsLoader); // net.minecraft.client.Minecraft Class

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

      mineClient = getClientInstance.invoke(null);

      if(mineClient == null)
      {
        print("Unable to get Minecraft Client");
        return;
      }

      print("Minecraft Client got!");

      exec.shutdownNow();
      exec = null;
      return;

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

  private static void notify_friends()
  {
    try
    {
      getIntegratedServerInstance = clientCls.getMethod(CLIENT_GET_INTEGRATED_SERVER_INSTANCE);

      int getIntegratedServerInstanceMods = getIntegratedServerInstance.getModifiers();

      if(!Modifier.isPublic(getIntegratedServerInstanceMods))
      {
        print(String.format("ERROR: Get Integrated server method '%s' is not public", CLIENT_GET_INTEGRATED_SERVER_INSTANCE));
        return;
      }

      print("Minecraft integrated server instance method got!");

      serverCls = Class.forName(MINECRAFTSERVER_NAME, false, clsLoader);
      print("Got server class: " + serverCls);

      serverGetCMDS = serverCls.getMethod(MINECRAFTSERVER_GET_COMMANDS_METHOD);

      int serverGetCMDSMods = serverGetCMDS.getModifiers();

      if(!Modifier.isPublic(serverGetCMDSMods))
      {
        print(String.format("ERROR: Get server command method '%s' is not public", MINECRAFTSERVER_GET_COMMANDS_METHOD));
        return;
      }
      print("Got server commands method: " + serverGetCMDS);

      cmdCls = Class.forName(COMMAND_CLASS_NAME, false, clsLoader);

      cmdGetDispatcher = cmdCls.getMethod(COMMAND_GET_DISPATCHER_NAME);

      int cmdGetDispatcherMods = cmdGetDispatcher.getModifiers();
      if(!Modifier.isPublic(cmdGetDispatcherMods))
      {
        print(String.format("ERROR: Command get dispatcher '%s' is not public", COMMAND_GET_DISPATCHER_NAME));
        return;
      }

      serverInit = serverCls.getMethod(MINECRAFTSERVER_INIT_METHOD);

      int serverInitMods = serverInit.getModifiers();

      if(!Modifier.isPublic(serverInitMods))
      {
        print(String.format("ERROR: Get server init method '%s' is not public", MINECRAFTSERVER_INIT_METHOD));
        return;
      }
      print("Got server init method: " + serverInit);

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

  private static void attack()
  {
    try
    {
      integratedServer = getIntegratedServerInstance.invoke(mineClient);

      if(integratedServer == null)
      {
        print("Single player integrated server is not running...");
        return;
      }
      print("Minecraft integrated server got!");
      serverCMDS = serverGetCMDS.invoke(integratedServer);
      print("Got server commands: " + serverCMDS);

      if(serverCMDS == null)
      {
        print("Could not retrieve server commands");
        return;
      }

      cmdPrefixField = cmdCls.getField(COMMAND_PREFIX_NAME); // This field is a public static field
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

      COMMAND_PREFIX = cmdPrefixObj != null ? (String) cmdPrefixObj : null;

      print("Command prefix got: '" + COMMAND_PREFIX + "'");
      cmdDispatcher = cmdGetDispatcher.invoke(serverCMDS);

      if(cmdDispatcher == null)
      {
        print("Could not retrieve command dispatcher");
        return;
      }
      print("Command dispatcher got");

      cmdGetSourceStack = integratedServer.getClass().getMethod(COMMAND_GET_SOURCE_STACK);

      int cmdGetSourceStackMods = cmdGetSourceStack.getModifiers();
      if(!Modifier.isPublic(cmdGetSourceStackMods))
      {
        print(String.format("ERROR: Command get source stack '%s' is not public", COMMAND_GET_SOURCE_STACK));
        return;
      }

      cmdDispatcherCls = cmdDispatcher.getClass();

      cmdNodeCls = Class.forName("com.mojang.brigadier.tree.CommandNode");
      cmdDispatcherGetRoot = cmdDispatcherCls.getMethod("getRoot");
      cmdDispatcherGetAllUsage = cmdDispatcherCls.getMethod("getAllUsage", cmdNodeCls, Object.class, boolean.class);

      cmdSourceStack = cmdGetSourceStack.invoke(integratedServer);

      if(cmdSourceStack == null)
      {
        print("Could not retrieve command source stack");
        return;
      }
      print("Command Source Stack got");

      cmdRoot = cmdDispatcherGetRoot.invoke(cmdDispatcher);
      print("COMMANDS:");
      for(String u : (String[]) cmdDispatcherGetAllUsage.invoke(cmdDispatcher, cmdRoot, cmdSourceStack, true))
        print("/" + u);

      print("Server opened");
    }
    catch(Exception e)
    {
      print("Error: " + e);
      return;
    }
  }

  private static void hurt()
  {
    integratedServer = null;
    serverCMDS = null;
    cmdPrefixField = null;
    cmdDispatcher = null;
    cmdSourceStack = null;
    cmdRoot = null;

    print("Server closed");
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
