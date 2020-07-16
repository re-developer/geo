package kr.macadamia.geo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShapeFileReaderMain {

  public static void main(String... s) throws IOException {

    ShpFileHandler shpFileHandler = ShpFileHandler.getInstance();

    File dir = new File("C:\\tmps\\shpfiles");

    List<File> shpFiles = Stream.of(dir.listFiles())
        .filter(x -> x.getName().indexOf(".shp") == x.getName().length() - 4)
        .collect(
            Collectors.toList());
    for (File shpFile : shpFiles) {
      shpFileHandler.shpToTextFile(
          shpFile.getAbsolutePath(),
          dir.getAbsolutePath() + File.separator + shpFile.getName().replaceAll(".shp", ".sql"),
          "insert into m7a_legaldong_latlng(legaldong_cd, lat, lng)values('%s',%4.16f,%4.16f);\n"
      );
    }


  }
}

