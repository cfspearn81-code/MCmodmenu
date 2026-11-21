import net.fastersprint.*;
import java.nio.file.*;
public class TintSettingsTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  var s=TintSettings.SKY;Path file=Path.of(System.getProperty("test.config")).resolve("faster-sky-tint.properties");
  if(args[0].equals("write")){
   check(!s.enabled(),"default switch");s.preview(.5F,.7F,.9F);check(!Files.exists(file),"preview wrote to disk");s.setEnabled(true);String before=Files.readString(file);s.preview(.8F,1,.4F);check(before.equals(Files.readString(file)),"drag wrote to disk");s.save();
   try{s.preview(Float.NaN,1,1);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
  }else{check(s.enabled(),"switch persisted");check(s.color()==ColorMath.rgb(.8F,1,.4F),"sky color persistence");check(Math.abs(s.vector().y()-((s.color()>>8)&255)/255F)<.00001,"cached sky vector");}
  System.out.println("PASS sky defaults, deferred drag writes and compatible saved color/toggle "+args[0]);
 }
}
