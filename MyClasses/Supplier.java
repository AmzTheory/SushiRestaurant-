package MyClasses;
import common.Model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Supplier extends Model implements Serializable {
    private static final long serialVersionUID = 11L;
    private Number distance;
    transient Object lockA=new Object();

    public Supplier(String name,Number distance){
        this.setDistance((int)distance);
        this.setName(name);
    }
    @Override
    public String getName() {
        return this.name;
    }

    public synchronized Number getDistance() {
        synchronized (lockA) {
            return distance;
        }
    }

    public synchronized void setDistance(double distance) {
        synchronized (lockA) {
            this.notifyUpdate("distance", this.distance, distance);
            this.distance = distance;
        }
    }
    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }



}
