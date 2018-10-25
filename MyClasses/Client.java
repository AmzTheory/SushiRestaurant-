package MyClasses;
import client.ClientInterface;
import common.UpdateEvent;
import common.UpdateListener;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client implements ClientInterface,Serializable{
    private User loginUser;
    private List<UpdateListener> listeners;
    private Order basket;

    public Client(){
        listeners=new ArrayList<UpdateListener>();
        loginUser=null;
        basket=null;
    }
    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        User user=new User(username,password,address,postcode);
        try {
            boolean result = (boolean) sendMessage("register", user);
            if (result) {
                loginUser = user;
                basket = new Order(loginUser);
                return user;
            }
        }catch (ConnectionLost ex) {}
        return null;
    }

    @Override
    public User login(String username, String password) {

        User user=new User(username,password,null,null);
        try {
            user = (User) sendMessage("login", user);
            if (user != null) {
                loginUser = user;
                basket = new Order(user);
                return user;
            }
        }catch (ConnectionLost ex){
        }
        return null;
    }

    @Override
    public List<Postcode> getPostcodes() {
        try {
            return (ArrayList<Postcode>) sendMessage("postcodes", null);
        }catch (ConnectionLost conn){}
        return new ArrayList<Postcode>();
    }

    @Override
    public List<Dish> getDishes() {
        try {
            return (ArrayList<Dish>) sendMessage("dishes", null);
        }catch (ConnectionLost connectionLost){
        }
        return new ArrayList<Dish>();
    }

    @Override
    public String getDishDescription(Dish dish) {
        return dish.getDescription();
    }

    @Override
    public Number getDishPrice(Dish dish) {
        return dish.getPrice();
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {
        return basket.getDishes();
    }

    @Override
    public Number getBasketCost(User user) {
        return  basket.getTotalPrice();
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
         basket.addDish(dish,(int)quantity);
         double newTotal=(double)dish.getPrice()*(int)quantity;
         this.notifyUpdate();
    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
       basket.changeQunatity(dish,(int)quantity);
     //   this.notifyUpdate();
    }

    @Override
    public Order checkoutBasket(User user) {
        try {
            if (loginUser != null) {
                sendMessage("add order", basket);
                clearBasket(user);
            }
        }catch (ConnectionLost conn){};

        return null;
    }

    @Override
    public void clearBasket(User user) {
        this.basket.clearOrder();
        basket=new Order(user);
        this.notifyUpdate();
    }

    @Override
    public List<Order> getOrders(User user) {
        try {
            if (loginUser != null) {

                ArrayList<Order> orders = (ArrayList<Order>) sendMessage("orders for", user);
                return orders;
            }
        }catch (ConnectionLost Excep){}
         catch (Exception exp){}
        return new ArrayList<>();
    }

    @Override
    public boolean isOrderComplete(Order order)
    {
        this.notifyUpdate();
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
    public void cancelOrder(Order order) {
        try{
        if(loginUser!=null) {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the order", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean managed = (boolean) sendMessage("cancel order", order);
                if (managed)
                    JOptionPane.showMessageDialog(null, "The order has been deleted");
                else
                    JOptionPane.showMessageDialog(null, "Order can't be deleted by the when current state of delivery ");
            }
            this.notifyUpdate();

          }
        }catch(ConnectionLost ex){}

    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        for(UpdateListener list:listeners){
            list.updated(new UpdateEvent());
        }
    }
    public Object sendMessage(String title,Object attach) throws ConnectionLost{
        try {
            Message message = new Message(title);
            if (attach != null)
                message.setAttach(attach);
            SpecialSocket soc = Comms.sendMessage(message);
            Message recieveMessage = Comms.receiveMessage(soc);
            return recieveMessage.getAttach();
        }catch (ConnectionLost Ex){
            JOptionPane.showMessageDialog(null,"Connection has been lost with the server");
            throw new ConnectionLost();
        }
    }
}
