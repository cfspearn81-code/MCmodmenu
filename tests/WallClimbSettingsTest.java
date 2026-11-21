import net.fastersprint.WallClimbSettings;
public class WallClimbSettingsTest {
 public static void main(String[] args)throws Exception{
  switch(args[0]){
   case "write"->{if(WallClimbSettings.enabled())throw new AssertionError("must start off");WallClimbSettings.setEnabled(true);if(!WallClimbSettings.enabled())throw new AssertionError("toggle on");}
   case "read"->{if(!WallClimbSettings.enabled())throw new AssertionError("enabled did not persist");WallClimbSettings.setEnabled(false);}
   case "off"->{if(WallClimbSettings.enabled())throw new AssertionError("off did not persist");}
   default->throw new AssertionError("unknown test mode");
  }
  System.out.println("PASS Wall Climb settings "+args[0]);
 }
}
