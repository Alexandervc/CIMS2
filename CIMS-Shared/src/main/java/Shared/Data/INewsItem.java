/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Kargathia
 */
public interface INewsItem extends IData {
    public Set<Situation> getSituations();
    public int getVictims();
    public Date getDate();
    public String getDateString();
    public void setID(int ID);
    public void setDate(Date date);
    public String getCity();
    public String getStreet();
    public List<String> getPictures();
    /**
     * 
     * @param picture the ftp-link, cannot be null or empty
     */
    public void addPicture(String picture);
}
