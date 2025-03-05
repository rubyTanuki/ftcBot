package ftcbot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import webscraper.*;
import java.util.*;

import javax.security.auth.login.LoginException;
public class DiscordBot extends ListenerAdapter{

    private static ArrayList<Product> lastSearchResults = new ArrayList<>();

    private final int SEARCH_RESULT_NUM = 3;
    
    static JDA jda;

    private enum Action{
        IDLE,
        SEARCH,
        ADD_TO_CART
    }

    private Action lastAction = Action.IDLE;
    public static void main(String[] args) throws LoginException{
        String token = System.getenv("DISCORD_TOKEN");
        
        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Enable message content intent
                .addEventListeners(new DiscordBot())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot()){

            return;
        }

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannelUnion channel = event.getChannel();

        if(content.startsWith("!")){
            String command = content.substring(1, content.contains(" ")?content.indexOf(" "):content.length());
            switch(command){
                case "lastAction":
                    channel.sendMessage(lastAction + "").queue();
                case "cart":
                case "addToCart":
                    //adding to cart
                    if(lastAction==Action.SEARCH){
                        try{
                            String[] inputs = content.split(" ");
                            int selection = Integer.parseInt(inputs[1]);
                            int amt = inputs.length>2?Integer.parseInt(inputs[2]):1;
                            Product product = lastSearchResults.get(selection-1);
                            channel.sendMessage("Added " + amt + " " + product.getName() + (amt>1?" s":"") + " To Cart").queue();
                            ProductFileWriter.writeToProductFile(product, amt);
                            lastAction = Action.ADD_TO_CART;
                        }catch(Exception e){}
                    }
                case "search":
                    //scraping search results
                    if(lastAction!=Action.ADD_TO_CART){
                        String search = content.substring(content.indexOf(" "));
                        if(search.contains(".com")){
                            String[] input = search.split(" ");
                            Product product = GoBildaScraper.getProductFromLink(input[1]);
                            int amt = input.length>2?Integer.parseInt(input[2]):1;
                            channel.sendMessage("Added " + amt + " " + product.getName() + (amt>1?" s":"") + " To Cart").queue();
                            ProductFileWriter.writeToProductFile(product, amt);
                        }else{
                            channel.sendMessage("searching...").queue();
                            lastSearchResults = GoBildaScraper.searchForGoBildaProduct(search);
                            editLastMessage(channel, "Which of these are you looking for?");
                            

                            for(int i=0;i<SEARCH_RESULT_NUM;i++){
                                channel.sendMessage(i+1 + "\n" + lastSearchResults.get(i).toString()).queue();
                            }
                            lastAction = Action.SEARCH;
                        }
                        
                    }else{
                        lastAction = Action.IDLE;
                    }
                    break;
                case "more":
                    //showing more results
                    if(lastAction==Action.SEARCH){
                        for(int i=SEARCH_RESULT_NUM;i<lastSearchResults.size();i++){
                            channel.sendMessage(i+1 + "\n" + lastSearchResults.get(i).toString()).queue();
                        }
                    }
                    break;
                case "email":
                    //send email to mcclusky
                    try{
                        ProductGMailer.sendEmail();
                        ProductFileWriter.clearProductFile();
                    }catch(Exception e){}
                    
                    channel.sendMessage("Sending email").queue();
                    break;
                case "clear cart":
                case "clear":
                    ProductFileWriter.clearProductFile();
                    channel.sendMessage("Cleared cart").queue();
                    break;
                default:
                    channel.sendMessage("Not a valid command").queue();
            }
        }
        
    }

    private void editLastMessage(MessageChannelUnion channel, String text){
        try{
            List<Message> messages = channel.getHistory().retrievePast(50).submit().get(); // Adjust the limit as needed
            // Find the last message sent by the bot
            Message lastBotMessage = null;
            for (Message message : messages) {
                if (message.getAuthor().getId().equals(jda.getSelfUser().getId())) {
                    lastBotMessage = message;
                    break;
                }
            }
            lastBotMessage.delete().queue();
            channel.sendMessage(text).queue();
        }catch(Exception e){}
        
    }
}
