package com.jhontabio.minecraftreader;

// Debug Terminal imports
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
// Debug Terminal imports

public class Silverfish
{
  // DEBUG USE
  private static final PrintStream silverfish_stream = createStream();

  public static void premain(String arg)
  {
    //displayLoggerToConsole();
    print("Hello before anything else from " + arg);
  }

  public static void agentmain(String arg)
  {
    print("Hello after main from " + arg);
  }

  // DEBUG USE
  // Windows only solution to create a new PrintStream using Windows' special device name for the active console's output
  private static PrintStream createStream()
  {
    try{ return new PrintStream(new FileOutputStream("CONOUT$"), true, StandardCharsets.UTF_8); }
    catch(Exception e){ return System.out; }
  }

  private static void print(String str)
  {
    silverfish_stream.println("[Silverfish] " + str);
  }

  // Windows only solution to display our contents to the terminal
  private static void displayLoggerToConsole()
  {
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
