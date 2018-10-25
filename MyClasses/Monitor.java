package MyClasses;




public class Monitor implements Runnable {


    Configuration config;

    public Monitor(Configuration confi) {
        this.config = confi;
    }

    @Override
    public void run() {
        try {
            checkInterrupted();
            KitchenStock stock = config.getStock();
            DishStock dishStock = null;
            while (true) {
                checkInterrupted();

                for (Dish dish : stock.getDishStocks()) {
                    dishStock = stock.getDishStock(dish);
                    if (dishStock.checkRequireOrder()) {
                        if (checkDish(dishStock)) {
                            Staff first = config.getFirstStaff();
                            if (first != null) {
                                dishStock.addStaffPrepare();
                                first.prepareDish(stock.getDishStock(dish));
                                config.offerLastStaff(first);
                            }
                        }
                    } else if (dishStock.isInProgress()) {
                        checkDish(dishStock);
                        if ((dishStock.getCurrentQuantity() * 2) < dishStock.getRestockingAmount()) {
                            if (checkDish(dishStock)) {
                                Staff first = config.getFirstStaff();
                                if (first != null) {
                                    dishStock.addStaffPrepare();
                                    first.prepareDish(stock.getDishStock(dish));
                                    config.offerLastStaff(first);
                                }
                            }
                        }
                    }

                }

                checkInterrupted();
                //orders
                for (Order order : this.config.getOrders()) {
                    if (!order.isSent()) {
                        if (checkOrder(order)) {//check no issues
                            Drone first = config.getFirstDrone(true);
                            if (first != null) {
                                order.setSent(true);
                                first.sendOrder(order);//if the staff isn't  ask him to prepare
                                config.offerLastDrone(first);
                            }
                        }
                    }
                }

                checkInterrupted();
                //ingredients
                for (Ingredient ing : stock.getIngredientStocks()) {
                    if (stock.getIngredientStock(ing).checkRequireOrder() && !stock.getIngredientStock(ing).isInProgress()) {
                        Drone drone = config.getFirstDrone(false);
                        if (drone != null) {
                            drone.bringIngredient(stock.getIngredientStock(ing));
                            config.offerLastDrone(drone);
                        }
                    }

                }
                config.saveData();
            }

        }catch (InterruptedException exp){

        }
    }
    public void checkInterrupted() throws InterruptedException{

        if(Thread.currentThread().isInterrupted()) {
            System.out.println("Control Thread has been interrupted");
            this.config.saveData();
            throw new InterruptedException();
        }
    }
//in case the number of dishes asked is more than the current quantity
    public boolean checkOrder(Order order){
        for(Dish dish:order.getDishes().keySet()){
            DishStock dishStock=config.getStock().getDishStock(dish);
            int value=(int)order.getDishes().get(dish);
            boolean moreThanExist=value>dishStock.getCurrentQuantity();

            if(moreThanExist && !dishStock.isInProgress()) {
                Staff staff = config.getFirstStaff();
                if (staff != null) {
                    dishStock.addStaffPrepare();
                    staff.prepareDish(dishStock);
                    config.offerLastStaff(staff);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean checkDish( DishStock stock){
        Dish dish=stock.getDish();
        for(Ingredient ing:dish.getIngredients().keySet()){
            int value=dish.getAmountFor(ing);
            IngredientStock ingStock=config.getStock().getIngredientStock(ing);

            //incase the current ingredient is  not enought to prepare dish it will
            //ask drone to bring it
            if(value>ingStock.getCurrentQuantity() && !ingStock.isInProgress()){
                Drone drone=config.getFirstDrone(false);
                if(drone!=null){
                    drone.bringIngredient(ingStock);
                     config.offerLastDrone(drone);
                }else{
                    return false;
                }
            }
        }
        return true;
    }
}

