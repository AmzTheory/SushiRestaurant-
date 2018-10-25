package MyClasses;

import common.Model;

import java.util.ArrayList;


public class Staff extends Model implements Runnable{
    private static final long serialVersionUID = 10L;


    KitchenStock stock;
    String Status;
    DishStock dishStock;
    boolean busy;
    public Staff(String name,KitchenStock stock){
        this.stock=stock;
        this.setName(name);
        this.setStatus("Idle");
        this.busy=false;
    }
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void run() {


                dishStock.setInProgress(true);

                while(getStock().getDishStock(dishStock.getDish()).checkEnoughRestocking()) {
                    this.setStatus("Not enough ingredients for  "+dishStock.getDish().getName());
                    while(!getStock().enoughtIngredient(stock.getDishStock(dishStock.getDish()),1)){

                    }
                    this.setStatus("preparing "+dishStock.getDish().getName());
                    try {
                        Thread.sleep(Feature.getRandom(20000,60000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        this.setBusy(false);
                    }
                    getStock().getDishStock(dishStock.getDish()).orderStock();
                }

        this.setBusy(false);
        this.setStatus("idle");
    }


    public void prepareDish(DishStock stock){
        this.dishStock=stock;
        this.busy=true;
        System.out.println(stock.getDish().getName()+" assigned to be prepared (dish)");
        new Thread(this).start();
    }

    public void setStatus(String status){
        this.notifyUpdate("Status",this.getStatus(),status);
        this.Status=status;
    }
    public  String getStatus(){
        return this.Status;
    }
    public  KitchenStock getStock(){
        return this.stock;
    }

    public synchronized boolean isBusy() {
        return busy;
    }

    public  synchronized void setBusy(boolean busy) {
        this.busy = busy;
    }


}
