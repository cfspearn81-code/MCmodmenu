import net.fastersprint.BridgeFootingTracker;
public final class BridgeFootingTrackerTest {
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
    public static void main(String[] args){
        var tracker=new BridgeFootingTracker();long p=42,q=99;
        check(!tracker.blocked(p,0,10),"existing terrain stays usable");
        check(!tracker.confirmed(p,0,10),"unknown position is not proof for a pending placement");
        tracker.predicted(p);
        check(tracker.blocked(p,10000,10),"time or ACK alone cannot verify a ghost");
        tracker.prune(10000,10,key->false,key->false);
        check(tracker.blocked(p,10000,10),"reconciled solid ghost remains quarantined");
        tracker.serverUpdate(p,true,10000);
        check(tracker.blocked(p,10009,10),"server solid needs stability window");
        tracker.serverUpdate(p,true,10009);
        check(!tracker.blocked(p,10010,10),"duplicate solid update does not prolong settled footing");
        check(tracker.confirmed(p,10010,10),"explicit block confirmation reached stability window");
        tracker.serverUpdate(p,false,10011);
        check(tracker.blocked(p,10020,10),"late rejection revokes trust");
        tracker.serverUpdate(p,true,10020);tracker.predicted(p);
        check(tracker.blocked(p,20000,10),"newer prediction revokes old server proof");
        tracker.serverUpdate(p,true,20000);tracker.prune(20040,10,key->true,key->false);
        tracker.serverUpdate(p,false,20041);
        check(tracker.blocked(p,20042,10),"active prediction preserves quarantine after stable proof");
        tracker.predicted(q);tracker.removeIf(key->key==q);
        check(!tracker.blocked(q,0,10)&&tracker.blocked(p,20042,10),"chunk cleanup leaves other entries intact");
        tracker.prune(20042,10,key->false,key->true);
        check(!tracker.blocked(p,20042,10),"reconciled empty block releases metadata");
        tracker.predicted(p);tracker.clear();check(!tracker.blocked(p,0,10),"world reset clears metadata");
        System.out.println("PASS: authoritative footing, stale/newer predictions, late rejection, cleanup and world reset");
    }
}
