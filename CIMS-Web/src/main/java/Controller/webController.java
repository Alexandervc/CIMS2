/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import ServerApp.ServerMain;
import Shared.Data.Advice;
import Shared.Data.INewsItem;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.*;

/**
 *
 * @author Linda
 */
public class webController extends HttpServlet {

    private HashSet<Situation> situations = new HashSet<Situation>();
    private HashSet<Advice> advices = new HashSet<Advice>();
    private List<INewsItem> news = new ArrayList<INewsItem>();
    private HashMap<Integer,Integer> distances = new HashMap<>();
    private Date date = new Date();

    public webController() {
        //File tmpDir = (File)getServletContext().getAttribute(ServletContext.TEMPDIR);
        advices.add(new Advice(100, "Sluit ramen en deuren"));
        situations.add(new Situation(10, advices, "Gevaarlijke stoffen"));
        news.add(new NewsItem(1, "Title1", "Description1", "Rachelsmolen, Eindhoven",
                "Source1", situations, 0, date));
        news.add(new NewsItem(2, "Title2", "Description2", "Rachelsmolen, Eindhoven",
                "Source2", situations, 1, date));
        news.add(new NewsItem(3, "Title3", "Description3", "Rachelsmolen, Eindhoven",
                "Source3", situations, 2, date));
        ServerMain.ftpManager.uploadFile("E:/64277_816878888362056_5899858130298802750_n.jpg", "UnitTest1.jpg");
        news.get(0).addPicture("UnitTest1.jpg");
    }

    public INewsItem getNewsWithID(String ID) {
        return ServerMain.sortedDatabaseManager.getNewsItemByID(Integer.parseInt(ID));
    }

    public String getIconURL(Situation situation) {
        if (situation.getDescription().equals("Giftige stoffen")) {
            return "images/icon_stoffen.png";
        } else if (situation.getDescription().equals("Inbraak")) {
            return "images/icon_inbraak.png";
        } else if (situation.getDescription().equals("Levensgevaar")) {
            return "images/icon_leven.png";
        } else if (situation.getDescription().equals("Terroristische aanslag")) {
            return "images/icon_aanslag.png";
        } else if (situation.getDescription().equals("Brand")) {
            return "images/icon_brand.png";
        } else if (situation.getDescription().equals("Instortingsgevaar")) {
            return "images/icon_instorting.png";
        } else if (situation.getDescription().equals("Ordeverstoring")) {
            return "images/icon_orde.png";
        } else if (situation.getDescription().equals("Verkeersongeval")) {
            return "images/icon_verkeer.png";
        } else if (situation.getDescription().equals("Extreem weer")) {
            return "images/icon_weer.png";
        } else {
            return null;
        }
    }

    public HashSet<Advice> getAdvices(INewsItem item) {
        HashSet<Advice> set = new HashSet<Advice>();
        for (Situation sit : item.getSituations()) {
            for (Advice ad : sit.getAdvices()) {
                set.add(ad);
            }
        }
        return set;
    }

    public String getDate() {
        Date newsDate = new Date();
        DateFormat date = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat time = new SimpleDateFormat("HH:mm");

        return date.format(newsDate) + " om " + time.format(newsDate);
    }

    public String getFile(String photoName) {
        System.out.println("Filepath = " + "http://athena.fhict.nl/users/i204267/" + photoName);
        return "http://athena.fhict.nl/users/i204267/" + photoName;
    }

    public boolean sendPhoto(String path, INewsItem item) {
        try {
            System.out.println("filepath = " + path);
            String newName = "";
            if(!item.getPictures().isEmpty()){
            newName = "NewsItem" + item.getId() + "_" + item.getPictures().size() + 10 + ".jpg";
            }
            else
            {
                newName = "NewsItem" + item.getId() + "_9.jpg";
            }
            boolean upload = ServerMain.ftpManager.uploadFile(path, newName);
            boolean database = ServerMain.sortedDatabaseManager.insertPicture(item, newName);
            if (upload && database)
            {
                return true;
            }
            return false;
        } catch (Exception ex) {
            Logger.getLogger(webController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void calcDistance(int newsid, int distance) {
        if (distance >= 0) {
            distances.put(newsid, distance);
        } else {
            distances.put(newsid, -1);
        }
        
        System.out.println(distance);
    }
}
