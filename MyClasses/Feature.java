package MyClasses;

import java.io.File;
import java.util.Random;

public class Feature {

    public static int getRandom(int lower,int upper){
        return new Random().nextInt(upper)+lower;
    }
    public static String formatRelativePath(String path){
        String basePath = new File("").getAbsolutePath();
        return basePath.concat(path);
    }
}
