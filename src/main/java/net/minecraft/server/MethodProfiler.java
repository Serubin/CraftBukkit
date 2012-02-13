package net.minecraft.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;

public class MethodProfiler
{
  public static boolean a = true;

  private static List b = new ArrayList();
  private static List c = new ArrayList();
  private static String d = "";
  private static Map<String, Long> e = new HashMap<String, Long>();

  public static void a(String paramString)
  {
    if (!a) return;
    if (d.length() > 0) d += ".";
    d += paramString;
    b.add(d);
    c.add(Long.valueOf(System.nanoTime()));
  }

  public static void a() {
    if (!a) return;
    long l1 = System.nanoTime();
    long l2 = ((Long)c.remove(c.size() - 1)).longValue();
    b.remove(b.size() - 1);
    long l3 = l1 - l2;

    if (e.containsKey(d))
      e.put(d, Long.valueOf(((Long)e.get(d)).longValue() + l3));
    else {
      e.put(d, Long.valueOf(l3));
    }

    d = b.size() > 0 ? (String)b.get(b.size() - 1) : "";
  }

  public static void b(String paramString)
  {
    a();
    a(paramString);
  }
  
  public static void print()
  {
	  print(1);
  }
  public static void print(int ticks)
  {
	  Bukkit.getLogger().info("Profiler:");
	  Bukkit.getLogger().info("" + e.size());
	  String[] keys = e.keySet().toArray(new String[0]);
	  Arrays.sort(keys);
	  for (String key : keys) {
		  Bukkit.getLogger().info(key + ": " + (float)e.get(key)/(ticks*1000*1000) + "ms.");				
	  }
	  e.clear();
  }
  
}