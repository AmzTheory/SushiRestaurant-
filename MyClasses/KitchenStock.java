package MyClasses;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KitchenStock implements Serializable
{
    private static final long serialVersionUID = 7L;
    private ArrayList<User> users;
    private ConcurrentHashMap<Dish,DishStock> dishStocks;
    private ConcurrentHashMap<Ingredient,IngredientStock> ingredientStocks;
    transient private Object lockA;
    public KitchenStock(){
        users=new ArrayList<>();
        dishStocks=new ConcurrentHashMap<>();
        ingredientStocks=new ConcurrentHashMap<>();
        lockA=new Object();
    }

    public synchronized void addIngedientStock(Ingredient ingredient,int thereshold,int restocking){
        synchronized (lockA) {
            ingredientStocks.put(ingredient, new IngredientStock(ingredient, thereshold, restocking));
        }
    }

    public synchronized void addDishStock(Dish dish,int thereshold,int restocking){
        synchronized (lockA) {
            dishStocks.put(dish, new DishStock(dish, thereshold, restocking));
        }
    }

    public synchronized Set<Dish> getDishStocks() {
        synchronized (lockA) {
            return dishStocks.keySet();
        }
    }
    public synchronized DishStock getDishStock(Dish dish){
        synchronized (lockA) {
            return this.dishStocks.get(dish);
        }
    }
    public synchronized Set<Ingredient> getIngredientStocks() {
        synchronized (lockA) {
            return ingredientStocks.keySet();
        }
    }
    public synchronized IngredientStock getIngredientStock(Ingredient ing){
        synchronized (lockA){
        return ingredientStocks.get(ing);}
    }


    public synchronized boolean enoughtIngredient(DishStock dishStock,int value){
        synchronized (lockA) {
            Set<Ingredient> recipe = dishStock.getDish().getIngredients().keySet();
            //make sure that all of the ingredient exist
            for (Ingredient ing : recipe) {
                // if(this.getIngredientStock(ing).checkRequireRestocking())
                IngredientStock ingStock = ingredientStocks.get(ing);
                int total = value * dishStock.getDish().getAmountFor(ing);
                if ((ingStock.getCurrentQuantity() - total) < 0)
                    return false;
            }
            //take the ingredients for the stock
            for(Ingredient ing: recipe){
                IngredientStock ingStock = ingredientStocks.get(ing);
                int total = value * dishStock.getDish().getAmountFor(ing);
                ingStock.getFromStock(total);
            }
            return true;
        }

    }
    public synchronized boolean removeDish(Dish dish){
        synchronized (lockA) {
            this.dishStocks.remove(dish);
            return true;
        }
    }
    public synchronized boolean removeIngredient(Ingredient ing){
        synchronized (lockA) {
            this.ingredientStocks.remove(ing);
            return true;
        }
    }

    public synchronized Map<Dish,Number> getDishStockLevel(){
        synchronized (lockA) {
            HashMap<Dish, Number> stock = new HashMap<>();
            for (Dish dish : this.dishStocks.keySet()) {
                stock.put(dish, this.getDishStock(dish).getCurrentQuantity());
            }
            return stock;
        }

    }
    //remove from the dishStock queue
    public synchronized void removeOrders(Order order){
        for(DishStock dishStock:dishStocks.values()){
            dishStock.removeOrder(order);
        }
    }
    public synchronized Map<Ingredient,Number> getIngredientStockLevel(){
        synchronized (lockA) {
            HashMap<Ingredient, Number> stock = new HashMap<>();
            for (Ingredient ing : this.ingredientStocks.keySet()) {
                stock.put(ing, this.getIngredientStock(ing).getCurrentQuantity());
            }
            return stock;
        }
    }

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }
    public synchronized void startKitchen(){
        ArrayList<Stock> items=new ArrayList<>();
        items.addAll(this.dishStocks.values());
        items.addAll(this.ingredientStocks.values());

        for(Stock stock:items){

             stock.setInProgress(false);
            if(stock instanceof DishStock){
                ((DishStock)stock).setZeroPreparing();
                ((DishStock)stock).orderQueue=new ConcurrentLinkedQueue<>();
            }
        }
    }

}
