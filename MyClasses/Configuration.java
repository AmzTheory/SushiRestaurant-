package MyClasses;

import server.ServerInterface;
import server.ServerWindow;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Configuration implements Serializable{

    private static final long serialVersionUID = 1L;

    private HashMap<String,User> users;
    private Vector<Supplier> suppliers;
    private Vector<Drone> drones;
    private Vector<Staff> staff;
    private KitchenStock stock;
    private HashMap<String,Postcode> postcodes;
    private Vector<Order> orders=new Vector<>();


     private ConcurrentLinkedQueue<Staff> staffQueue;
     private ConcurrentLinkedQueue<Drone> droneQueue;

    transient Object lockA;
    public Configuration(String file){

            stock = new KitchenStock();
            users = new HashMap<>();
            suppliers = new Vector<>();
            drones = new Vector<>();
            staff = new Vector<>();
            postcodes = new HashMap<>();
            staffQueue=new ConcurrentLinkedQueue<>();
            droneQueue=new ConcurrentLinkedQueue<>();
            lockA=new Object();
           // if(!file.equals(""))
            if(!file.equals(""))
                this.readFile(file);
    }
    public void readFile(String file){
        try {
            int countSpace=0;
            boolean cont=true;
            BufferedReader reader = new BufferedReader(new FileReader(file));

            while(cont){
                String line=reader.readLine();
                if (line==null)
                    break;

                if(line!="") {
                    String[] data = line.split(":");
                    this.addData(data);
                }
            }

            reader.close();
            JOptionPane.showMessageDialog(null,"Configuration have been imported successfully");
        }catch (Exception EX){
            System.out.println(EX.getMessage());
            EX.printStackTrace();
        }
    }
    public void addData(String[] data){
        if (data[0].equals("SUPPLIER")){
            this.suppliers.add(new Supplier(data[1],Integer.parseInt(data[2])));
        }
        else if(data[0].equals("INGREDIENT")){
            Ingredient ing=new Ingredient(data[1],data[2],getSupplier(data[3]));
            this.stock.addIngedientStock(ing,Integer.parseInt(data[4]),Integer.parseInt(data[5]));
        }else if(data[0].equals("DISH")){
            Dish dish=new Dish(data[1],data[2],Integer.parseInt(data[3]));
            stock.addDishStock(dish,Integer.parseInt(data[4]),Integer.parseInt(data[5]));

            String[] recipe=data[6].split(",");
            for(int i=0;i<recipe.length;i++){
                String[] info=recipe[i].split("\\*");
                Ingredient ing=stock.getIngredientStock(new Ingredient(info[1].trim(),"dd",null)).getIngredient();
                dish.addIngredient(ing,Integer.parseInt(info[0].trim()));
            }
        }else if(data[0].equals("POSTCODE")){
            this.postcodes.put(data[1],new Postcode(data[1],Integer.parseInt(data[2])));
        }else if(data[0].equals("STOCK")){
            DishStock dishStock=  stock.getDishStock(new Dish(data[1],"",0));
            if(dishStock!=null){
                dishStock.setCurrentQuantity(Integer.parseInt(data[2]));
            }else if(stock.getIngredientStock(new Ingredient(data[1]," ",null))!=null){
                stock.getIngredientStock(new Ingredient(data[1]," ", null)).setCurrentQuantity(Integer.parseInt(data[2]));
            }
        }else if(data[0].equals("USER")){
            users.put(data[1],new User(data[1],data[2],data[3],postcodes.get(data[4])));
        }else if( data[0].equals("ORDER")){
            HashMap<Dish,Number> map=new HashMap<>();
            String[] details=data[2].split(",");
            for(String s : details){
                String[] info=s.split(" \\* ");
                Dish dish=stock.getDishStock(new Dish(info[1],"",0)).getDish();
                map.put(dish,Integer.parseInt(info[0]));
            }
            Order order=new Order(map,this.users.get(data[1]));
            this.orders.add(order);
        }else if (data[0].equals("DRONE")){
            this.addDrone((Integer.parseInt(data[1])));
        }else if (data[0].equals("STAFF")){
            this.addStaff(new Staff(data[1],stock));
        }
    }


    public synchronized Supplier getSupplier(String name){
        for(Supplier supplier :this.suppliers){
            if(supplier.getName().equals(name));{
                return supplier;
            }
        }
        return null;
    }
    public synchronized void setDishStock(Dish dish,Number value){
        stock.getDishStock(dish).setCurrentQuantity((int)value);
        this.saveData();
    }
    public synchronized void setIngredientStock(Ingredient ing,Number value){
        stock.getIngredientStock(ing).setCurrentQuantity((int)value);
        this.saveData();
    }

    public synchronized ArrayList<User> getUsers() {
        ArrayList<User> users=new ArrayList<>(this.users.values());
        return users;
    }

    public synchronized ArrayList<Supplier> getSuppliers() {
        return new ArrayList<>(suppliers);
    }

    public  synchronized ArrayList<Drone> getDrones() {
        return new ArrayList<>(this.drones);
    }

    public synchronized ArrayList<Staff> getStaff() {
        return new ArrayList<>(this.staff);
    }

    public synchronized KitchenStock getStock() {
        return stock;
    }
    public synchronized ArrayList<Dish> getDishes(){
        return new ArrayList<>(this.stock.getDishStocks());
    }
    public synchronized ArrayList<Ingredient> getIngredients(){
        synchronized (lockA) {
            return new ArrayList<>(this.stock.getIngredientStocks());
        }
    }
    public synchronized ArrayList<Postcode> getPostcodes() {
        return new ArrayList<Postcode>(this.postcodes.values());
    }

    public synchronized ArrayList<Order> getOrdersFor(User user) {
        ArrayList<Order> orders=new ArrayList<>();
        for(Order order : this.orders){
            if(order.getUser().equals(user))
                orders.add(order);
        }
        return orders;
    }
    public synchronized Order getOrder(Order passed){
        synchronized (lockA){
            for(Order o:orders){
                if(o.equals(passed)){
                    return o;
                }
            }

            return null;
        }
    }
    public  ArrayList<Order> getNonCompleteOrders(){
        ArrayList<Order> orders=new ArrayList<>();
        for(Order order : this.orders){
            if(!order.isComplete() && order.isSent())
                orders.add(order);
        }
        return orders;
    }

    public ArrayList<Order> getOrders() {
        return new ArrayList<>(this.orders);
    }
    public synchronized void addDish(Dish dish,Number restockingAmount,Number restockingThershold){
        synchronized (lockA) {
            this.stock.addDishStock(dish, (int) restockingThershold, (int) restockingAmount);
        }
    }
    public synchronized void removeDish(Dish dish) throws ServerInterface.UnableToDeleteException {
        synchronized (lockA) {
            if (!this.getStock().getDishStocks().contains(dish)) {
                throw new ServerInterface.UnableToDeleteException("this dish doesn't actually exist in the stock");
            }

            //Dish can't be deleted if
            //1-if there is order which is not complete that was include the dish
            for(Order o:this.getOrders()){
                if(!o.isComplete())
                    throw new ServerInterface.UnableToDeleteException("a dish can't be deleted if there exist order that is not complete that include the dish");
            }
            this.getStock().removeDish(dish);
            this.saveData();
        }
    }

    public void addIngredient(Ingredient ing,Number restockingAmount,Number restockingThershold){
        this.stock.addIngedientStock(ing,(int)restockingThershold,(int)restockingAmount);
        this.saveData();
    }
    public synchronized void removeIngredient(Ingredient ing) throws ServerInterface.UnableToDeleteException {
        synchronized (lockA) {
            if (!this.getStock().getIngredientStocks().contains(ing))
                throw new ServerInterface.UnableToDeleteException("this Ingredient doesn't actually exist in the stock");

                //ingredient won't be able to be deleted if there exist Dish that uses the ingredient as part of the recipe

                for(Dish dish : this.getStock().getDishStocks()) {
                    if (dish.getIngredients().containsKey(ing)) {
                        JOptionPane.showMessageDialog(null,"make sure that there is no dish that uses this ingredient ");
                        throw new ServerInterface.UnableToDeleteException("ingredient can't be deleted if it's used by one of dish in the stock");
                    }
                }
                this.getStock().removeIngredient(ing);
                this.saveData();
            }
        }
    public void setRestockLevel(Dish dish, Number restockThreshold, Number restockAmount){
        DishStock dishStock=this.stock.getDishStock(dish);
        dishStock.setRestockingAmount((int)restockAmount);
        dishStock.setThereshold((int)restockThreshold);
        this.saveData();
    }
    public void setRestockLevel(Ingredient ingredient, Number restockThreshold, Number restockAmount){
        IngredientStock ingredientStock=this.stock.getIngredientStock(ingredient);
        ingredientStock.setRestockingAmount((int)restockAmount);
        ingredientStock.setThereshold((int)restockThreshold);
        this.saveData();
    }
    public DishStock getRestockLevel(Dish dish){
        return this.stock.getDishStock(dish);
    }
    public IngredientStock getRestockLevel(Ingredient ing){
        return this.stock.getIngredientStock(ing);
    }
    public Map<Dish,Number> getDishesStockLevel(){
        return this.stock.getDishStockLevel();
    }
    public Map<Ingredient,Number> getIngredientLevel(){
        return this.stock.getIngredientStockLevel();
    }

    public void addSupplier(Supplier sup){
        this.suppliers.add(sup);
        this.saveData();
    }
    public synchronized void addIngredientToDish(Dish dish,Ingredient ing,Number quantity){
        synchronized (lockA){
            IngredientStock ingStock=getStock().getIngredientStock(ing);
            if(ingStock.getRestockingAmount()>=(int)quantity)
                dish.addIngredient(ing,(int)quantity);
        }
    }
    public void removeSupplier(Supplier supplier)throws ServerInterface.UnableToDeleteException {
        if(!this.suppliers.contains(supplier))
            throw new ServerInterface.UnableToDeleteException("The Supplier doesn't exist");

        //supplier can't be deleted if there exist ingredient that is associated with it
        for(Ingredient ing:getStock().getIngredientStocks()){
            if(ing.getSupplier()==supplier) {
                JOptionPane.showMessageDialog(null,"There is ingredient ( "+ing.getName()+" ) that this supplier provide");
                throw new ServerInterface.UnableToDeleteException("The supplier can't be deleted if there exist is associated with it");
            }

        }
        this.suppliers.remove(supplier);
        this.saveData();
    }

    public Drone addDrone(Number Speed){
        Drone drone=new Drone(Speed,this);
        this.drones.add(drone);
        this.droneQueue.offer(drone);
        //new Thread(drone).start();
        this.saveData();
        return drone;
    }
    public void removeDrone(Drone drone)throws ServerInterface.UnableToDeleteException {
        if(!this.drones.contains(drone))
            throw new ServerInterface.UnableToDeleteException("The Drone doesn't exist");
        else if(drone.isBusy()){
            JOptionPane.showMessageDialog(null,"Drone can't be delete wile it'ss busy");
            throw new ServerInterface.UnableToDeleteException("the Drone is currently busy");
        }else if(getDrones().size()==2){
            //two avoid having situation where drone are busy with orders that require ingredient restocking
            JOptionPane.showMessageDialog(null," The Minimum number of drones  is two");
            throw new ServerInterface.UnableToDeleteException("The min number of drone to be in the system is two");
        }

        this.drones.remove(drone);
        this.droneQueue.remove(drone);
        this.saveData();
    }
    public void addStaff(Staff staff){
        this.staff.add(staff);
        this.staffQueue.offer(staff);
        //new Thread(staff).start();
        this.saveData();
    }
    public void removeStaff(Staff staff)throws ServerInterface.UnableToDeleteException {
        if(!this.staff.contains(staff))
            throw new ServerInterface.UnableToDeleteException("The Staff doesn't exist(can't be deleted while working)");
        else if(staff.isBusy()){
            JOptionPane.showMessageDialog(null,"The staff can't be delete while it's working");
            throw new ServerInterface.UnableToDeleteException("The Staff is currently bust (can't be deleted while working)");
        }


        this.staff.remove(staff);
        this.staffQueue.remove(staff);

        this.saveData();
    }


    public void removeOrder(Order order) throws ServerInterface.UnableToDeleteException{
        synchronized(lockA) {
            if (!this.orders.contains(order))
                throw new ServerInterface.UnableToDeleteException("1");

            if (order.getStatus().equals("order in delivery"))
                throw new ServerInterface.UnableToDeleteException("2");
            else if(order.getStatus().equals(order.getStatus().equals("Waiting for delivery"))){
                order.setComplete(true);
                order.returnReady(getStock());
            }
            getStock().removeOrders(order);
            this.orders.remove(order);
            this.saveData();
        }

    }


    public synchronized void addPostcode(Postcode postcode){
        synchronized (lockA) {
            this.postcodes.put(postcode.getName(), postcode);
            this.saveData();
        }
    }
    public void removePostCode(Postcode postcode)throws ServerInterface.UnableToDeleteException {
        synchronized (lockA) {
            if (!this.postcodes.keySet().contains(postcode.getName()))
                throw new ServerInterface.UnableToDeleteException("The postcode doesn't exist");
            //Postcode can't be Deleted if it's used by one of the users
            for (User user : this.getUsers()) {
                if (user.equals(postcode)) {
                    JOptionPane.showMessageDialog(null, "Postcode can't be deleted if it's used by any of the users");
                    throw new ServerInterface.UnableToDeleteException("Postcode can't be deleted if it's used by one of the users");
                }
            }
            this.postcodes.remove(postcode.getName());
        }

        this.saveData();
    }

    public void removeUser(User user)throws ServerInterface.UnableToDeleteException {
        if(!this.users.keySet().contains(user.getName()))
            throw new ServerInterface.UnableToDeleteException("The User doesn't exist");

        //for user to be removed, all of the orders belong to this specific user have to be completed
        for(Order order:this.getOrdersFor(user)){
            if(!order.isComplete()){
                JOptionPane.showMessageDialog(null,"user can't be deleted if the user have orders that are not complete yet");
                throw new ServerInterface.UnableToDeleteException("can't delete user if there exist order that belong to the user which is not yet complete");
            }

        }
        this.users.remove(user.getName());
        this.saveData();
    }

    public boolean addUser(User U){
        if(this.users.containsKey(U.getName())){
            return false;
        }
        this.users.put(U.getName(),U);
        this.saveData();
        return true;
    }
    public User checkUser(String userName,String password){
        User U=this.users.get(userName);
        //User doesn't exist
        if(U==null)
            return null;
        //correct  userName and password
        if(U.getName().equals(userName) && U.getPassword().equals(password))
            return U;

        //correct userName but wrong password
        return null;
    }
    public void addOrder(Order o){
        this.orders.add(o);
        this.saveData();
    }

    public void saveData(){
        DataPersistence.writeBusinessApp(this,1);
        DataPersistence.writeBusinessApp(this,2);
        DataPersistence.writeBusinessApp(this,3);
    }
    public synchronized Staff getFirstStaff(){

        Staff first=null;
        synchronized (lockA) {
            for (int i = 0; i < staffQueue.size(); i++) {
                first = staffQueue.poll();

                if (!first.isBusy()) {
                    first.setBusy(true);
                    //this.saveData();
                    return first;
                }
                offerLastStaff(first);
            }
        }
        //this.saveData();
        return null;
    }
    public synchronized void offerLastStaff(Staff last){
        this.staffQueue.offer(last);
        //this.saveData();
    }

    public synchronized  Drone getFirstDrone(boolean order){
        synchronized (lockA){
            Drone first=null;
            for(int i=0;i<droneQueue.size();i++) {
                first = droneQueue.poll();
                if (!first.isBusy()) {
                    first.setBusy(true);
                    return first;
                }
                offerLastDrone(first);
            }
        }
        return null;
    }


    public  synchronized void offerLastDrone(Drone last){
        synchronized (lockA) {
            this.droneQueue.offer(last);
        }
    }

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }
    //set all of the drones to not busy
    public synchronized void startDrones(){
        this.droneQueue=new ConcurrentLinkedQueue<>();
        for(Drone drone:this.getDrones()){
            drone.empty();
            this.droneQueue.add(drone);
        }
    }
    //set all of staff to not busy
    public synchronized void startStaff(){
        this.staffQueue=new ConcurrentLinkedQueue<>();
        for(Staff staff:this.getStaff()){
            staff.setBusy(false);
            staff.dishStock=null;
            staff.setStatus("idle");
            this.staffQueue.add(staff);
        }
    }

    public synchronized void startkitchen(){
        getStock().startKitchen();
    }
    //set of flag sent to all the orders that are not complete to false and return the reserved dishes
    public synchronized void startOders(){
        for(Order order :this.getNonCompleteOrders()){
            order.setSent(false);
            order.returnReady(getStock());
        }
    }
}
