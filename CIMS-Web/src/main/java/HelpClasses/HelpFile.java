/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HelpClasses;

import Shared.Data.INewsItem;
import java.io.File;

/**
 *
 * @author Linda
 */
public class HelpFile {
    private INewsItem item;
    private File img;
    
    public HelpFile(){
        this.item = null;
        this.img = null;
    }
    
    public INewsItem getItem() {
        return item;
    }

    public void setItem(INewsItem item) {
        this.item = item;
    }
    
    public File getImg() {
        return img;
    }

    public void setImg(File img) {
        this.img = img;
    }
    
    
}
