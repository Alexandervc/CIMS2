/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import HelpClasses.HelpUnsortedData;
import ServerApp.ServerMain;
import Shared.Data.IData;
import Shared.Data.UnsortedData;
import Shared.Users.IUser;

/**
 *
 * @author Alexander
 */
public class ReportValidationController {
    
    public ReportValidationController() {
        
    }
    
    /**
     * Tries to send the unsortedData
     * @param unsortedData
     * @param uploader the signed in user
     * @return true if sending unsortedData is successfull, otherwise false
     */
    public boolean sendUnsortedData(HelpUnsortedData unsortedData, IUser uploader) {
        if(unsortedData == null || uploader == null) {
            throw new IllegalArgumentException("Kon waarden niet ophalen");
        }
        if(unsortedData.getCity() == null || unsortedData.getCity().isEmpty()) {
            throw new IllegalArgumentException("Voer een plaats in");
        }
        
        // Load values
        String location = "";
        if(unsortedData.getStreet() == null || unsortedData.getStreet().isEmpty()) {
            location = unsortedData.getCity();
        } else {
            location = unsortedData.getStreet() + ", " + unsortedData.getCity();
        }
        
        String source = uploader.getUsername();
        
        // Make new unsortedData
        IData newUnsortedData = new UnsortedData(unsortedData.getTitle(),
                                        unsortedData.getDescription(),
                                        location,
                                        source);
        
        IData data = ServerMain.unsortedDatabaseManager.insertToUnsortedData(newUnsortedData);
        return (data != null);
    }
}
