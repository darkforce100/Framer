package edu.lonestar.framer.util;

import android.app.Activity;

/**
 * Created by husse on 1/27/2018.
 */

public class ArtistSwitchModel extends Activity{
   public  boolean selected = false;
    public String name;
   public ArtistSwitchModel(String artist, boolean select)
{
    // assigning them
    this.selected = select;
    this.name = artist;

}
// creating second one for just string
    public ArtistSwitchModel(String artist)
    {
        //just setting the name
        this.name = artist;
    }

    public String getName(){
        return this.name;
    }
    public boolean getValue(){
        return this.selected;
    }



}
