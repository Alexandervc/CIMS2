/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Kargathia
 */
public class NewsItem implements INewsItem {

    private HashSet<Situation> situations;
    private int victims, ID;
    private String title, description, location, source;
    private Date date;
    private List<String> pictures;

    /**
     * id specified
     * @param ID
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source the username of the source, cannot be null or empty
     * @param situations
     * @param victims has to be zero or greater
     * @param date
     */
    public NewsItem(int ID,String title, String description, String location,
            String source, HashSet<Situation> situations, int victims, Date date) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        if(source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Voer een bron in");
        }
        if(victims < 0) {
            throw new IllegalArgumentException("Slachtoffers moet 0 of meer zijn");
        }
        this.situations = situations;
        this.victims = victims;
        this.ID = ID;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
        this.date = date;

        if(this.situations == null){
            this.situations = new HashSet<>();
        }
        
        this.pictures = new ArrayList<>();
    }

    /**
     * no id specified
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source the username of the source, cannot be null or empty
     * @param situations
     * @param victims has to be zero or greater
     */
    public NewsItem(String title, String description, String location,
            String source, HashSet<Situation> situations, int victims) {
        this(-1, title, description, location, source, situations, victims, null);
    }
    
    @Override
    public void setID(int ID){
        this.ID = ID;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public HashSet<Situation> getSituations() {
        return situations;
    }

    @Override
    public int getVictims() {
        return victims;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public Date getDate() {
        return date;
    }
    
    @Override
    public String getDateString() {
        DateFormat date = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat time = new SimpleDateFormat("HH:mm");

        return date.format(this.date) + " om " + time.format(this.date) + " uur";
    }
    
    @Override
    public String getCity(){
        String[] location = this.location.split(",");
        
        if (location.length > 1) {
            String l = location[1];
            
            if (l.length() > 0) {
                l.trim();
            }
            
            return l;
        }
        
        return "";
    }
    
    @Override
    public String getStreet(){
        String[] location = this.location.split(",");
        
        if (location.length > 1) {
            String l = location[0];
            
            if (l.length() > 0) {
                l.trim();
            }
            
            return l;
        }
        
        return "";
    }
    
    @Override
    public List<String> getPictures() {
        return this.pictures;
    }
    
    /**
     * 
     * @param picture the ftp-link, cannot be null or empty
     */
    @Override
    public void addPicture(String picture) {
        if(picture == null || picture.isEmpty()) {
            throw new IllegalArgumentException("Voeg een foto toe");
        }
        this.pictures.add(picture);
    }

    public void addSituation(Situation sit){
        this.situations.add(sit);
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.ID;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NewsItem other = (NewsItem) obj;
        if (this.ID != other.ID) {
            return false;
        }
        return true;
    }
    
    
}
