package abka.enduri.luckysheet;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


class ExcelUtilsTest {

    @Test
    void exportLuckySheetXlsxByPOI() throws IOException, URISyntaxException {
        String exportDir = "export_dir";
        String exportFileName = "/demo_with_border.xlsx";
        String excelData = loadFileData("demo_with_border.json");
        String [] watermark = new String[]{"water1","water2"};

        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

        exportFileName = "/demo_with_border2.xlsx";
        excelData = loadFileData("demo_with_border2.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

        exportFileName = "/demo_with_border3.xlsx";
        excelData = loadFileData("demo_with_border3.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

        exportFileName = "/demo_with_border4.xlsx";
        excelData = loadFileData("demo_with_border4.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

        exportFileName = "/demo_with_border5.xlsx";
        excelData = loadFileData("demo_with_border5.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

        exportFileName = "/demo_with_border6.xlsx";
        excelData = loadFileData("demo_with_border6.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);


        exportFileName = "/demo_with_border7.xlsx";
        excelData = loadFileData("demo_with_border7.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

        exportFileName = "/demo_with_border8.xlsx";
        excelData = loadFileData("demo_with_border8.json");
        ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);

    }


    String loadFileData(String path) throws URISyntaxException, IOException {
        return Files.readString(Paths.get(ExcelUtilsTest.class.getClassLoader().getResource(path).toURI()), StandardCharsets.UTF_8);

    }
}