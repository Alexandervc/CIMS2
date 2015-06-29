/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import ServerApp.ServerMain;
import Shared.Data.INewsItem;
import Shared.NetworkException;
import com.google.maps.*;
import com.google.maps.model.*;
import java.util.List;

/**
 *
 * @author Alexander
 */
public class IndexController {
    
    public IndexController() {
    }
    
    public List<INewsItem> getNewsItems(int offset, int limit) throws NetworkException {        
        return ServerMain.sortedDatabaseManager.getNewsItems(offset, limit);
    }
    
    public int getNewsItemCount() throws NetworkException {
        try {
            int i = ServerMain.sortedDatabaseManager.getNewsItemCount();
            return i;
        } catch (NullPointerException ex) {
            return 0;
        }
    }
    
    public String getFile(String photoName) {
       return "http://athena.fhict.nl/users/i204267/" + photoName;
    }
    
    public int calculateDistance(String location, String livingplace) {
        try {
            //server key
            //GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyDYRxQCxF__dbvcKKragjuaEVDCRFNPsIw");
            
            //client key
            GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyDLsAkb6W8XFkcN-Cw3C38JawrqkN04oCU");
            
            GeocodingResult[] results =  GeocodingApi.geocode(context,
                location).await();
            GeocodingResult[] results2 =  GeocodingApi.geocode(context,
                livingplace).await();
            System.out.println(results[0].formattedAddress);
            System.out.println(results2[0].formattedAddress);
            
            return 10;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return -1;
        }
    }
}
