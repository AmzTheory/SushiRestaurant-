import MyClasses.Client;
import client.ClientInterface;
import client.ClientWindow;




public class ClientApplication {
    public static void main(String[] args){
       launchGUI(initialise()).setVisible(true);

    }
    public static ClientInterface initialise(){
        return new Client();
    }
    public static ClientWindow launchGUI(ClientInterface client){
        return new ClientWindow(client);
    }
}
