/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.report.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sigmah.server.dao.BaseMapDAO;
import org.sigmah.server.dao.PivotDAO;
import org.sigmah.server.report.generator.map.BubbleLayerGenerator;
import org.sigmah.server.report.generator.map.IconLayerGenerator;
import org.sigmah.server.report.generator.map.LayerGenerator;
import org.sigmah.server.report.generator.map.Margins;
import org.sigmah.server.report.generator.map.PiechartLayerGenerator;
import org.sigmah.server.report.generator.map.TiledMap;
import org.sigmah.shared.dao.Filter;
import org.sigmah.shared.dao.IndicatorDAO;
import org.sigmah.shared.dao.SiteTableDAO;
import org.sigmah.shared.domain.Indicator;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.dto.IndicatorDTO;
import org.sigmah.shared.map.BaseMap;
import org.sigmah.shared.map.GoogleBaseMap;
import org.sigmah.shared.map.PredefinedBaseMaps;
import org.sigmah.shared.map.TileBaseMap;
import org.sigmah.shared.report.content.LatLng;
import org.sigmah.shared.report.content.MapContent;
import org.sigmah.shared.report.content.MapMarker;
import org.sigmah.shared.report.model.DateRange;
import org.sigmah.shared.report.model.DimensionType;
import org.sigmah.shared.report.model.MapReportElement;
import org.sigmah.shared.report.model.SiteData;
import org.sigmah.shared.report.model.layers.BubbleMapLayer;
import org.sigmah.shared.report.model.layers.IconMapLayer;
import org.sigmah.shared.report.model.layers.MapLayer;
import org.sigmah.shared.report.model.layers.PiechartMapLayer;
import org.sigmah.shared.util.mapping.Extents;
import org.sigmah.shared.util.mapping.TileMath;

import com.google.inject.Inject;

/**
 * @author Alex Bertram
 */
public class MapGenerator extends ListGenerator<MapReportElement> {

	private final BaseMapDAO baseMapDAO;
	private final IndicatorDAO indicatorDAO;

    private static final Logger logger = Logger.getLogger(MapGenerator.class);
    
    @Inject
    public MapGenerator(PivotDAO pivotDAO, SiteTableDAO siteDAO, BaseMapDAO baseMapDAO, IndicatorDAO indicatorDAO) {
        super(pivotDAO, siteDAO);
        this.baseMapDAO = baseMapDAO;
        this.indicatorDAO = indicatorDAO;
    }

    public void generate(User user, MapReportElement element, Filter inheritedFilter, DateRange dateRange) {

        Filter filter = resolveElementFilter(element, dateRange);
        Filter effectiveFilter = inheritedFilter == null ? filter : new Filter(inheritedFilter, filter);

        List<SiteData> sites = siteDAO.query(
                user,
                effectiveFilter,
                null,
                new SiteDataBinder(),
                SiteTableDAO.RETRIEVE_ALL, 0, -1);

        MapContent content = new MapContent();
        content.setFilterDescriptions(generateFilterDescriptions(filter, Collections.<DimensionType>emptySet(), user));

        // Set up layer generators
        List<LayerGenerator> layerGenerators = new ArrayList<LayerGenerator>();
        for (MapLayer layer : element.getLayers()) {
        	if (layer.isVisible()) {
	            if (layer instanceof BubbleMapLayer) {
	                layerGenerators.add(new BubbleLayerGenerator(element, (BubbleMapLayer) layer));
	            } else if (layer instanceof IconMapLayer) {
	                layerGenerators.add(new IconLayerGenerator(element, (IconMapLayer) layer));
	            } else if (layer instanceof PiechartMapLayer) {
	            	layerGenerators.add(new PiechartLayerGenerator(element, (PiechartMapLayer) layer));
	            }
        	}
        }

        // FIRST PASS: calculate extents and margins
        Extents extents = Extents.emptyExtents();
        Margins margins = new Margins(0);
        for (LayerGenerator layerGtor : layerGenerators) {
            extents.grow(layerGtor.calculateExtents(sites));
            margins.grow(layerGtor.calculateMargins());
        }

        // Now we're ready to calculate the zoom level
        // and the projection
        int width = element.getWidth();
        int height = element.getHeight();
        int zoom = element.getZoomLevel() == -1 ? TileMath.zoomLevelForExtents(extents, width, height) : element.getZoomLevel();
        LatLng center = element.getCenter() == null ? extents.center() : element.getCenter();

        // Retrieve the basemap and clamp zoom level
        BaseMap baseMap = null;
        if (element.getBaseMapId() == null) {
        	// default
        	baseMap = GoogleBaseMap.ROADMAP;
        } else if(PredefinedBaseMaps.isPredefinedMap(element.getBaseMapId())) {
        	baseMap = PredefinedBaseMaps.forId(element.getBaseMapId());
        } else {
        	baseMap = baseMapDAO.getBaseMap(element.getBaseMapId());
            if (baseMap == null) {
            	baseMap = TileBaseMap.createNullMap(element.getBaseMapId());
    			logger.error("Could not find base map id=" + element.getBaseMapId());
            }
        } 
                
        if (zoom < baseMap.getMinZoom()) {
            zoom = baseMap.getMinZoom();
        }
        if (zoom > baseMap.getMaxZoom()) {
            zoom = baseMap.getMaxZoom();
        }

        TiledMap map = new TiledMap(width, height, center, zoom);
        content.setBaseMap(baseMap);
        content.setZoomLevel(zoom);
        if (baseMap == null) {
        	baseMap = TileBaseMap.createNullMap(element.getBaseMapId());
			logger.error("Could not find base map id=" + element.getBaseMapId());
        }
        content.setExtents(extents);

        // Generate the actual content
        for (LayerGenerator layerGtor : layerGenerators) {
            layerGtor.generate(sites, map, content);
        }
        
        // Get relevant indicators for the map markers
        Set<Integer> indicatorIds = new HashSet<Integer>(); 
        
        for (MapLayer maplayer : element.getLayers()) {
        	indicatorIds.addAll(maplayer.getIndicatorIds());
        }
        
        Set<IndicatorDTO> indicatorDTOs = new HashSet<IndicatorDTO>();
        for (Integer indicatorId : indicatorIds) {
        	Indicator indicator = indicatorDAO.findById(indicatorId);
        	IndicatorDTO indicatorDTO = new IndicatorDTO();
        	indicatorDTO.setId(indicator.getId());
        	indicatorDTO.setName(indicator.getName());
        	
        	indicatorDTOs.add(indicatorDTO);
        }
        
        content.setIndicators(indicatorDTOs);

        // sort order by symbol radius descending
        // (this assures that smaller symbols are drawn on
        // top of larger ones)
        Collections.sort(content.getMarkers(), new Comparator<MapMarker>() {
            public int compare(MapMarker o1, MapMarker o2) {
                if (o1.getSize() > o2.getSize()) {
                    return -1;
                } else if (o1.getSize() < o2.getSize()) {
                    return 1;
                }
                return 0;
            }
        });

        element.setContent(content);

    }
}
