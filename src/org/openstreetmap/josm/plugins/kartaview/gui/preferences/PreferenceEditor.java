/*
 * Copyright 2019 Grabtaxi Holdings PTE LTE (GRAB), All rights reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file.
 *
 */
package org.openstreetmap.josm.plugins.kartaview.gui.preferences;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.openstreetmap.josm.plugins.kartaview.util.cnf.GuiConfig;
import org.openstreetmap.josm.plugins.kartaview.util.cnf.IconConfig;
import org.openstreetmap.josm.plugins.kartaview.util.pref.PreferenceManager;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.kartaview.argument.CacheSettings;
import org.openstreetmap.josm.plugins.kartaview.argument.PreferenceSettings;


/**
 * Defines the preference editor settings.
 *
 * @author beataj
 * @version $Revision$
 */
public class PreferenceEditor extends DefaultTabPreferenceSetting {

    private final PreferencePanel pnlPreference;


    public PreferenceEditor() {
        super(IconConfig.getInstance().getPluginIconName(), GuiConfig.getInstance().getPluginLongName(),
                GuiConfig.getInstance().getPluginTlt());
        pnlPreference = new PreferencePanel();
    }


    @Override
    public void addGui(final PreferenceTabbedPane pnlParent) {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(pnlPreference, BorderLayout.NORTH);
        createPreferenceTabWithScrollPane(pnlParent, mainPanel);
    }

    @Override
    public boolean ok() {
        final PreferenceSettings settings = pnlPreference.getSelectedSettings();
        final CacheSettings oldCacheSettings =
                PreferenceManager.getInstance().loadPreferenceSettings().getCacheSettings();
        PreferenceManager.getInstance().savePreferenceSettings(settings);
        return !settings.getCacheSettings().equals(oldCacheSettings);
    }
}