/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import HelpClasses.HelpUnsortedData;
import ServerApp.ServerMain;
import Shared.Data.IData;

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
     * @return true if sending unsortedData is successfull, otherwise false
     */
    public boolean sendUnsortedData(HelpUnsortedData unsortedData) {
        
        IData data = ServerMain.unsortedDatabaseManager.insertToUnsortedData(null);
        return (data != null);
    }
}
