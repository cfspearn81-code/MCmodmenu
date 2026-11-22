import net.fastersprint.BridgeConfirmationPolicy;
import net.fastersprint.BridgeFootingTracker;
public final class BridgeConfirmationPolicyTest {
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
    static void maintain(BridgeFootingTracker footing,BridgeConfirmationPolicy policy,int tick){footing.prune(tick,policy.settleTicks(),key->false,key->false,policy::stable);}
    public static void main(String[] args){
        var policy=new BridgeConfirmationPolicy();var footing=new BridgeFootingTracker();
        check(policy.settleTicks()==10,"new server starts conservatively");
        for(int p=0;p<3;p++){footing.predicted(p);footing.serverUpdate(p,true,0);footing.approve(p,10);}
        maintain(footing,policy,29);check(policy.settleTicks()==10,"fresh confirmation cannot earn fast mode");
        maintain(footing,policy,30);check(policy.settleTicks()==1,"three sustained confirmed placements earn fast mode");
        footing.predicted(9);check(footing.blocked(9,1000,1),"fast mode never trusts an ACK-only ghost");
        footing.serverUpdate(9,true,31);check(footing.blocked(9,31,1),"one tick release floor");
        check(!footing.blocked(9,32,1),"confirmed footing releases promptly");footing.approve(9,32);
        check(footing.serverUpdate(9,false,38),"late rollback detected after release");policy.rejected(38);
        check(policy.settleTicks()==10&&footing.blocked(9,1000,1),"rollback quarantines block and resets confidence");
        check(!footing.serverUpdate(9,false,39),"duplicate air feedback does not multiply rejections");
        maintain(footing,policy,60);maintain(footing,policy,80);
        check(policy.settleTicks()==10,"old observations are counted once");
        for(int p=10;p<13;p++){footing.predicted(p);footing.serverUpdate(p,true,60);footing.approve(p,70);}
        maintain(footing,policy,90);check(policy.settleTicks()==10,"recovery ignores early clean observations");
        maintain(footing,policy,150);check(policy.settleTicks()==10,"same observations cannot be replayed after recovery");
        for(int p=20;p<23;p++){footing.predicted(p);footing.serverUpdate(p,true,150);footing.approve(p,160);}
        maintain(footing,policy,180);check(policy.settleTicks()==1,"fresh stable placements restore fast mode after recovery");
        policy.clear();check(policy.settleTicks()==10,"new session forgets confidence");
        System.out.println("PASS: sustained confirmations, fast release, ACK-only quarantine, late rollback, deduplication, recovery and reset");
    }
}
