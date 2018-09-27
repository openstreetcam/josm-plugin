/*
 * The code is licensed under the LGPL Version 3 license http://www.gnu.org/licenses/lgpl-3.0.en.html.
 * The collected imagery is protected & available under the CC BY-SA version 4 International license.
 * https://creativecommons.org/licenses/by-sa/4.0/legalcode.
 *
 * Copyright (c)2016, Telenav, Inc. All Rights Reserved
 */
package org.openstreetmap.josm.plugins.openstreetcam.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.openstreetcam.argument.DataType;
import org.openstreetmap.josm.plugins.openstreetcam.argument.SearchFilter;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Author;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Cluster;
import org.openstreetmap.josm.plugins.openstreetcam.entity.ClusterBuilder;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Contribution;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Detection;
import org.openstreetmap.josm.plugins.openstreetcam.entity.EditStatus;
import org.openstreetmap.josm.plugins.openstreetcam.entity.HighZoomResultSet;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Photo;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Segment;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Sequence;
import org.openstreetmap.josm.plugins.openstreetcam.service.ServiceException;
import org.openstreetmap.josm.plugins.openstreetcam.service.apollo.ApolloService;
import org.openstreetmap.josm.plugins.openstreetcam.service.photo.OpenStreetCamService;
import org.openstreetmap.josm.plugins.openstreetcam.util.Util;
import org.openstreetmap.josm.plugins.openstreetcam.util.cnf.GuiConfig;
import org.openstreetmap.josm.plugins.openstreetcam.util.pref.PreferenceManager;
import com.telenav.josm.common.argument.BoundingBox;


/**
 * Executes the service operations corresponding to user actions. If an operation fails, a corresponding message is
 * displayed to the user.
 *
 * @author Beata
 * @version $Revision$
 */
public final class ServiceHandler extends SearchServiceHandler {

    private static final ServiceHandler INSTANCE = new ServiceHandler();
    private final OpenStreetCamService openStreetCamService;
    private final ApolloService apolloService;


    private ServiceHandler() {
        openStreetCamService = new OpenStreetCamService();
        apolloService = new ApolloService();
    }

    public static ServiceHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public HighZoomResultSet searchHighZoomData(final BoundingBox area, final SearchFilter filter) {
        return filter != null && filter.getDataTypes() != null ? super.searchHighZoomData(area, filter)
                : new HighZoomResultSet();
    }


    /**
     * Retrieves the sequence identified by the given id.
     *
     * @param sequenceId the identifier of the sequence
     * @return a {@code Sequence} object
     */
    public Sequence retrieveSequence(final Long sequenceId) {
        final List<DataType> dataTypes = PreferenceManager.getInstance().loadSearchFilter().getDataTypes();
        final ExecutorService executorService = Executors.newFixedThreadPool(dataTypes.size());
        final Future<Sequence> sequenceFuture = dataTypes.contains(DataType.PHOTO)
                ? executorService.submit(() -> retrieveSequencePhotos(sequenceId)) : null;
                final Future<List<Detection>> detectionsFuture = dataTypes.contains(DataType.DETECTION)
                        ? executorService.submit(() -> retrieveSequenceDetections(sequenceId)) : null;
                        List<Photo> photos = null;
                        try {
                            if (sequenceFuture != null) {
                                final Sequence sequence = sequenceFuture.get();
                                photos = sequence != null ? sequence.getPhotos() : null;
                            }
                        } catch (final Exception ex) {
                            if (!PreferenceManager.getInstance().loadSequenceErrorSuppressFlag()) {
                                final boolean flag = handleException(GuiConfig.getInstance().getErrorSequenceText());
                                PreferenceManager.getInstance().saveSequenceErrorSuppressFlag(flag);
                            }
                        }
                        List<Detection> detections = null;
                        try {
                            detections = detectionsFuture != null ? detectionsFuture.get() : null;
                        } catch (final Exception ex) {
                            if (!PreferenceManager.getInstance().loadSequenceErrorSuppressFlag()) {
                                final boolean flag = handleException(GuiConfig.getInstance().getErrorSequenceText());
                                PreferenceManager.getInstance().saveSequenceErrorSuppressFlag(flag);
                            }
                        }
                        executorService.shutdown();
                        return new Sequence(sequenceId, photos, detections);
    }

    public Cluster retrieveClusterDetails(final Long id) {
        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        final Future<Cluster> clusterFuture = executorService.submit(() -> apolloService.retrieveCluster(id));
        final Future<List<Photo>> photosFuture = executorService.submit(() -> apolloService.retrieveClusterPhotos(id));
        final Future<List<Detection>> detectionsFuture =
                executorService.submit(() -> apolloService.retrieveClusterDetections(id));

        ClusterBuilder clusterBuilder = new ClusterBuilder();
        try {
            clusterBuilder = new ClusterBuilder(clusterFuture.get());
        } catch (final Exception ex) {
            if (!PreferenceManager.getInstance().loadSequenceErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorSequenceText());
                PreferenceManager.getInstance().saveSequenceErrorSuppressFlag(flag);
            }
        }

        try {
            clusterBuilder.photos(photosFuture.get());
        } catch (final Exception ex) {
            if (!PreferenceManager.getInstance().loadSequenceErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorSequenceText());
                PreferenceManager.getInstance().saveSequenceErrorSuppressFlag(flag);
            }
        }

        try {
            final List<Detection> detections = detectionsFuture.get();
            if (detections != null) {
                Collections.sort(detections, new Comparator<Detection>() {

                    @Override
                    public int compare(final Detection d1, final Detection d2) {
                        final Double sizeD2 = d2.getLocationOnPhoto().surface();
                        final Double sizeD1 = d1.getLocationOnPhoto().surface();
                        return sizeD2.compareTo(sizeD1);
                    }
                });
            }
            clusterBuilder.detections(detections);
        } catch (final Exception ex) {
            if (!PreferenceManager.getInstance().loadSequenceErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorSequenceText());
                PreferenceManager.getInstance().saveSequenceErrorSuppressFlag(flag);
            }
        }
        executorService.shutdown();
        return clusterBuilder.build();
    }


    private Sequence retrieveSequencePhotos(final Long id) {
        Sequence sequence = null;
        try {
            sequence = openStreetCamService.retrieveSequence(id);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadSequenceErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorSequenceText());
                PreferenceManager.getInstance().saveSequenceErrorSuppressFlag(flag);
            }
        }
        return sequence;
    }

    private List<Detection> retrieveSequenceDetections(final Long id) {
        List<Detection> result = null;
        try {
            result = apolloService.retrieveSequenceDetections(id);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadSequenceDetectionsErrorFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorDetectionRetrieveText());
                PreferenceManager.getInstance().saveSequenceDetectionsErrorFlag(flag);
            }
        }
        return result;
    }

    /**
     * Retrieve the list of detections corresponding to the given sequence.
     *
     * @param sequenceId the identifier of the sequence
     * @param sequenceIndex the photo index in the given sequence
     * @return a list of {@code Detection}s
     */
    public List<Detection> retrievePhotoDetections(final Long sequenceId, final Integer sequenceIndex) {
        List<Detection> result = null;
        try {
            result = apolloService.retrievePhotoDetections(sequenceId, sequenceIndex);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadSequenceDetectionsErrorFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorDetectionRetrieveText());
                PreferenceManager.getInstance().saveSequenceDetectionsErrorFlag(flag);
            }
        }
        return result;
    }

    /**
     * Lists the segments that have OpenStreetCam coverage from the given area(s) corresponding to the specified zoom
     * level.
     *
     * @param areas a list of {@code BoundingBox}s representing the search areas. If the OsmDataLayer is active, there
     * might be several bounding boxes.
     * @param filter a {@code Filter} represents the user's search filters. Null values are ignored.
     * @param zoom the current zoom level
     * @return a list of {@code Segment}s
     */
    public List<Segment> listMatchedTracks(final List<BoundingBox> areas, final SearchFilter filter, final int zoom) {
        List<Segment> finalResult = new ArrayList<>();
        final Long osmUserId = filter != null ? filter.getOsmUserId() : null;
        try {
            if (areas.size() > 1) {
                // special case: there are several different areas visible in the OSM data layer
                final ExecutorService executor = Executors.newFixedThreadPool(areas.size());
                final List<Future<List<Segment>>> futures = new ArrayList<>();
                for (final BoundingBox bbox : areas) {
                    final Callable<List<Segment>> callable =
                            () -> openStreetCamService.listMatchedTracks(bbox, osmUserId, zoom);
                            futures.add(executor.submit(callable));
                }
                finalResult.addAll(readResult(futures));
                executor.shutdown();
            } else {
                finalResult = openStreetCamService.listMatchedTracks(areas.get(0), osmUserId, zoom);
            }
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadSegmentsErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorSegmentListText());
                PreferenceManager.getInstance().saveSegmentsErrorSuppressFlag(flag);
            }
        }
        return finalResult;
    }

    /**
     * Retrieves the photo with the given name.
     *
     * @param photoName the name of a photo
     * @return the photo content in byte array format
     * @throws ServiceException if the download operation fails
     */
    byte[] retrievePhoto(final String photoName) throws ServiceException {
        return openStreetCamService.retrievePhoto(photoName);
    }

    /**
     * Retrieves details of the given photo.
     *
     * @param sequenceId the identifier of the sequence
     * @param sequenceIndex the photo index in the given sequence
     * @return a {@code Photo}
     */
    public Photo retrievePhotoDetails(final Long sequenceId, final Integer sequenceIndex) {
        Photo result = null;
        try {
            result = openStreetCamService.retrievePhotoDetails(sequenceId, sequenceIndex);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadPhotosErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorPhotoLoadingText());
                PreferenceManager.getInstance().savePhotoDetectionsErrorFlag(flag);
            }
        }
        return result;
    }

    /**
     * Updates the edit status of the given detection.
     *
     * @param detectionId the identifier of the detection that need to be updated
     * @param editStatus a new edit status
     * @param comment a descriptive comment for the update.
     */
    public void updateDetection(final Long detectionId, final EditStatus editStatus, final String comment) {
        final Long userId = Util.getOsmUserId();
        final String userName = UserIdentityManager.getInstance().getUserName();
        if (userId == null) {
            JOptionPane.showMessageDialog(MainApplication.getMap().mapView,
                    GuiConfig.getInstance().getAuthenticationNeededErrorMessage(),
                    GuiConfig.getInstance().getWarningTitle(), JOptionPane.WARNING_MESSAGE, null);
        } else {
            final Author author = new Author(userId, userName);
            try {
                apolloService.updateDetection(new Detection(detectionId, editStatus),
                        new Contribution(author, comment));
            } catch (final ServiceException e) {
                if (!PreferenceManager.getInstance().loadDetectionUpdateErrorSuppressFlag()) {
                    final boolean flag = handleException(GuiConfig.getInstance().getErrorDetectionUpdateText());
                    PreferenceManager.getInstance().saveDetectionUpdateErrorSuppressFlag(flag);
                }
            }
        }
    }


    /**
     * Retrieves the detection corresponding to the given identifier.
     *
     * @param detectionId the unique identifier of a detection
     * @return a {@code Detection}
     */
    public Detection retrieveDetection(final Long detectionId) {
        Detection result = null;
        try {
            result = apolloService.retrieveDetection(detectionId);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadPhotosErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorPhotoLoadingText());
                PreferenceManager.getInstance().savePhotoDetectionsErrorFlag(flag);
            }
        }
        return result;
    }

    private <T> Set<T> readResult(final List<Future<List<T>>> futures) throws ServiceException {
        final Set<T> result = new HashSet<>();
        for (final Future<List<T>> future : futures) {
            try {
                result.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new ServiceException(e);
            }
        }
        return result;
    }
}