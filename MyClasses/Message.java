package MyClasses;


import java.io.Serializable;

public class Message implements Serializable{


    private String title;
    private Object attach;
    public Message(String title){
        this.setTitle(title);
    }

    public synchronized String getTitle() {
        return title;
    }

    public synchronized void setTitle(String title) {
        this.title = title;
    }

    public synchronized Object getAttach() {
        return attach;
    }
    public synchronized void setAttach(Object attach) {
        this.attach = attach;
    }
}
