package webscraper;

import java.util.List;
import java.util.ArrayList;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class GoBildaScraper 
{
    public static ArrayList<Product> searchForGoBildaProduct(String search){
        String adjustedSearch = search.replaceAll("hex", "rex");

        System.setProperty("webdriver.http.factory", "jdk-http-client");

        String url = "https://www.gobilda.com/search-results-page?q=" + adjustedSearch;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");

        WebDriver driver = new ChromeDriver(options);

        return getProductsFromSearch(driver, url);
    }

    private static ArrayList<Product> getProductsFromSearch(WebDriver driver, String url){
        driver.get(url);
        ArrayList<Product> results = new ArrayList<>();
        try{
            List<WebElement> resultItems = driver.findElements(By.cssSelector("li.snize-product"));
            System.out.println("found " + resultItems.size() + " results");
            for(int i=0;i<Math.min(resultItems.size(), 8);i++){
                try{
                    WebElement item = resultItems.get(i);
                    String productName = item.findElement(By.cssSelector("a[aria-label]")).getAttribute("aria-label");
                    String link = item.findElement(By.cssSelector("a[href]")).getAttribute("href");
                    String imageUrl = item.findElement(By.cssSelector("img[src]")).getAttribute("src");
                    results.add(new Product(link, productName, imageUrl));
                }catch(Exception e){
                    break;
                }
                
            }
        }catch(Exception e){
            return null;
        }
        driver.quit();
        return results;
    }

    public static Product getProductFromLink(String url){
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");

        WebDriver driver = new ChromeDriver(options);
        driver.get(url);

        try{
            String productName = driver.findElement(By.cssSelector("h1[class]")).getAttribute("class");
            String imageUrl = driver.findElement(By.cssSelector("img[srcset]")).getAttribute("srcset");
            return new Product(url, productName, imageUrl);   
        }catch(Exception e){
            return null;
        }
    }
}

