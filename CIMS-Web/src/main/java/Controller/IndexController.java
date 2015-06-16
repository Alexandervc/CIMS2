/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import ServerApp.Database.SortedDatabaseManager;
import ServerApp.ServerMain;
import Shared.Data.Advice;
import Shared.Data.INewsItem;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 *
 * @author Alexander
 */
public class IndexController {
    
    public IndexController() {
    }
    
    public List<INewsItem> getNewsItems(int offset, int limit) {        
        return ServerMain.sortedDatabaseManager.getNewsItems(offset, limit);
    }
    
    public int getNewsItemCount() {
        return ServerMain.sortedDatabaseManager.getNewsItemCount();
    }
}
