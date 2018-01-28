/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lonestar.framer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.Vector;

import edu.lonestar.framer.util.ArtistSwitchModel;
import edu.lonestar.framer.util.RemoteImage;

public class SettingsActivity extends Activity {

    SharedPreferences sharedPref;
    ArtistListAdapter dataadapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPref = this.getSharedPreferences("framer", Context.MODE_PRIVATE);
        // setting the id to the list view
        final ListView list_view_artists = (ListView) findViewById(R.id.list);

        // creating the adapter object
        dataadapter = new ArtistListAdapter(this, refreshArtistSwitchVector());
                // setting the adapter
        new DownloadDaemon().parseString();

        list_view_artists.setAdapter(dataadapter);
        // settings id's
        final Switch Adaptive_matting = findViewById(R.id.switch2);
        final Switch displayNameplateSwitch = findViewById(R.id.displayNameplateSwitch);

        Adaptive_matting.setChecked(sharedPref.getBoolean("display_nameplate", false));
        Adaptive_matting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor a = sharedPref.edit();
                // just saving whether the adaptive matting is on or not
                a.putBoolean("adaptive_matting", Adaptive_matting.isChecked());
                a.apply();

            }
        });

        displayNameplateSwitch.setChecked(sharedPref.getBoolean("display_nameplate", false));
        displayNameplateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = sharedPref.edit();
                // just saving the name plate and whether it was checked
                e.putBoolean("display_nameplate", displayNameplateSwitch.isChecked());
                e.apply();

            }
        });
        //
        //
    }
// returning the actual vector

    public static Vector<ArtistSwitchModel> refreshArtistSwitchVector(){
        Vector<ArtistSwitchModel> finalArtists = new Vector<>();
        for (RemoteImage i: DownloadDaemon.obunfiltered){
            boolean found = false;
            for (ArtistSwitchModel s : finalArtists){
                if (s.name.equals(i.getArtist())){
                    found = true;
                }
            }
            if (!found){
                finalArtists.add(new ArtistSwitchModel(i.getArtist()));
            }
        }
        return finalArtists;
    }
}

