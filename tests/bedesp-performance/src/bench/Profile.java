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
 static void key(int scan,int code){var g=Minecraft.getInstance();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new net.minecraft.client.input.KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new net.minecraft.client.input.KeyEvent(scan,code,0));}
 static void click(int x,int y){var g=Minecraft.getInstance();float s=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*s)/2+x*s,(g.getWindow().getGuiScaledHeight()-410*s)/2+y*s);}
 static void number(String value){click(375,125);for(char c:value.toCharArray())key(30,c);key(40,13);}
 static void ui()throws Exception{
  ClientMenu.toggle();click(40,210);number("1");if(BedEspSettings.range()!=1)throw new AssertionError("range typing minimum");
  number("5001");var f=ClientMenu.class.getDeclaredField("error");f.setAccessible(true);if(BedEspSettings.range()!=1||((String)f.get(null)).isEmpty())throw new AssertionError("invalid range typing");
  number("5000");if(BedEspSettings.range()!=5000)throw new AssertionError("range typing maximum");
  click(170,65);if(!BedEspSettings.enabled())throw new AssertionError("BedEsp menu toggle");click(170,65);if(BedEspSettings.enabled())throw new AssertionError("BedEsp toggle off");
  click(40,90);ClientMenu.scroll(-1);click(170,345);if(!QuickFallSettings.enabled())throw new AssertionError("QuickFall menu toggle");click(170,345);if(QuickFallSettings.enabled())throw new AssertionError("QuickFall toggle off");ClientMenu.toggle();
  System.out.println("BEDESP UI PASS numeric min/max/invalid input, BedEsp switch, scrolled QuickFall switch");
 }
 public static void setup(){
  var g=Minecraft.getInstance();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("BedEsp performance","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.framerateLimit().set(260);g.options.enableVsync().set(false);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setRange(5000);BedEspSettings.setEnabled(false);g.player.connection.sendCommand("tp BridgeLive 0.5 81 -20.5 0 8");}
   g.player.setYRot(Boolean.getBoolean("bench.motion")?(float)(8*Math.sin(warmup*.09)):0);g.player.setXRot(8);
   if(g.gui.screen()!=null||g.gui.overlay()!=null)return;
   warmup++;
   if(warmup==10)ui();
   if(warmup==30){
    int placed=0;
    for(int x=-48;x<48;x++)for(int z=-10;z<46;z+=2){
     if(g.level.getChunkSource().getChunk(x>>4,z>>4,net.minecraft.world.level.chunk.status.ChunkStatus.FULL,false)==null)continue;
     var block=(net.minecraft.world.level.block.BedBlock)net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(net.minecraft.world.item.DyeColor.byId(Math.floorMod(x+z,16)).getName()+"_bed"));
     for(int half=0;half<2;half++)g.level.setBlock(new net.minecraft.core.BlockPos(x,81,z+half),block.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING,net.minecraft.core.Direction.SOUTH).setValue(net.minecraft.world.level.block.BedBlock.PART,half==0?net.minecraft.world.level.block.state.properties.BedPart.FOOT:net.minecraft.world.level.block.state.properties.BedPart.HEAD),2);
     placed++;
    }
    System.out.println("BED STRESS placed logical beds="+placed);
   }
   if(warmup==40){BedEspSettings.setEnabled(true);}
   if(warmup>150)sampling=true;
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
  int beds;
  try{var f=BedEsp.class.getDeclaredField("mesh");f.setAccessible(true);var mesh=f.get(null);var b=mesh.getClass().getDeclaredField("beds");b.setAccessible(true);beds=b.getInt(mesh);}catch(Exception e){throw new RuntimeException(e);}
  if(beds!=1024||states.stream().anyMatch(v->v!=1))throw new AssertionError("bed cap/batching "+beds+" "+states);
  System.out.println("BED STRESS selected="+beds);
  String result=String.format(java.util.Locale.ROOT,"{\"frames\":%d,\"render_ms\":%.4f,\"render_p95_ms\":%.4f,\"render_allocated_bytes\":%.0f,\"gui_states\":%.1f,\"tick_ms\":%.4f,\"tick_p95_ms\":%.4f,\"frame_interval_ms\":%.4f,\"vertex_build_ms\":%.4f}",frames,average(render)/1e6,p95(render)/1e6,average(allocated),states.stream().mapToInt(v->v).average().orElse(0),average(tick)/1e6,p95(tick)/1e6,average(intervals)/1e6,average(vertices)/1e6);
  try{Files.writeString(Path.of(System.getProperty("bench.output","/tmp/bedesp-perf/result.json")),result);}catch(Exception e){throw new RuntimeException(e);}System.out.println("BEDESP BENCHMARK "+result);try{BedEspSettings.setEnabled(false);BedEspSettings.setRange(200);}catch(Exception e){throw new RuntimeException(e);}Minecraft.getInstance().stop();
 }
}
