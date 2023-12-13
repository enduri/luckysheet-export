# luckysheet-export
* https://github.com/enduri/luckysheet-export
* java export lucksheet data
# export luckysheet version
* luckysheet version: 2.1.13
* https://github.com/mengshukeji/Luckysheet
# Key features:
* Exporting lucysheet is based on version 2.1.13 data
* Contains watermark and built-in font to avoid text watermark that cannot be displayed due to no font on the server
* The image is downloaded and converted to the embedded base64 encoded image
* Support merging tables, background colors, etc.
# issue
* Image positioning, at present, the understanding of POI and Lucysheet image positioning is not particularly clear, and there are problems with image positioning and scaling.
* Without much compatibility testing, there may be some bugs
# Usage
Maven reference
```
       <dependency>
            <groupId>io.github.enduri</groupId>
            <artifactId>luckysheet-export</artifactId>
            <version>1.0.0</version>
        </dependency>
```
usage
```
import abka.enduri.luckysheet.ExcelUtils;

String exportDir = "export_dir";
String exportFileName = "/demo_with_border.xlsx";
String excelData = loadFileData("demo_with_border.json");
String [] watermark = new String[]{"water1","water2"};

ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);
```