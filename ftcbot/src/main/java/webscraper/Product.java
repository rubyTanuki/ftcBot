package webscraper;

public class Product {
    public String link;
    public String name;
    public String imageUrl;

    public Product(String link, String name, String imageUrl){
        this.link = link;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String toString(){
        return name + "\n" + link + "\n\n";
    }

    public String toFile(){
        return link + "\n\n";
    }

    public String getName(){
        return name;
    }
}
