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
public interface ICitizen extends IUser {
    String getCity();
    String getStreet();
    void setDistance(int newsid, int distance);
    int getDistance(int newsid);
}
