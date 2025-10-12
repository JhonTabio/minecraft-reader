package com.jhontabio.minecraftreader;

// Debug Terminal imports
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
// Debug Terminal imports

public class Silverfish
{
  public static void premain(String arg)
  {
    displayOutputToConsole();
    System.out.println("Hello before anything else from " + arg);
  }

  public static void agentmain(String arg)
  {
    System.out.println("Hello after main from " + arg);
  }

  // DEBUG USE
  // Windows only solution to display our contents to the terminal
  private static void displayOutputToConsole()
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
