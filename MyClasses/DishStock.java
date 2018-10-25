package MyClasses;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DishStock extends Stock implements Serializable {
    private static final long serialVersionUID = 3L;


    private Dish dish;
    private int howManyPreparing;
    transient private Object lockA=new Object();
    ConcurrentLinkedQueue<Order> orderQueue;
    public DishStock(Dish dish,int thereshold,int restocking){
        super(thereshold,restocking);
        this.setDish(dish);
        howManyPreparing =0;
        orderQueue=new ConcurrentLinkedQueue<>();
    }
    @Override
    public synchronized void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity=currentQuantity;
    }
    public synchronized Dish getDish() {
        synchronized (lockA) {
            return dish;
        }
    }

    public synchronized void setDish(Dish dish) {
        synchronized (lockA) {
            this.dish = dish;
        }
    }

    @Override
    public synchronized void orderStock(){
        synchronized (lockA) {
            this.setCurrentQuantity(this.getCurrentQuantity() + 1);
        }
    }

    public synchronized boolean checkRequireOrder() {
        synchronized (lockA) {
            if (this.getCurrentQuantity() <= this.getThereshold()) {
                return true;
            }else
                return false;
        }
    }
    public synchronized boolean enoughDishes(int value,Order order){
        synchronized (lockA) {
            if (this.getCurrentQuantity() >= value) {
                //check that this order is in the top of queue
                if(!this.isOrderTop(order)){
                    return false;
                }
                //remove the order from the queue
                this.pollOrder();
                getFromStock(value);
                return true;
            }

            return false;
        }
    }

    public synchronized boolean checkEnoughRestocking() {
        synchronized (lockA) {
            howManyPreparing -=1;

            //in case the restocking amount is equal to zero
            if(this.getRestockingAmount()==0 && this.getCurrentQuantity()+ howManyPreparing >=this.getThereshold()) {
                if(howManyPreparing==0)
                    this.setInProgress(false);
                return false;
            }//otherwise do the normal check
            else if (this.getCurrentQuantity()+ howManyPreparing >=this.getRestockingAmount()){
                if(howManyPreparing==0)
                    this.setInProgress(false);
                return false;
            }
            this.addStaffPrepare();
            return true;
        }
    }


    public synchronized void addStaffPrepare(){
        synchronized (lockA){
            howManyPreparing+=1;
        }
    }

    public synchronized void setZeroPreparing(){
        synchronized (lockA){
            howManyPreparing=0;
        }
    }
    public synchronized int getCurrentQuantity() {
        return currentQuantity;
    }




    public  synchronized int getThereshold() {
        return thereshold;
    }

    public  synchronized void setThereshold(int thereshold) {
        this.thereshold = thereshold;
    }

    public synchronized int getRestockingAmount() {
        return restockingAmount;
    }

    public synchronized void setRestockingAmount(int restockingAmount) {
        this.restockingAmount = restockingAmount;
    }

    public synchronized boolean isInProgress() {
        return inProgress;
    }

    public synchronized void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
    public synchronized void getFromStock(int value){
        this.setCurrentQuantity(this.getCurrentQuantity()-value);
    }
    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }

    public synchronized boolean isOrderTop(Order order){
        if(orderQueue.size()==0)
            return false;
        else if(orderQueue.peek()==order){
            return true;
        }
        return false;
    }
    public synchronized Order pollOrder(){
        return orderQueue.poll();
    }
    public synchronized void offerOrder(Order order){
        this.orderQueue.offer(order);
    }
    public synchronized void removeOrder(Order order){
        this.orderQueue.remove(order);
    }
}
