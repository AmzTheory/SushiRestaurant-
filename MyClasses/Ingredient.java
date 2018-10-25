package MyClasses;
import common.Model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Ingredient extends Model implements Serializable {
    private static final long serialVersionUID = 5L;

    private String unit;
    private Supplier supplier;

    transient private Object lockA=new Object();
    public Ingredient(String name, String unit, Supplier sup) {
        this.setName(name);
        this.setUnit(unit);
        this.setSupplier(sup);
    }

    public synchronized String getUnit() {
        return unit;
    }

    public synchronized void setUnit(String unit) {
        this.notifyUpdate("Unit", this.unit, unit);
        this.unit = unit;
    }

    public synchronized Supplier getSupplier() {
        synchronized (lockA) {
            return supplier;
        }
    }

    public synchronized void setSupplier(Supplier supplier) {
        synchronized (lockA) {
            this.notifyUpdate("Supplier", this.supplier, supplier);
            this.supplier = supplier;
        }
    }

    @Override
    public synchronized String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        Ingredient instance = (Ingredient) o;
        if (instance.getName().equals(this.getName()))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.getName().length(); i++) {
            hash += this.getName().charAt(i);
        }
        return hash;

    }

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }

}
