import net.fastersprint.AimLockSettings;
import com.mojang.blaze3d.platform.InputConstants;
public class AimLockSettingsTest {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  if(args[0].equals("write")){
   check(!AimLockSettings.enabled()&&AimLockSettings.key()==InputConstants.KEY_R,"defaults");
   for(int scan=InputConstants.KEY_A;scan<=InputConstants.KEY_Z;scan++)if(scan!=InputConstants.KEY_P){AimLockSettings.setKey(scan);check(AimLockSettings.key()==scan,"letter binding "+scan);}
   for(int bad:new int[]{-1,0,InputConstants.KEY_P,InputConstants.KEY_ESCAPE,512})try{AimLockSettings.setKey(bad);throw new AssertionError("invalid key accepted");}catch(IllegalArgumentException expected){}
   AimLockSettings.setEnabled(true);AimLockSettings.setKey(InputConstants.KEY_F8);
  }else if(args[0].equals("reload")){check(AimLockSettings.enabled()&&AimLockSettings.key()==InputConstants.KEY_F8,"saved state");AimLockSettings.setEnabled(false);check(AimLockSettings.key()==InputConstants.KEY_F8,"toggle lost key");}
  else check(!AimLockSettings.enabled()&&AimLockSettings.key()==InputConstants.KEY_F8,"saved disabled state");
  System.out.println("PASS Aim Lock letters/reserved keys, independent binding and enabled persistence "+args[0]);
 }
}
