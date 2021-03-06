/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Users;

/**
 *
 * @author Alexander
 */
public class Citizen extends User implements ICitizen {
    private String city;
    private String street;
    
    /**
     * 
     * @param username
     * @param name
     * @param city cannot be null or empty
     * @param street 
     */
    public Citizen(String username, String name, String city, String street) {
        super(username, name);
        if(city == null || city.isEmpty()) {
            throw new IllegalArgumentException("Voer een woonplaats in");
        }
        this.city = city;
        this.street = street;
    }

    @Override
    public String getCity() {
        return this.city;
    }

    @Override
    public String getStreet() {
        return this.street;
    }      
}
