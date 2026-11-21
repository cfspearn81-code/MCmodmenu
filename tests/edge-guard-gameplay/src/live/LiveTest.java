package live;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.fastersprint.PlayerDisplaySettings;
import net.fastersprint.AutoBridge;
public final class LiveTest {
 public static boolean running, releasingAck;
 public static String scenario="idle";
 static boolean connecting; static String last=""; static int ticks,limit,phase,suite=-1,wait,packets,lastPacket=-999,rateErrors;
 static double x,z,minY,maxY; static int initial; static long frame;
 static final List<String> RESULTS=new ArrayList<>();
 static final String[] SCENARIOS={"straight","diagonal","angle22","angle67","angle135","angle225","zigzag","jump","sprint","delay","reject","empty","place","place-delay","place-jump"};
 static final Deque<String> commands=new ArrayDeque<>();
 record Ack(net.minecraft.client.multiplayer.ClientLevel level,int sequence,long due){}
 static final List<Ack> acks=new ArrayList<>();
 public static boolean delay(net.minecraft.client.multiplayer.ClientLevel level,int sequence){
  if(releasingAck || !running || !scenario.contains("delay"))return false;
  acks.add(new Ack(level,sequence,frame+10));return true;
 }
 public static void interaction(){if(!running)return;packets++;int now=Minecraft.getInstance().player.tickCount;if(now-lastPacket<4)rateErrors++;lastPacket=now;}
 static void clearKeys(Minecraft g){g.options.keyUp.setDown(false);g.options.keyDown.setDown(false);g.options.keyLeft.setDown(false);g.options.keyRight.setDown(false);g.options.keyJump.setDown(false);g.options.keySprint.setDown(false);}
 static void prepare(Minecraft g){
  running=false;clearKeys(g);scenario=SCENARIOS[suite];
  commands.clear();
  for(int y=80;y<96;y+=4)commands.add("fill -32 "+y+" -32 32 "+(y+3)+" 32 air");
  commands.add("fill -2 80 -2 2 80 2 stone");commands.add("tp BridgeLive 0.5 81 0.5 0 0");
  commands.add("clear BridgeLive");if(!scenario.equals("empty"))commands.add("give BridgeLive stone 64");
  commands.add("gamemode "+(scenario.equals("reject")?"adventure":"survival")+" BridgeLive");
  phase=1;wait=25;System.out.println("LIVE PREPARE "+scenario);
 }
 static void begin(Minecraft g){
  ticks=packets=rateErrors=0;lastPacket=-999;running=true;phase=2;
  x=g.player.getX();z=g.player.getZ();minY=maxY=g.player.getY();initial=g.player.getMainHandItem().getCount();
  System.out.println("LIVE START "+scenario+" at "+g.player.position());
 }
 public static void tick(){
  Minecraft g=Minecraft.getInstance();frame++;
  if(g.player==null || g.level==null){
   if(!connecting && g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Bridge test","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;
  }
  try{
   if(!g.player.isAlive()){g.player.respawn();return;}
   g.options.pauseOnLostFocus=false;
   if(!PlayerDisplaySettings.autoBridgeEnabled())PlayerDisplaySettings.setAutoBridgeEnabled(true);
   for(var it=acks.iterator();it.hasNext();){var a=it.next();if(a.due<=frame){releasingAck=true;a.level.handleBlockChangedAck(a.sequence);releasingAck=false;it.remove();}}
   String cmd=Files.exists(Path.of("/tmp/bridge-gameplay/command"))?Files.readString(Path.of("/tmp/bridge-gameplay/command")).trim():"idle";
   if(!cmd.equals(last)){last=cmd;running=false;phase=0;clearKeys(g);
    if(cmd.startsWith("suite")){limit=120;suite=0;RESULTS.clear();prepare(g);}
    else if(!cmd.equals("idle")){String[] p=cmd.split(":");scenario=p[0];limit=p.length>1?Integer.parseInt(p[1]):120;suite=-1;begin(g);}
   }
   if(phase==1){clearKeys(g);if(!commands.isEmpty()){if(frame%3==0)g.player.connection.sendCommand(commands.removeFirst());return;}if(wait-->0)return;begin(g);}
   g.options.keyUp.setDown(running&&!scenario.startsWith("place"));g.options.keyDown.setDown(running&&scenario.startsWith("place"));
   g.options.keyLeft.setDown(running&&(scenario.equals("diagonal")||(scenario.contains("zigzag")&&(ticks/15)%2==0)));
   g.options.keyRight.setDown(running&&scenario.contains("zigzag")&&(ticks/15)%2==1);
   g.options.keyJump.setDown(running&&scenario.contains("jump"));g.options.keySprint.setDown(running&&scenario.contains("sprint"));
   g.player.setYRot(scenario.startsWith("place")?180:scenario.startsWith("angle")?Float.parseFloat(scenario.substring(5)):0);
   float pitch=0;
   if(scenario.startsWith("place")) {
    pitch=88;
    var under=net.minecraft.core.BlockPos.containing(g.player.getX(),Math.floor(g.player.getY())-1,g.player.getZ());
    if(g.level.getBlockState(under).canBeReplaced()) {
     var support=under.north();
     if(!g.level.getBlockState(support).canBeReplaced()) {
      double distance=g.player.getZ()-(support.getZ()+1.0);
      if(distance>0.001)pitch=(float)Math.toDegrees(Math.atan2(g.player.getEyePosition().y-(support.getY()+.5),distance));
     }
    }
   }
   g.player.setXRot(pitch);
   if(running && scenario.equals("reject"))g.gameMode.setLocalMode(net.minecraft.world.level.GameType.SURVIVAL);
   if(!running)return;
   ticks++;minY=Math.min(minY,g.player.getY());maxY=Math.max(maxY,g.player.getY());
   if(ticks%20==0)System.out.println("LIVE TICK "+scenario+" "+ticks+" pos="+g.player.position()+" status="+AutoBridge.status());
   if(ticks>=limit||g.player.getY()<80.5){running=false;clearKeys(g);
    String result=String.format(Locale.ROOT,"{\"scenario\":\"%s\",\"ticks\":%d,\"dx\":%.4f,\"dz\":%.4f,\"minY\":%.4f,\"maxY\":%.4f,\"used\":%d,\"attempts\":%d,\"rateErrors\":%d,\"status\":\"%s\"}",scenario,ticks,g.player.getX()-x,g.player.getZ()-z,minY,maxY,initial-g.player.getMainHandItem().getCount(),packets,rateErrors,AutoBridge.status());
    RESULTS.add(result);Files.writeString(Path.of("/tmp/bridge-gameplay/result.json"),result);Files.writeString(Path.of("/tmp/bridge-gameplay/suite.json"),"["+String.join(",",RESULTS)+"]");System.out.println("LIVE RESULT "+result);
    if(suite>=0&&++suite<SCENARIOS.length)prepare(g);else{phase=0;System.out.println("LIVE COMPLETE");}
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
