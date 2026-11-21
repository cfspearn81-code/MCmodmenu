import net.fastersprint.QuickJumpSettings;
public class QuickJumpSettingsTest {
 public static void main(String[] args)throws Exception{
  if(args[0].equals("write")){if(QuickJumpSettings.enabled())throw new AssertionError("default must be off");QuickJumpSettings.setEnabled(true);}
  else if(args[0].equals("reload")){if(!QuickJumpSettings.enabled())throw new AssertionError("enabled not saved");QuickJumpSettings.setEnabled(false);}
  else if(QuickJumpSettings.enabled())throw new AssertionError("disabled not saved");
  System.out.println("PASS QuickJump settings "+args[0]);
 }
}
