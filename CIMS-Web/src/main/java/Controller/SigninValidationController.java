/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import HelpClasses.HelpUser;
import ServerApp.ServerMain;
import Shared.Users.IUser;

/**
 *
 * @author Alexander
 */
public class SigninValidationController {
    
    public SigninValidationController() {
        
    }
    
    /**
     * Tries to sign in the given user
     * @param tempSigninUser
     * @return true if sign in is succesfull, otherwise false
     */
    public IUser signIn(HelpUser tempSigninUser) {
        if(tempSigninUser == null) {
            throw new IllegalArgumentException("Kon waarden niet ophalen");
        }
        if(tempSigninUser.getUsername() == null || tempSigninUser.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Voer een gebruikersnaam in");
        }
        if(tempSigninUser.getPassword() == null || tempSigninUser.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Voer een wachtwoord in");
        }
        
        return ServerMain.tasksDatabaseManager.loginUser(tempSigninUser.getUsername(),
                                                        tempSigninUser.getPassword());
    }
}
