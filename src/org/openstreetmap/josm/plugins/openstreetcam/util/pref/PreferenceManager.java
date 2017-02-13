/*
 *  Copyright 2016 Telenav, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openstreetmap.josm.plugins.openstreetcam.util.pref;

import java.util.Date;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.openstreetcam.argument.ListFilter;


/**
 * Utility class, manages save and load (put & get) operations of the preference variables. The preference variables are
 * saved into a global preference file. Preference variables are static variables which can be accessed from any plugin
 * class. Values saved in this global file, can be accessed also after a JOSM restart.
 *
 * @author Beata
 * @version $Revision$
 */
public final class PreferenceManager {

    private static final PreferenceManager INSTANCE = new PreferenceManager();

    private PreferenceManager() {}

    public static PreferenceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Loads the photo search error suppress flag. If this value is true, then all the future photo search service
     * errors will be suppressed.
     *
     * @return a boolean value
     */
    public boolean loadPhotosErrorSuppressFlag() {
        return Main.pref.getBoolean(Keys.SUPPRESS_PHOTOS_ERROR);
    }

    /**
     * Saves the photo search error suppress flag.
     *
     * @param flag a boolean value
     */
    public void savePhotosErrorSuppressFlag(final boolean flag) {
        Main.pref.put(Keys.SUPPRESS_PHOTOS_ERROR, flag);
    }

    /**
     * Loads the sequence error suppress flag. If this value is true, then all the future sequence retrieve errors will
     * be suppressed.
     *
     * @return a boolean value
     */
    public boolean loadSequenceErrorSuppressFlag() {
        return Main.pref.getBoolean(Keys.SUPPRESS_SEQUENCE_ERROR);
    }

    /**
     * Saves the sequence error suppress flag.
     *
     * @param flag a boolean value
     */
    public void saveSequenceErrorSuppressFlag(final boolean flag) {
        Main.pref.put(Keys.SUPPRESS_SEQUENCE_ERROR, flag);
    }

    /**
     * Saves the 'filtersChanged' flag to the preference file.
     *
     * @param changed a boolean value
     */
    public void saveFiltersChangedFlag(final boolean changed) {
        Main.pref.put(Keys.FILTERS_CHANGED, "");
        Main.pref.put(Keys.FILTERS_CHANGED, Boolean.toString(changed));
    }

    /**
     * Returns the 'filtersChanged' flag key.
     *
     * @return a string
     */
    public String getFiltersChangedFlagKey() {
        return Keys.FILTERS_CHANGED;
    }

    /**
     * Loads the list filters from the preference file.
     *
     * @return a {@code ListFilter}
     */
    public ListFilter loadListFilter() {
        final String dateStr = Main.pref.get(Keys.DATE);
        Date date = null;
        if (!dateStr.isEmpty()) {
            date = new Date(Long.parseLong(dateStr));
        }
        final String onlyUserFlagStr = Main.pref.get(Keys.ONLY_USER_FLAG);
        final boolean onlyUserFlag =
                onlyUserFlagStr.isEmpty() ? ListFilter.DEFAULT.isOnlyUserFlag() : Boolean.parseBoolean(onlyUserFlagStr);
        return new ListFilter(date, onlyUserFlag);
    }

    /**
     * Saves the list filter to the preference file.
     *
     * @param filter a {@code ListFilter} represents the current filter settings
     */
    public void saveListFilter(final ListFilter filter) {
        if (filter != null) {
            final String dateStr = filter.getDate() != null ? Long.toString(filter.getDate().getTime()) : "";
            Main.pref.put(Keys.DATE, dateStr);
            Main.pref.put(Keys.ONLY_USER_FLAG, filter.isOnlyUserFlag());
        }
    }

    /**
     * Loads the layer appearance status from the preference file.
     *
     * @return a boolean
     */
    public boolean loadLayerOpenedFlag() {
        final String layerOpened = Main.pref.get(Keys.LAYER_OPENED);
        return layerOpened.isEmpty() ? false : Boolean.valueOf(layerOpened);
    }

    /**
     * Saves the layer appearance status to the preference file.
     *
     * @param isLayerOpened represents the layer showing/hiding status
     */
    public void saveLayerOpenedFlag(final boolean isLayerOpened) {
        Main.pref.put(Keys.LAYER_OPENED, isLayerOpened);
    }

    /**
     * Loads the panel appearance status from the preference file.
     * 
     * @return a boolean
     */
    public boolean loadPanelOpenedFlag() {
        final String layerOpened = Main.pref.get(Keys.PANEL_OPENED);
        return layerOpened.isEmpty() ? false : Boolean.valueOf(layerOpened);
    }

    /**
     * Saves the panel appearance status to the preference file
     * 
     * @param isPanelOpened represents the panel showing/hiding status
     */
    public void savePanelOpenedFlag(final boolean isPanelOpened) {
        Main.pref.put(Keys.PANEL_OPENED, isPanelOpened);
    }

    /**
     * Saves the high quality photo flag to the preference file.
     * 
     * @param highQualityFlag if true then the high quality photos will be loaded
     */
    public void saveHighQualityPhotoFlag(final boolean highQualityFlag) {
        Main.pref.put(Keys.HIGH_QUALITY_PHOTO_FLAG, highQualityFlag);
    }

    /**
     * Loads the high quality photo flag from the preference file.
     * 
     * @return a boolean value
     */
    public boolean loadHighQualityPhotoFlag() {
        return Main.pref.getBoolean(Keys.HIGH_QUALITY_PHOTO_FLAG);
    }

    /**
     * Returns the high quality photo flag key.
     * 
     * @return a {@code String}
     */
    public String getHighQualityPhotoFlagKey() {
        return Keys.HIGH_QUALITY_PHOTO_FLAG;
    }

    public String getDisplayTrackFlagKey() {
        return Keys.DISPLAY_TRACK_FLAG;
    }

    public boolean loadDisplayTrackFlag() {
        final String displayTrackFlag = Main.pref.get(Keys.DISPLAY_TRACK_FLAG);
        return displayTrackFlag.isEmpty() ? true : Boolean.valueOf(displayTrackFlag);
    }

    public void saveDisplayTrackFlag(boolean displayTrackFlag) {
        Main.pref.put(Keys.DISPLAY_TRACK_FLAG, displayTrackFlag);
    }
}