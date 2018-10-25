package MyClasses;
import common.Model;

import java.io.IOException;
import java.io.ObjectInputStream;

public class User extends Model{
    private static final long serialVersionUID = 12L;

    private String password;
    private String address;
    private Postcode postcode;
    transient Object lockA=new Object();
    public User(String userName,String password,String address,Postcode postCode){
        this.setName(userName);
        this.setPassword(password);
        this.setAddress(address);
        this.setPostcode(postCode);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized Postcode getPostcode() {
        synchronized (lockA) {
            return postcode;
        }
    }

    public synchronized void setPostcode(Postcode postcode) {
        synchronized (lockA) {
            this.postcode = postcode;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        User instance=(User)o;
        if (this.getName().equals(instance.getName()))
            return true;
        else
            return  false;
    }
    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        lockA=new Object();
    }


}
