/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import HelpClasses.HelpUser;
import ServerApp.ServerMain;
import Shared.Users.Citizen;
import Shared.Users.ICitizen;

/**
 *
 * @author Alexander
 */
public class RegisterValidationController {
    
    public RegisterValidationController() {
        
    }
    
    /**
     * Tries to register the given user
     * @param registeredUser
     * @return true if the user is successfull registered, otherwise false
     */
    public boolean registerUser(HelpUser registeredUser) {
        if(registeredUser == null) {
            throw new IllegalArgumentException("Kon waarden niet ophalen");
        }
        if(registeredUser.getPassword() == null || registeredUser.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Voer een wachtwoord in");
        }
        if(!registeredUser.getPassword().equals(registeredUser.getRepeatPassword())) {
            throw new IllegalArgumentException("Beide wachtwoorden komen niet overeen");
        }
        if(ServerMain.tasksDatabaseManager.getUser(registeredUser.getUsername()) != null) {
            throw new IllegalArgumentException("Er bestaat al een gebruiker met deze gebruikersnaam");
        }
        
        ICitizen newCitizen = new Citizen(registeredUser.getUsername(),
                                        registeredUser.getName(),
                                        registeredUser.getCity(),
                                        registeredUser.getStreet());
        
        newCitizen = ServerMain.tasksDatabaseManager.registerCitizen(newCitizen, 
                                                    registeredUser.getPassword());
        return (newCitizen != null);
    }
}
