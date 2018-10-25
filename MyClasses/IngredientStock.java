package MyClasses;


public class IngredientStock extends Stock{
    private static final long serialVersionUID = 6L;
    private Ingredient ingredient;;
    public IngredientStock(Ingredient ingredient,int thereshold,int restocking){
        super(thereshold,restocking);
        this.setIngredient(ingredient);
        this.setIngredient(ingredient);
    }
    public synchronized Ingredient getIngredient() {
        return ingredient;
    }

    public synchronized void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }
    @Override
    public synchronized void orderStock(){
        this.setCurrentQuantity(this.getRestockingAmount());
    }
    public synchronized int getCurrentQuantity() {
        return currentQuantity;
    }

    public synchronized void setCurrentQuantity(int currentQuantity) {

        this.currentQuantity = currentQuantity;
    }

    public synchronized boolean isInProgress() {
        return inProgress;
    }

    public synchronized void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public  synchronized int getThereshold() {
        return thereshold;
    }

    public  synchronized void setThereshold(int thereshold) {
        this.thereshold = thereshold;
    }

    public synchronized int getRestockingAmount() {
        return restockingAmount;
    }

    public synchronized void setRestockingAmount(int restockingAmount) {
        this.restockingAmount = restockingAmount;
    }

    public synchronized boolean checkRequireOrder() {
        if(this.getCurrentQuantity()<=this.getThereshold())
            return true;
        return false;
    }

    public synchronized void getFromStock(int value){
        this.setCurrentQuantity(this.getCurrentQuantity()-value);
    }
}
