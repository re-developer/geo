package kr.macadamia.geo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.ProjCoordinate;

final class ShpFileHandler {

  private static final ShpFileHandler shpFileHandler = new ShpFileHandler();
  private static final String URL = "url";
  private static final String WSG_80 = "EPSG:5179";
  private static final String WSG_84 = "EPSG:4326";

  private ShpFileHandler(){

  }

  public static ShpFileHandler getInstance(){
    return shpFileHandler;
  }

  void writeFile(final String path, final String text) throws IOException {
    File file = new File(path);
    FileUtils.writeStringToFile(file, text);
  }

  File readShpFile(final String path) {

    File file;
    if (path != null && path.toLowerCase().indexOf(".shp") == path.length() - 4) {
      file = new File(path);
    } else {
      throw new RuntimeException("only shp file!!");
    }
    return file;
  }

  void shpToTextFile(final String srcPath, final String distPath, final String template)
      throws IOException {
    File file = readShpFile(srcPath);

    Map<String, Object> map = new HashMap<>();
    map.put(URL, file.toURI().toURL());

    DataStore dataStore = DataStoreFinder.getDataStore(map);
    String typeName = dataStore.getTypeNames()[0];

    FeatureSource<SimpleFeatureType, SimpleFeature> source =
        dataStore.getFeatureSource(typeName);
    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

    try (FeatureIterator<SimpleFeature> features = collection.features()) {
      StringBuilder builder = new StringBuilder();

      while (features.hasNext()) {
        SimpleFeature feature = features.next();
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        String emdCd = (String) feature.getAttribute("EMD_CD");

        if (geometry != null) {

          CRSFactory factory = new CRSFactory();


          for (Coordinate readCoordinate : geometry.getCoordinates()) {
            BasicCoordinateTransform transform = new BasicCoordinateTransform(
                factory.createFromName(WSG_80),
                factory.createFromName(WSG_84)
            );

            ProjCoordinate coordinate = new ProjCoordinate();
            transform.transform(new ProjCoordinate(readCoordinate.getX(), readCoordinate.getY()),
                coordinate);
            builder.append(String.format(template, emdCd, coordinate.y, coordinate.x));
          }
        }
        writeFile(distPath, builder.toString());
      }
    } finally {
      dataStore.dispose();
    }
  }

}
