package MyClasses;
import common.Model;

import java.util.Set;

public class Drone extends Model implements Runnable {
    private static final long serialVersionUID = 4L;

    private Configuration businessConfig;
    //speed is in m/s
    private boolean busy;
    private int speed;
    private String status;

    private IngredientStock ingredientStock;
    private Order order;

    public Drone(Number speed, Configuration config) {
        this.setName("Drone");
        this.setStatus("IDLE");
        this.setSpeed((int) speed);
        this.businessConfig = config;
        busy = false;
    }

    public synchronized boolean isBusy() {
        return busy;
    }

    public  synchronized void setBusy(boolean busy) {

            this.busy = busy;
    }

    @Override
    public void run() {
        this.setStatus("IDLE");
        this.setBusy(true);
        if (order != null) {
            try {
                order.setSent(true);
                order.setStatus("Waiting for delivery");
                this.setStatus("checking enough dishes");
                DishStock dishStock=null;
                for(Dish dish:order.getDishes().keySet()){
                    dishStock=getStock().getDishStock(dish);
                    dishStock.offerOrder(order);
                }

                dishStock=null;
                for (Dish dish : order.getDishes().keySet()) {
                    this.setStatus("no enough amount " + dish.getName() + "(Belongs to  " + order.getUser().getName() + ")");
                     dishStock = getStock().getDishStock(dish);;
                    while (!dishStock.enoughDishes((int) order.getDishes().get(dish),order)) ;
                    {
                        //this will alarm the drone that the order has been cancelled
                        if(order.isComplete())
                            throw new CantContinue();

                    }
                    order.setDishReady(dishStock.getDish());
                }
                int distance = (int) order.getUser().getPostcode().getDistance();
                this.setStatus("Sending Order");
                order.setStatus("order in delivery");
                long Time = (long) ((distance / this.getSpeed()) * 1000);
                try {
                    Thread.sleep(Time * 2);
                } catch (InterruptedException ex) {
                }

            order.setComplete(true);
            order.setStatus("order has been delivered");
            }catch (CantContinue EX){

            }catch (NullPointerException NE){
                order.setSent(false);
                order.setComplete(false);
                order.returnReady(businessConfig.getStock());
            }

        }else if (ingredientStock!=null){
                try {
                    ingredientStock.setInProgress(true);
                    this.setStatus("Restocking " + ingredientStock.getIngredient().getName());
                    //amount time
                    double distance = (double) ingredientStock.getIngredient().getSupplier().getDistance();
                    long Time = (long) ((distance / this.getSpeed()) * 1000);
                    try {
                        Thread.sleep(Time * 2);
                    } catch (InterruptedException ex) {
                        ingredientStock.setInProgress(false);
                    }
                    ingredientStock.orderStock();
                }catch (NullPointerException exp){

                }

            //v=m/s     m=vs    s=m/v


        }
            if(ingredientStock!=null)
                  ingredientStock.setInProgress(false);
            order = null;
            ingredientStock = null;
            this.setStatus("IDLE");
            this.setBusy(false);
    }

    @Override
    public  synchronized String getName() {
        return this.name;
    }

    public synchronized int getSpeed() {
        return speed;
    }

    public synchronized void setSpeed(int speed) {
        this.notifyUpdate("Drone Speed",this.getSpeed(),speed);
        this.speed = speed;
    }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        this.notifyUpdate("Drone Supplier",this.getStatus(),status);
        this.status = status;
    }
    public synchronized KitchenStock getStock(){
        return businessConfig.getStock();
    }
    public synchronized Set<Ingredient> getIngredients(){
        return this.businessConfig.getStock().getIngredientStocks();
    }

    public synchronized void sendOrder(Order o){
        order=o;
        System.out.println(this+" is assigned to be sent");
        new Thread(this).start();
    }
    public synchronized void bringIngredient(IngredientStock ing){
        ingredientStock=ing;
        ingredientStock.setInProgress(true);
        System.out.println(ingredientStock.getIngredient().getName()+" commanded to bring(ingredient)");
        new Thread(this).start();
    }
    public synchronized void empty(){
        this.setStatus("IDLE");
        this.setBusy(false);
        this.order=null;
        this.ingredientStock=null;
    }
}