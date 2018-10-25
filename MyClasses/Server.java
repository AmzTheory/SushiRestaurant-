package MyClasses;
import common.UpdateEvent;
import common.UpdateListener;
import server.ServerInterface;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.*;

public class Server  implements ServerInterface{
    private boolean changeDish;
    private boolean changeIngredient;
    Configuration config;
    Thread controlThread;



    private ArrayList<UpdateListener> listeners;
    public Server(){

        Comms.receiveMessage(this);
        config=DataPersistence.readBusinessApp(1);
         if(config!=null) {
             config.startDrones();
             config.startStaff();
             config.startkitchen();
             config.startOders();
             config.saveData();
             controlThread = new Thread(new Monitor(config));
             controlThread.start();
         }
         listeners=new ArrayList<UpdateListener>();


    }
    @Override
    public void loadConfiguration(String filename) throws FileNotFoundException {
        //here list of user supplier....   will be filled
        try {
            config=new Configuration(filename);
            config.saveData();
            if(controlThread!=null)
                controlThread.interrupt();
            controlThread=new Thread(new Monitor(config));
            controlThread.start();
            this.notifyUpdate();


        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
    public boolean addUser(User U){
        return this.config.addUser(U);
    }
    public User checkUser(String name, String pass){
        return this.config.checkUser(name,pass);
    }
    public ArrayList<Order> getOrdersFor(User u){
        return this.config.getOrdersFor(u);
    }
    public void addorder(Order o){
        this.config.addOrder(o);
    }
    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        this.changeIngredient=enabled;
    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        this.changeDish=enabled;
    }

    @Override
    public void setStock(Dish dish, Number stock) {
        if((int)stock >0)
            config.setDishStock(dish,stock);
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        this.config.setIngredientStock(ingredient,stock);
    }

    @Override
    public List<Dish> getDishes() {
        return config.getDishes();
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        Dish dish=new Dish(name,description,(int)price);
        if(check(restockAmount,restockThreshold)) {
            this.config.addDish(dish, restockAmount, restockThreshold);
        }
        this.notifyUpdate();
        return dish;
    }

    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {
        try {
            this.config.removeDish(dish);
            this.notifyUpdate();
        }catch (Exception exp){

        }

    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {

        this.config.addIngredientToDish(dish,ingredient,quantity);
        this.notifyUpdate();
    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {

        dish.removeIngredient(ingredient);
    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        dish.setIngredients((HashMap<Ingredient,Number>)recipe);
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        this.config.setRestockLevel(dish,restockThreshold,restockAmount);
    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        return this.config.getRestockLevel(dish).getThereshold();
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        return this.config.getRestockLevel(dish).getRestockingAmount();
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return dish.getIngredients();
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return this.config.getDishesStockLevel();
    }

    @Override
    public List<Ingredient> getIngredients() {
        List<Ingredient> listOfIngredients=new ArrayList<>(this.config.getIngredients());
        return listOfIngredients;
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        Ingredient ing = new Ingredient(name, unit, supplier);
        if (check(restockAmount, restockThreshold))
            this.config.addIngredient(ing, (int) restockAmount, (int) restockThreshold);

            notifyUpdate();
            return ing;
        }


    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
        try {
            this.config.removeIngredient(ingredient);
            this.notifyUpdate();
        }catch (Exception ex){

        }
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        if(check(restockAmount,restockThreshold))
            this.config.setRestockLevel(ingredient,restockThreshold,restockAmount);
    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return this.config.getRestockLevel(ingredient).getThereshold();
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return this.config.getRestockLevel(ingredient).getRestockingAmount();
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {

        return this.config.getIngredientLevel();
    }

    @Override
    public List<Supplier> getSuppliers() {
        return this.config.getSuppliers();
    }

    @Override
    public Supplier addSupplier(String name, Number distance) {
        Supplier supplier=new Supplier(name,distance);
        this.config.addSupplier(supplier);
        this.notifyUpdate();
        return supplier;
    }

    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
        try {
            this.config.removeSupplier(supplier);
            this.notifyUpdate();
        }catch (Exception ex){}
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    @Override
    public List<Drone> getDrones() {
        return this.config.getDrones();
    }

    @Override
    public Drone addDrone(Number speed) {
        Drone drone=this.config.addDrone(speed);
        this.notifyUpdate();
        return drone;
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        try {
            this.config.removeDrone(drone);
            this.notifyUpdate();
        }catch (Exception ex){}

    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }


    @Override
    public String getDroneStatus(Drone drone) {
        return drone.getStatus();
    }

    @Override
    public List<Staff> getStaff() {
        return this.config.getStaff();
    }

    @Override
    public Staff addStaff(String name) {
        Staff staffnew=new Staff(name,this.config.getStock());
        this.config.addStaff(staffnew);
        this.notifyUpdate();
        return staffnew;
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        try{
            this.config.removeStaff(staff);
            this.notifyUpdate();
        }catch (Exception exe){

        }
    }

    @Override
    public String getStaffStatus(Staff staff) {
        return staff.getStatus();
    }

    @Override
    public List<Order> getOrders() {
        return this.config.getOrders();
    }

    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        try{
            try {
                this.config.removeOrder(order);
            }catch (ServerInterface.UnableToDeleteException exe){
                if(exe.getMessage().equals("1"))
                    JOptionPane.showMessageDialog(null,"this order doesn't exist");
                else if (exe.getMessage().equals("2"))
                    JOptionPane.showMessageDialog(null,"this order can't be deleted when is in state of delivery ");
            }
            this.notifyUpdate();
        }catch (Exception exp){}
    }

    public boolean removeOrderForCustomer(Order order) throws UnableToDeleteException {
        try{
            order=this.config.getOrder(order);
            this.config.removeOrder(order);
            this.notifyUpdate();
            return true;
        }catch (Exception exp){
            return false;
        }
    }

    @Override
    public Number getOrderDistance(Order order) {
        return order.getUser().getPostcode().getDistance();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        return order.isComplete();
    }

    @Override
    public String getOrderStatus(Order order) {
        return order.getStatus();
    }

    @Override
    public Number getOrderCost(Order order) {
        return order.getTotalPrice();
    }

    @Override
    public List<Postcode> getPostcodes() {
        return this.config.getPostcodes();
    }

    @Override
    public void addPostcode(String code, Number distance) {
        this.config.addPostcode(new Postcode(code,distance));
        this.notifyUpdate();
    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        try{
            this.config.removePostCode(postcode);
            this.notifyUpdate();
        }catch (Exception exp){}
    }

    @Override
    public List<User> getUsers() {
        return this.config.getUsers();
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        try{
            this.config.removeUser(user);
            this.notifyUpdate();
        }catch (Exception exp){}
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        for(UpdateListener listener : listeners) {
            listener.updated(new UpdateEvent());
        }
    }
 //it checks
   private boolean check(Number restocked,Number threshold){
        if((int)restocked<=(int)threshold) {
            JOptionPane.showMessageDialog(null,"The restocked Amount should be higher than threshold");
            return false;
        }else if((int)restocked>0  && (int) threshold >0){
           JOptionPane.showMessageDialog(null,"can't inset negative value for restock amount and thersold");
            return false;
        }

       return true;
   }}