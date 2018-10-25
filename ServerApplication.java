import MyClasses.Dish;
import MyClasses.Server;
import server.ServerInterface;
import server.ServerWindow;

import java.io.File;
import java.util.HashMap;

public class ServerApplication {
    public static void main(String[] args){
        launchGUI(initialise()).setVisible(true);
    }
    public static ServerInterface initialise(){

        return new Server();
    }
    public static ServerWindow launchGUI(ServerInterface server){
        return new ServerWindow(server);
    }
}
