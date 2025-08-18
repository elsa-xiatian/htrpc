import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class zookeeperTest {
    ZooKeeper zookeeper;
    @Before
    public void createZk(){
        String connectString = "127.0.0.1:2181";
        int timeout = 10000;
        try {
            zookeeper = new ZooKeeper(connectString,timeout,null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreatePNode(){
        try {
            String res = zookeeper.create("/xhtrpc", "hello".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("res" + res);
        } catch (KeeperException |InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(zookeeper != null){
                try {
                    zookeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        }

    @Test
    public void testWatcher(){
        try {
            zookeeper.exists("/xht",new MyWatcher());

        } catch (KeeperException |InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(zookeeper != null){
                try {
                    zookeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
