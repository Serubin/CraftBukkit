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
    if (!a) return;
    a();
    a(paramString);
  }
  
  public static void print()
  {
	  print(1);
  }
  public static void print(int ticks)
  {
      if (!a) return;
      long sum = 0;
	  HashMap<String, Long> newTimes = new HashMap<String, Long>();
	  Bukkit.getLogger().info("Profiler:");
	  Bukkit.getLogger().info("" + e.size());
	  String[] keys = e.keySet().toArray(new String[0]);
	  Arrays.sort(keys);
	  for (String key : keys) {
		Long tmp = e.get(key);
		for (String key2 : keys) {
			if (!key2.equals(key) && key2.startsWith(key)) {
				if(key2.split("\\.").length == (key.split("\\.").length + 1)) {
					tmp = tmp - e.get(key2);
				}
			}
		}
		newTimes.put(key,tmp);
		sum += tmp;
	}
	  for (String key : keys) {
		  long normal = e.get(key);
		  long slim = newTimes.get(key);
		  String slimText = "";
		  if (normal != slim) {
			slimText = "("+ (float)slim/(ticks*1000*1000) + "ms) ";
		  }
		  Bukkit.getLogger().info(key + ": " + (float)normal/(ticks*1000*1000) + "ms. " + slimText + "(" +  (float)100*slim/sum + "%)");
	  }
	  e.clear();
  }
  
}