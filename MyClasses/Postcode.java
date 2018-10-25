package MyClasses;
import common.Model;
public class Postcode extends Model{
    private static final long serialVersionUID = 9L;
    private Number distance;
    public Postcode(String name,Number distance){
        this.setName(name);
        this.setDistance((int)distance);
    }
    @Override
    public String getName() {
        return this.name;
    }
    public void setName(String name){
        this.notifyUpdate("name",this.getName(),name);
        this.name=name;
    }

    public synchronized Number getDistance() {
        return distance;
    }

    public synchronized void setDistance(int distance) {
        this.distance = distance;
    }
}
