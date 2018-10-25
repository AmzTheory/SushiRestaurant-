package MyClasses;

import javax.swing.*;
import java.io.*;

public class DataPersistence {

    public static Configuration readBusinessApp(int file){
        ObjectInputStream reader=null;
        try {
             String path= Feature.formatRelativePath("\\MyClasses\\save"+file);
             reader = new ObjectInputStream(new FileInputStream(new File(path)));

            Configuration config=(Configuration)reader.readObject();

            return config;
        }catch (FileNotFoundException exe){
            JOptionPane.showMessageDialog(null,"make sure the file directory does exist");
        }catch (ClassNotFoundException exe){
            JOptionPane.showMessageDialog(null,"sorry can you select the configuration file again");
        }catch (EOFException exe){
            if(file==3)
                return null;
            else
                return readBusinessApp(file+1);
        }catch (IOException EX){

        }
        finally{
            try {
                if(reader!=null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Configuration("");

    }
    public static boolean writeBusinessApp(Configuration config,int file){
        ObjectOutputStream writer=null;
        try {
            String path= Feature.formatRelativePath("\\MyClasses\\save"+file);
            writer = new ObjectOutputStream(new FileOutputStream(new File(path)));

           writer.writeObject(config);
        }catch (Exception EX){
            EX.printStackTrace();
            return false;
        }finally{
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
