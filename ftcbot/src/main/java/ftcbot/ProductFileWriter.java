package ftcbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import webscraper.*;

public class ProductFileWriter {
    static File productFile;
    private static final String filePath = "ftcbot/src/main/resources/products.txt";

    public static void createProductFile(){
        productFile = new File(filePath);
    }
    public static void clearProductFile(){
        try{
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write("");
            myWriter.close();
        }catch(Exception e){

        }
    }
    public static void writeToProductFile(Product product, int num){
        try{
            FileWriter myWriter = new FileWriter(filePath, true);
            myWriter.write("x" + num + "\n" + product.toFile());
            myWriter.close();
        }catch(Exception e){

        }
    }

    public static String readProductFile() throws IOException{
        Scanner sc = new Scanner(new File("ftcbot/src/main/resources/products.txt")).useDelimiter("\\A");
        String text = sc.next();
        sc.close();
        return text;
    }

    public static void sendEmail(){
        
    }
    
}
