package bench;
import java.nio.file.*;
import java.util.*;
import java.lang.management.ManagementFactory;
import net.fastersprint.*;
import net.minecraft.client.Minecraft;
public final class Profile {
 public static boolean active, sampling;
 public static long start, allocStart;
 public static int fills, frames, ticks;
 static boolean connecting, ready;
 static int warmup;
 static long lastFrame;
 static final List<Long> render=new ArrayList<>(), tick=new ArrayList<>(), allocated=new ArrayList<>(), intervals=new ArrayList<>();
 static final List<Long> vertices=new ArrayList<>();
 static final List<Integer> states=new ArrayList<>();
 static final com.sun.management.ThreadMXBean bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
 public static long bytes(){return bean.getThreadAllocatedBytes(Thread.currentThread().threadId());}
 public static void setup(){
  var g=Minecraft.getInstance();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Xray performance","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.framerateLimit().set(260);g.options.enableVsync().set(false);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);XraySettings.setRange(200);for(var ore:OreType.values())XraySettings.setOreEnabled(ore,true);XraySettings.setEnabled(false);g.player.connection.sendCommand("tp BridgeLive 0.5 81 0.5 0 0");}
   g.player.setYRot(Boolean.getBoolean("bench.motion")?(float)(30*Math.sin(warmup*.09)):0);g.player.setXRot(0);
   warmup++;
   if(warmup==10)g.player.connection.sendCommand("fill -32 65 8 -1 96 39 coal_ore");
   if(warmup==20)g.player.connection.sendCommand("fill 0 65 8 31 96 39 coal_ore");
   if(warmup==40){XraySettings.setEnabled(true);}
   if(warmup>100)sampling=true;
   if(sampling&&++ticks>500)finish();
  }catch(Exception e){throw new RuntimeException(e);}
 }
 public static void begin(){active=sampling;fills=0;start=System.nanoTime();allocStart=bytes();}
 public static void end(){if(!active)return;long now=System.nanoTime();render.add(now-start);allocated.add(bytes()-allocStart);states.add(fills);active=false;if(lastFrame!=0)intervals.add(now-lastFrame);lastFrame=now;if(++frames==120)finish();}
 public static void vertexTime(long ns){if(sampling)vertices.add(ns);}
 public static void tickTime(long ns){if(sampling)tick.add(ns);}
 static double average(List<Long> values){return values.stream().mapToLong(v->v).average().orElse(0);}
 static double p95(List<Long> values){if(values.isEmpty())return 0;var a=values.stream().mapToLong(v->v).sorted().toArray();return a[Math.min(a.length-1,(int)(a.length*.95))];}
 static boolean done;
 static void finish(){if(done)return;done=true;
  String result=String.format(java.util.Locale.ROOT,"{\"frames\":%d,\"render_ms\":%.4f,\"render_p95_ms\":%.4f,\"render_allocated_bytes\":%.0f,\"gui_states\":%.1f,\"tick_ms\":%.4f,\"tick_p95_ms\":%.4f,\"frame_interval_ms\":%.4f,\"vertex_build_ms\":%.4f}",frames,average(render)/1e6,p95(render)/1e6,average(allocated),states.stream().mapToInt(v->v).average().orElse(0),average(tick)/1e6,p95(tick)/1e6,average(intervals)/1e6,average(vertices)/1e6);
  try{Files.writeString(Path.of(System.getProperty("bench.output","/tmp/xray-perf/result.json")),result);}catch(Exception e){throw new RuntimeException(e);}System.out.println("XRAY BENCHMARK "+result);Minecraft.getInstance().stop();
 }
}
