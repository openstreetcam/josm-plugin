/*
 * Copyright 2019 Grabtaxi Holdings PTE LTE (GRAB), All rights reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file.
 *
 */
package org.openstreetmap.josm.plugins.kartaview.service.photo;

import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.BBOX_BOTTOM_RIGHT;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.BBOX_TOP_LEFT;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.EXTERNAL_USER_ID;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.MY_TRACKS;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.MY_TRACKS_VAL;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.PAGE;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.PAGE_ITEMS;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.SEQUENCE_ID;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.SEQUENCE_INDEX;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.USER_TYPE;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.USER_TYPE_OSM;
import static org.openstreetmap.josm.plugins.kartaview.service.photo.RequestConstants.ZOOM;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.grab.josm.common.argument.BoundingBox;
import org.openstreetmap.josm.plugins.kartaview.util.cnf.Config;


/**
 * Helper class, builds the content for the HTTP POST methods.
 *
 * @author Beata
 * @version $Revision$
 */
final class HttpContentBuilder {

    private static final String SEPARATOR = ",";
    private static final String DATE_FORMAT = "YYYY-MM-dd";

    private final Map<String, String> content = new HashMap<>();


    HttpContentBuilder(final BoundingBox area, final Date date, final Long osmUserId, final Paging paging) {
        content.put(BBOX_TOP_LEFT, area.getNorth() + SEPARATOR + area.getWest());
        content.put(BBOX_BOTTOM_RIGHT, area.getSouth() + SEPARATOR + area.getEast());
        if (date != null) {
            content.put(RequestConstants.DATE, new SimpleDateFormat(DATE_FORMAT).format(date));
        }

        if (osmUserId != null && osmUserId > 0) {
            content.put(EXTERNAL_USER_ID, Long.toString(osmUserId));
            content.put(USER_TYPE, USER_TYPE_OSM);
        }

        if (paging == null) {

            addPaging(Paging.NEARBY_PHOTOS_DEAFULT);
        } else {
            addPaging(paging);
        }
    }

    HttpContentBuilder(final BoundingBox area, final Long osmUserId, final int zoom, final Paging paging) {
        content.put(BBOX_TOP_LEFT, area.getNorth() + SEPARATOR + area.getWest());
        content.put(BBOX_BOTTOM_RIGHT, area.getSouth() + SEPARATOR + area.getEast());

        if (zoom >= Config.getInstance().getTracksMaxZoom()) {
            content.put(ZOOM, Integer.toString(Config.getInstance().getTracksMaxZoom()));
        } else {
            content.put(ZOOM, Integer.toString(zoom));
        }
        if (osmUserId != null && osmUserId > 0) {
            content.put(EXTERNAL_USER_ID, Long.toString(osmUserId));
            content.put(USER_TYPE, USER_TYPE_OSM);
            content.put(MY_TRACKS, MY_TRACKS_VAL);
        }
        if (paging == null) {
            addPaging(Paging.TRACKS_DEFAULT);
        } else {
            addPaging(paging);
        }
    }

    HttpContentBuilder(final Long id) {
        content.put(SEQUENCE_ID, id.toString());
    }

    HttpContentBuilder(final Long sequenceId, final Integer sequenceIndex) {
        content.put(SEQUENCE_ID, Long.toString(sequenceId));
        content.put(SEQUENCE_INDEX, Integer.toString(sequenceIndex));
    }

    Map<String, String> getContent() {
        return content;
    }

    private void addPaging(final Paging paging) {
        content.put(PAGE, Integer.toString(paging.getPage()));
        content.put(PAGE_ITEMS, Integer.toString(paging.getItemsPerPage()));
    }
}