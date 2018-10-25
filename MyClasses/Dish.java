package MyClasses;
import common.Model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Dish extends Model implements Serializable{
    private static final long serialVersionUID = 2L;

    private String description;
    private int price;
    private HashMap<Ingredient,Number> ingredients;
    transient private Object lockA=new Object();
    transient private Object lockB=new Object();
    public Dish(String name,String description,int price){
        this.setName(name);
        this.setDescription(description);
        this.setPrice(price);
        this.ingredients=new HashMap<>();
    }

    @Override
    public synchronized String getName() {
        synchronized (lockA) {
            return this.name;
        }
    }

    public synchronized String getDescription() {
            return this.description;
    }

    public synchronized void setDescription(String description) {
            this.notifyUpdate("Description", this.description, description);
            this.description = description;
    }

    public synchronized int getPrice() {
        synchronized (lockA) {
            return price;
        }
    }

    public synchronized void setPrice(int price) {
        synchronized (lockA) {
            this.notifyUpdate("price", this.price, price);
            this.price = price;
        }
    }
    public int getAmountFor(Ingredient ing){
        synchronized (lockA) {
            return (int) this.ingredients.get(ing);
        }
    }
    public synchronized void addIngredient(Ingredient ingredient,int quantity){
        synchronized (lockB) {
            this.ingredients.put(ingredient, quantity);
        }
    }
    public synchronized void removeIngredient(Ingredient ing){
        synchronized (lockB) {
            this.ingredients.remove(ing);
        }
    }
    public synchronized void setIngredients(HashMap<Ingredient,Number> reciepe){
        synchronized (lockB) {
            this.ingredients = reciepe;
        }
    }
    public synchronized HashMap<Ingredient,Number> getIngredients(){
        synchronized (lockB) {
            return this.ingredients;
        }
    }

    @Override
    public boolean equals(Object o) {
      Dish dish=(Dish)o;
      if(dish.getName().equals(this.getName()))
          return true;
      else
          return false;
    }

    @Override
    public int hashCode() {
        int hash=0;
        for(int i=0;i<this.getName().length();i++){
            hash+=this.getName().charAt(i);
        }
        return hash;
    }

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
        lockB=new Object();
    }
}
