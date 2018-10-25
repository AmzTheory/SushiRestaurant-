package MyClasses;

import java.io.Serializable;

public abstract class Stock implements Serializable{
    protected int currentQuantity;
    protected int thereshold;
    protected int restockingAmount;
    protected boolean inProgress;
    public Stock(int thereshold,int restockingAmount){
        this.setCurrentQuantity(0);
        this.setRestockingAmount(restockingAmount);
        this.setThereshold(thereshold);
    }
    public abstract int getCurrentQuantity() ;

    public abstract void setCurrentQuantity(int currentQuantity);


    public  abstract int getThereshold();

    public  abstract void setThereshold(int thereshold);

    public abstract int getRestockingAmount() ;

    public abstract void setRestockingAmount(int restockingAmount);
    public  abstract void orderStock();
    public abstract boolean checkRequireOrder();

    public abstract void getFromStock(int value);

    public abstract boolean isInProgress();

    public abstract void setInProgress(boolean inProgress);
}
