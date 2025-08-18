import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        //判断事件类型
        if(event.getType() == Event.EventType.None){
            if(event.getState() == Event.KeeperState.SyncConnected){
                System.out.println("zookeeper 连接成功");
            }
        }
    }
}
