package com.jhontabio.minecraftreader;

public class Silverfish
{
	public static void premain(String arg) throws Exception
	{
		System.err.println("Hello before anything else from " + arg);
	}

	public static void agentmain(String arg)
	{
		System.out.println("Hello after main from " + arg);
	}
}
