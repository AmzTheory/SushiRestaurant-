package MyClasses;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SpecialSocket extends Socket {
    private static String IP="127.0.0.1";
    private static int port=1666;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    boolean recieved;
    public SpecialSocket() throws IOException{
        this.setSocket(new Socket(IP,port));
        this.out=new ObjectOutputStream(this.getSocket().getOutputStream());
        this.in=new ObjectInputStream(this.getSocket().getInputStream());


    }
    public SpecialSocket(Socket socket) throws IOException{
        this.setSocket(socket);
        this.in=new ObjectInputStream(this.getSocket().getInputStream());
        this.out=new ObjectOutputStream(this.getSocket().getOutputStream());

    }


    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }


    public Socket getSocket() {
        return socket;
    }

    public Message getMessage()throws IOException,ClassNotFoundException{
        return (Message)in.readObject();
    }
    public void writeMessage(Message message) throws IOException{
        out.writeObject(message);
    }



    public void close(){
        try{
            socket.close();
            in.close();
            out.close();
            in=null;
            out=null;
        }catch (Exception ex){

        }
    }


}
