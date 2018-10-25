package MyClasses;
import common.Model;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;

public class Order extends Model{
    private static final long serialVersionUID = 8L;
    private User user;
    private HashMap<Dish,Number> dishes;
    private HashMap<Dish,Boolean> ready;
    private String status;
    private boolean complete;
    private boolean sent;
    private double totalPrice;
    transient private Object lockA=new Object();

    public Order(HashMap<Dish,Number> map,User user){
        this.setStatus("not ready");
        this.setUser(user);
        String date=LocalDateTime.now().toString();
        this.setName(getUser().getName()+"  "+date);
        this.setDishes(map);
        int price=0;
        for(Dish instance:map.keySet()){
            price+=(instance.getPrice())*(int)map.get(instance);
        }
        this.setTotalPrice(price);
        this.setSent(false);
        this.ready=new HashMap<>();
    }
    public Order(User user){
        this.setStatus("not ready");
        this.setUser(user);
        String date=LocalDateTime.now().toString();
        this.setName(getUser().getName()+"  "+date);
        this.setComplete(false);
        this.totalPrice=0.0;
        this.dishes=new HashMap<>();
        this.ready=new HashMap<>();
    }

    @Override
    public synchronized String getName() {
        return this.name;
    }

    public synchronized User getUser() {
        return user;
    }

    public synchronized void setUser(User user) {
        this.user = user;
    }

    public synchronized HashMap<Dish, Number> getDishes() {
        synchronized (lockA) {
            return dishes;
        }
    }

    public synchronized void setDishes(HashMap<Dish, Number> dishes) {
        synchronized (lockA) {
            this.dishes = dishes;
        }
    }

    public synchronized String getStatus() {
        return status;
    }
    public synchronized void addDish(Dish dish,int quantity){
        synchronized (lockA) {
            if (this.dishes.keySet().contains(dish)) {
                this.changeQunatity(dish, quantity);
            } else {
                this.setTotalPrice(this.getTotalPrice() + (dish.getPrice() * quantity));
                this.getDishes().put(dish, quantity);
                this.ready.put(dish, false);
            }
        }
    }
    public synchronized void removeDish(Dish dish){
        synchronized (lockA) {
            try {
                double removedAmount = (dish.getPrice() * (int) this.getDishes().get(dish));
                this.notifyUpdate("Basket Price", this.getTotalPrice(), this.getTotalPrice() - removedAmount);
                this.setTotalPrice(this.getTotalPrice() - removedAmount);
                this.getDishes().remove(dish);

            } catch (NullPointerException exe) {
                JOptionPane.showMessageDialog(null, "The dish no longer exist");
            }
        }
    }
    public synchronized void changeQunatity(Dish dish,int newQunatity){
        synchronized (lockA) {
            try {
                this.setTotalPrice(this.getTotalPrice() - (dish.getPrice() * (int) getDishes().get(dish)));
                this.setTotalPrice(this.getTotalPrice() + (dish.getPrice() * newQunatity));
                this.getDishes().replace(dish, newQunatity);
            }catch (NullPointerException exe){
                JOptionPane.showMessageDialog(null,"The dish no longer exist");
            }
        }
    }
    public synchronized void clearOrder(){
            synchronized (lockA) {
                this.setTotalPrice(0.0);
                this.dishes = new HashMap<>();
            }
    }
    public synchronized void setStatus(String status) {
        this.status = status;
    }

    public synchronized double getTotalPrice() {
        synchronized (lockA) {
            return totalPrice;
        }
    }

    public synchronized void setTotalPrice(double totalPrice) {
        synchronized (lockA) {
            this.totalPrice = totalPrice;
        }
    }

    public synchronized boolean isSent() {
        synchronized (lockA) {
            return sent;
        }
    }

    public synchronized void setSent(boolean sent) {
        synchronized (lockA) {
            this.sent = sent;
        }
    }

    public synchronized boolean isComplete() {
        synchronized (lockA) {
            return complete;
        }
    }

    public synchronized void setComplete(boolean complete) {
        synchronized (lockA) {
            this.complete = complete;
        }
    }
    public synchronized ArrayList<Dish> getDishReady(){
        synchronized(lockA) {
            ArrayList<Dish> dishes = new ArrayList<>();
            for (Dish dish : this.ready.keySet()) {
                if (ready.get(dish)) {
                    dishes.add(dish);
                }
            }

            return dishes;
        }
    }
    public synchronized void setDishReady(Dish dish){
        synchronized (lockA) {
            this.ready.put(dish, true);
        }
    }
    public synchronized void setDishNoteady(Dish dish){
        synchronized (lockA) {
            this.ready.put(dish, false);
        }
    }
    public synchronized void returnReady(KitchenStock stock){
        synchronized (lockA) {
            for (Dish dish : this.getDishReady()) {
                DishStock dishStock = stock.getDishStock(dish);
                int quantity = (int) this.getDishes().get(dish);
                dishStock.setCurrentQuantity(dishStock.getCurrentQuantity() + quantity);
                this.setDishNoteady(dish);
            }
        }
    }



    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }
    @Override
    public boolean equals(Object o) {
        Order order=(Order) o;
        if(order.getName().equals(this.getName()))
            return true;
        else
            return false;
    }
}
