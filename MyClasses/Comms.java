package MyClasses;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Comms {
    static String IP="127.0.0.1";
    static int port=1666;
    public Comms(){
    }

    /**
     * This method will be called when the clien
     * @param msg
     * @return
     */
    public static SpecialSocket sendMessage(Message msg)throws ConnectionLost{

           SpecialSocket socket = null;
           try {
               socket = new SpecialSocket();
               socket.writeMessage(msg);
           }catch (Exception exp) {
               throw new ConnectionLost();
           }
       return socket;
    }

    public static Message receiveMessage(SpecialSocket socket)throws ConnectionLost{
        try {
            Message message=socket.getMessage();
            socket.close();
            return message;
        }catch (Exception exp){
            throw new ConnectionLost();
        }
    }
    //Server recieve Message
    public static void receiveMessage(Server server) {
        class ServerThread implements Runnable{
            private Socket socket;
            private ServerSocket serverSoc;
            private SpecialSocket soc;
            @Override
            public void run() {
                try{
                    serverSoc=new ServerSocket(port);
                    while(true) {
                        socket = serverSoc.accept();
                        try {
                            soc = new SpecialSocket(socket);
                        } catch (Exception exe) {
                        }

                        try {
                            Message message = soc.getMessage();
                            if (message.getAttach() == null) {
                                getData(server, soc, message);
                            }else {
                                respond(server,soc,message);
                            }
                        } catch (Exception exe) {
                            System.out.println(exe);
                        }catch (ConnectionLost con){

                        }
                    }
                }catch (Exception ex){

                }
                new Thread(new ServerThread()).start();
            }
        }
        //while(true) {
            new Thread(new ServerThread()).start();
       // }
    }
    //server to client
    public static void sendMessage(SpecialSocket socket, Message msg) throws ConnectionLost{
        try{
            socket.writeMessage(msg);
        }catch (IOException exp){

        }catch (Exception exe){
            throw new ConnectionLost();
        }
    }
    public static void getData(Server serv, SpecialSocket soc, Message message)throws ConnectionLost{
        String title=message.getTitle();
        Message m=null;
        if(title.equals("dishes")){
            m=new Message("return dishes");
            m.setAttach(serv.getDishes());
        }else if (title.equals("postcodes")){
            m=new Message("return postcodes");
            m.setAttach(serv.getPostcodes());
        }

        sendMessage(soc,m);
    }
    public static void respond(Server serv, SpecialSocket soc, Message message) throws ConnectionLost {
        try {
            String title = message.getTitle();
            Message m = null;
            if (title.equals("register")) {
                User user = (User) message.getAttach();
                m = new Message("respond register");
                m.setAttach(serv.addUser(user));
            } else if (title.equals("login")) {
                User user = (User) message.getAttach();
                m = new Message("respond login");
                m.setAttach(serv.checkUser(user.getName(), user.getPassword()));
            } else if (title.equals("orders for")) {
                User user = (User) message.getAttach();
                m = new Message("respond orders");
                m.setAttach(serv.getOrdersFor(user));
            } else if (title.equals("add order")) {
                Order order = (Order) message.getAttach();
                m = new Message("order added");
                serv.addorder(order);
            } else if (title.equals("cancel order")) {
                Order order = (Order) message.getAttach();
                m = new Message("respond cancel Order");
                m.setAttach(serv.removeOrderForCustomer(order));
            }else {
                return;
            }
            sendMessage(soc, m);
        }catch (Exception ex){

        }
        }



}
