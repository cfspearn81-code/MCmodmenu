import net.fastersprint.BedEspSettings;
import net.fastersprint.QuickFallSettings;
public class BedQuickSettingsTest {
 public static void main(String[] args)throws Exception{
  if(args[0].equals("write")){
   if(BedEspSettings.enabled()||QuickFallSettings.enabled()||BedEspSettings.range()!=200)throw new AssertionError("wrong defaults");
   for(int bad:new int[]{0,-1,5001,Integer.MAX_VALUE})try{BedEspSettings.setRange(bad);throw new AssertionError("accepted "+bad);}catch(IllegalArgumentException expected){}
   BedEspSettings.setRange(1);if(BedEspSettings.range()!=1)throw new AssertionError("minimum");BedEspSettings.setRange(5000);BedEspSettings.setEnabled(true);QuickFallSettings.setEnabled(true);
  }else if(!BedEspSettings.enabled()||!QuickFallSettings.enabled()||BedEspSettings.range()!=5000)throw new AssertionError("persistence");
  System.out.println("PASS BedEsp/QuickFall settings "+args[0]);
 }
}
