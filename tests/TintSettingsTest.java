import net.fastersprint.*;
import java.nio.file.*;
public class TintSettingsTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  var p=TintSettings.PLAYER;var s=TintSettings.SKY;Path root=Path.of(System.getProperty("test.config"));
  if(args[0].equals("write")){
   check(!p.enabled()&&!s.enabled(),"default switches");p.preview(.3F,.5F,.75F);check(!Files.exists(root.resolve("faster-player-tint.properties")),"preview wrote to disk");p.setEnabled(true);String before=Files.readString(root.resolve("faster-player-tint.properties"));p.preview(.8F,1,.4F);check(before.equals(Files.readString(root.resolve("faster-player-tint.properties"))),"drag wrote to disk");p.save();
   s.preview(.5F,.7F,.9F);s.save();check(!s.enabled(),"color changed toggle");try{p.preview(Float.NaN,1,1);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
  }else{check(p.enabled()&&!s.enabled(),"independent switches persisted");check(p.color()==ColorMath.rgb(.8F,1,.4F),"player color persistence");check(s.color()==ColorMath.rgb(.5F,.7F,.9F),"sky color persistence");check(Math.abs(s.vector().y()-((s.color()>>8)&255)/255F)<.00001,"cached sky vector");}
  System.out.println("PASS tint defaults, deferred drag writes and independent color/toggle persistence "+args[0]);
 }
}
