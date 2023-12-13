package abka.enduri.luckysheet;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*;

import java.awt.Color;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

/**
 * luckysheet
 * version: 2.1.13
 * https://github.com/mengshukeji/Luckysheet
 */
@Slf4j
public class ExcelUtils {

    /***
     * 基于POI解析 从0开始导出xlsx文件，不是基于模板
     * @param exportDir 保存的文件夹名
     * @param exportFileName 保存的文件名
     * @param excelData luckysheet 表格数据
     */
    public static void exportLuckySheetXlsxByPOI(String exportDir, String exportFileName, String excelData, String[] watermark) throws IOException {
        log.info("exportLuckySheetXlsxByPOI: exportDir:{}, exportFileName:{}, watermark:{}", exportDir, exportFileName, watermark);
        File dir = new File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(exportDir + exportFileName);

            exportLuckySheetXlsxByPOI(excelData, out, watermark);

            out.close();

        } catch (FileNotFoundException e) {

            log.warn("FileNotFoundException", e);
            throw e;
        } catch (IOException e) {

            log.warn("IOException", e);
            throw e;
        } finally {
            if (out != null) {
                IOUtils.closeQuietly(out);
            }
        }
    }


    /***
     * 基于POI解析 从0开始导出xlsx文件，不是基于模板
     */
    public static void exportLuckySheetXlsxByPOI(String excelData, OutputStream out, String[] watermark) throws IOException {
        if (ObjectUtils.isEmpty(excelData)) {
            return;
        }
        excelData = excelData.replace("&#xA;", "\\r\\n"); //去除luckysheet中 &#xA 的换行

        //创建操作Excel的XSSFWorkbook对象
        XSSFWorkbook excel = new XSSFWorkbook();

        List<LuckySheetExcelJsonData> luckySheetExcelJsonData = null;
        try {
            luckySheetExcelJsonData = JsonUtil.toList(excelData, LuckySheetExcelJsonData.class);

        } catch (Exception ex) {
            log.warn("exportLuckySheetXlsxByPOI parse error. excelData:{}", excelData);
            return;
        }

        if (ObjectUtils.isEmpty(luckySheetExcelJsonData)) {
            return;
        }

        for (int sheetIndex = 0; sheetIndex < luckySheetExcelJsonData.size(); sheetIndex++) {
            LuckySheetExcelJsonData jsonObject = luckySheetExcelJsonData.get(sheetIndex);
            List<?> celldataObjectList = jsonObject.getCelldata();
            List<Integer> rowObjectList = jsonObject.getVisibledatarow();
            List<Integer> colObjectList = jsonObject.getVisibledatacolumn();
            List<List<LuckySheetExcelJsonData.DataDTO>> dataObjectList = jsonObject.getData();
            var config = jsonObject.getConfig();
            var mergeObject = config.getMerge();  //合并单元格

            var columnlenObject = config.getColumnlen();  //表格列宽
            var rowlenObject = config.getRowlen(); //表格行高
            var borderInfoObjectList = config.getBorderInfo(); //边框样式
            var images = jsonObject.getImages();
            //参考：https://blog.csdn.net/jdtugfcg/article/details/84100315
            XSSFCellStyle cellStyle = excel.createCellStyle();
            XSSFSheet sheet = excel.createSheet(jsonObject.getName());

            //创建行和列
            if (!ObjectUtils.isEmpty(rowObjectList)) {
                for (int i = 0; i < rowObjectList.size(); i++) {
                    XSSFRow row = sheet.createRow(i);//创建行

                    if (rowlenObject != null && rowlenObject.get(String.valueOf(i)) != null) {
                        try {
                            row.setHeightInPoints(Float.parseFloat(rowlenObject.get(String.valueOf(i)) + "")); //行高px值
                        } catch (Exception e) {
                            log.warn("setHeightInPoints error. rowlenObject.get(String.valueOf(i)):{}", rowlenObject.get(String.valueOf(i)));
                            row.setHeightInPoints(20f);//默认行高
                        }
                    } else {
                        row.setHeightInPoints(20f);//默认行高
                    }


                    for (int j = 0; j < colObjectList.size(); j++) {
                        if (columnlenObject != null && columnlenObject.get(j + "") != null) {
                            sheet.setColumnWidth(j, Math.min(columnlenObject.get(j + "") * 42, 65280)); //列宽px值

                        }
                        row.createCell(j);//创建列
                    }
                }
            }

            Map<String, XSSFCellStyle> rowColCell = new HashMap<>();
            setCellValue(celldataObjectList, sheet, excel, dataObjectList, rowColCell);
            //合并单元格与填充单元格颜色
            setMergeAndColorByObject(mergeObject, sheet);
            //设置边框
            setBorder(borderInfoObjectList, excel, sheet, rowColCell);
            setImages(images, sheet, excel, columnlenObject, rowlenObject);
            setHyperlink(jsonObject.getHyperlink(), excel, sheet);
        }


        FontImage.addWatermarkToExcel(excel, watermark);
        try {
            excel.write(out);
        } catch (IOException e) {
            log.warn("IOException", e);
            throw e;
        }
    }

    private static void setHyperlink(Map<String, LuckySheetExcelJsonData.HyperlinkDTO> hyperlink, XSSFWorkbook excel, XSSFSheet sheet) {
        if (hyperlink == null) {
            return;
        }


        for (var entry : hyperlink.entrySet()) {
            var rowCol = entry.getKey();
            var hyperlinkValue = entry.getValue();

            if (hyperlinkValue == null) {
                continue;
            }
            var linkType = hyperlinkValue.getLinkType();

            if (!"external".equalsIgnoreCase(linkType)) {
                continue;
            }
            int[] rowColArr = parseRowCol(rowCol, "_");
            if (ObjectUtils.isEmpty(rowColArr)) {
                continue;
            }
            var row = rowColArr[0];
            var col = rowColArr[1];
            var rowOfSheet = sheet.getRow(row);
            if (rowOfSheet == null) {
                rowOfSheet = sheet.createRow(row);
            }
            var cell = rowOfSheet.getCell(col);


            var link = excel.getCreationHelper().createHyperlink(HyperlinkType.URL);
            link.setAddress(hyperlinkValue.getLinkAddress());
            cell.setHyperlink(link);

        }
    }

    private static int[] parseRowCol(String rowcol, String split) {
        String[] arr = rowcol.split(split);
        if (arr.length <= 0 || arr.length != 2) {
            return null;
        }
        return new int[]{Integer.parseInt(arr[0]), Integer.parseInt(arr[1])};

    }

    private static String getRowColKey(int row, int col) {
        return row + "_" + col;
    }

    private static void setImages(Map<String, LuckySheetExcelJsonData.ImagesDTO> images, XSSFSheet sheet, XSSFWorkbook excel, Map<String, Integer> columnlenObject, Map<String, BigDecimal> rowlenObject) {
        if (ObjectUtils.isEmpty(images) || ObjectUtils.isEmpty(images.entrySet())) {
            return;
        }
        for (Map.Entry<String, LuckySheetExcelJsonData.ImagesDTO> imageEntry : images.entrySet()) {

            String imageKey = imageEntry.getKey();
            var imageObject = imageEntry.getValue();
            if (imageObject == null) {
                continue;
            }

            String imgType = imageObject.getType();

            String imgSrc = imageObject.getSrc();

            Integer originWidth = imageObject.getOriginWidth();
            Integer originHeight = imageObject.getOriginHeight();

            int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;


            int width = 0;
            int height = 0;
            double scaleX = 1;
            double scaleY = 1;
            if (imageObject.getDefaultX() != null) {
                Integer defaultWidth = imageObject.getDefaultX().getWidth();
                Integer defaultHeight = imageObject.getDefaultX().getHeight();
                Integer defaultLeft = imageObject.getDefaultX().getLeft();

                Integer defaultTop = imageObject.getDefaultX().getTop();


                width = Optional.ofNullable(defaultWidth).orElse(0);
                height = Optional.ofNullable(defaultHeight).orElse(0);
                dx1 = Optional.ofNullable(defaultLeft).orElse(0);
                dy1 = Optional.ofNullable(defaultTop).orElse(0);
                dx2 = width + dx1;
                dy2 = height + dy1;

                if (defaultWidth > 0 && defaultWidth > 0) {
                    scaleX = defaultWidth * 1.0 / originWidth;
                }

                if (defaultHeight > 0 && originHeight > 0) {
                    scaleY = defaultHeight * 1.0 / originHeight;
                }
            }


            if (imageObject.getCrop() != null) {
                Integer cropWidth = imageObject.getCrop().getWidth();
                Integer cropHeight = imageObject.getCrop().getHeight();
                Integer cropOffsetLeft = imageObject.getCrop().getOffsetLeft();

                Integer cropOffsetTop = imageObject.getCrop().getOffsetTop();


                width = Optional.ofNullable(cropWidth).orElse(width);
                height = Optional.ofNullable(cropHeight).orElse(height);

                dx1 = cropOffsetLeft > 0 ? cropOffsetLeft : dx1;
                dy1 = cropOffsetTop > 0 ? cropOffsetTop : dy1;
                dx2 = width + dx1;
                dy2 = height + dy1;

                if (width > 0 && cropWidth > 0) {
                    scaleX = width * 1.0 / originWidth;
                }

                if (height > 0 && originHeight > 0) {
                    scaleY = height * 1.0 / originHeight;
                }
            }
            Boolean isFixedPos = imageObject.getIsFixedPos();
            Integer fixedLeft = imageObject.getFixedLeft();
            Integer fixedTop = imageObject.getFixedTop();

            fixedLeft = Optional.ofNullable(fixedLeft).orElse(0);

            fixedTop = Optional.ofNullable(fixedTop).orElse(0);
            dx1 += fixedLeft;
            dx2 += fixedLeft;

            dy1 += fixedTop;
            dy2 += fixedTop;
            if (imageObject.getBorder() != null) {
                Integer borderRadius = imageObject.getBorder().getRadius();
                String borderStyle = imageObject.getBorder().getStyle();
                String borderColor = imageObject.getBorder().getColor();
            }


            // 图片的Base64编码，你需要替换为实际的Base64编码字符串
            String base64Image = ImgUtil.tryTransImgToBase64WithDataPrefix(imgSrc);
            // 去掉base64前缀 data:image/jpeg;base64,
            base64Image = base64Image.substring(base64Image.indexOf(",", 1) + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            int pictureIdx = excel.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

            int col1 = calCol(100, dx1);
            int col2 = calCol(100, dx2);

            int row1 = calCol(28, dy1);
            int row2 = calCol(28, dy2);

            if (col2 <= col1) {
                col2 = col1;
            }

            if (row2 <= row1) {
                row2 = row1;
            }

            addPictureByXy(excel, sheet, imageBytes, dx1, dy1, dx2, dy2, row1, col1, row2, col2, scaleX, scaleY);
        }
    }

    private static int calCol(int x, int dx1) {
        int col = 0;
        if (col * x < dx1) {
            for (int i = 0; i < 1000; i++) {
                col++;

                if (col * x > dx1) {
                    break;
                }
            }
        }
        return col;
    }


    public static void addPictureByXy(XSSFWorkbook workbook, XSSFSheet sheet, byte[] imgBytes, int dx1, int dy1, int dx2, int dy2,
                                      int row1, int col1,
                                      int row2, int col2,
                                      double scaleX, double scaleY) {
        int pictureIdx = workbook.addPicture(imgBytes, workbook.PICTURE_TYPE_PNG);
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        // 图片插入坐标
        anchor.setDx1(dx1 * Units.EMU_PER_POINT);
        anchor.setDy1(dy1 * Units.EMU_PER_POINT);
        anchor.setDx2(dx2 * Units.EMU_PER_POINT);
        anchor.setDy2(dy2 * Units.EMU_PER_POINT);
        anchor.setRow1(row1);
        anchor.setCol1(col1);
        anchor.setRow2(row2);
        anchor.setCol2(col2);
        // 插入图片
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        if (scaleX > 0 && scaleY > 0) {
            pict.resize(scaleX, scaleY);
        } else {
            log.info("scaleX: {} && scaleY: {}", scaleX, scaleY);
        }

    }

    private static void setMergeAndColorByObject(Map<String, LuckySheetExcelJsonData.ConfigDTO.MergeDTO> mergeObject, XSSFSheet sheet) {

        if (mergeObject == null) {
            return;
        }

        for (var mergeDTO : mergeObject.values()) {
            if (mergeDTO == null) {
                return;
            }

            var r = mergeDTO.getR();
            var c = mergeDTO.getC();
            var rs = mergeDTO.getRs();
            var cs = mergeDTO.getCs();

            if (r != null && c != null && rs != null && cs != null) {
                CellRangeAddress region = new CellRangeAddress(r, (r + rs - 1), c, (c + cs - 1));
                sheet.addMergedRegion(region);
            }

        }
    }

    private static void setBorder(List<LuckySheetExcelJsonData.ConfigDTO.BorderInfoDTO> borderInfoObjectList, XSSFWorkbook workbook, XSSFSheet sheet, Map<String, XSSFCellStyle> rowColCell) {
        if (ObjectUtils.isEmpty(borderInfoObjectList)) {
            return;
        }

        //设置边框样式map
        Map<String, BorderStyle> bordMap = new HashMap<>();
        bordMap.put("1", BorderStyle.THIN);
        bordMap.put("2", BorderStyle.HAIR);
        bordMap.put("3", BorderStyle.DOTTED);
        bordMap.put("4", BorderStyle.DASHED);
        bordMap.put("5", BorderStyle.DASH_DOT);
        bordMap.put("6", BorderStyle.DASH_DOT_DOT);
        bordMap.put("7", BorderStyle.DOUBLE);
        bordMap.put("8", BorderStyle.MEDIUM);
        bordMap.put("9", BorderStyle.MEDIUM_DASHED);
        bordMap.put("10", BorderStyle.MEDIUM_DASH_DOT);
        bordMap.put("11", BorderStyle.MEDIUM_DASH_DOT_DOT);
//        bordMap.put("11", BorderStyle.MEDIUM_DASH_DOT_DOTC);
        bordMap.put("12", BorderStyle.SLANTED_DASH_DOT);
        bordMap.put("13", BorderStyle.THICK);

        //一定要通过 cell.getCellStyle()  不然的话之前设置的样式会丢失
        //设置边框
        for (int i = 0; i < borderInfoObjectList.size(); i++) {
            var borderInfoObject = borderInfoObjectList.get(i);
            //            if (borderInfoObject.get("rangeType").equals("cell")) {//单个单元格
            if ("cell".equalsIgnoreCase(borderInfoObject.getRangeType())) {//单个单元格
                // TODO
            } else if ("range".equalsIgnoreCase(borderInfoObject.getRangeType())) {//选区
                Color borderColor = toColor(borderInfoObject.getColor());
                String style_ = borderInfoObject.getStyle(); // borderInfoObject.getInteger("style");
                BorderStyle styleOfBorder = bordMap.get(style_);

                var rangObject = borderInfoObject.getRange().get(0);

                var rowList = rangObject.getRow(); // rangObject.getJSONArray("row");
                var columnList = rangObject.getColumn(); // rangObject.getJSONArray("column");

                var cellStyle = workbook.createCellStyle();

                cellStyle.setBorderBottom(styleOfBorder);
                cellStyle.setBorderLeft(styleOfBorder);
                cellStyle.setBorderRight(styleOfBorder);
                cellStyle.setBorderTop(styleOfBorder);

                cellStyle.setBottomBorderColor((short) borderColor.getRGB());
                cellStyle.setLeftBorderColor((short) borderColor.getRGB());
                cellStyle.setRightBorderColor((short) borderColor.getRGB());
                cellStyle.setTopBorderColor((short) borderColor.getRGB());

                //设置样式
                for (int row_ = rowList.get(0); row_ <= rowList.get(rowList.size() - 1); row_++) {
                    bord(sheet.getRow(row_), columnList.get(0), columnList.get(columnList.size() - 1), styleOfBorder, borderColor, cellStyle, row_, rowColCell);

                }
            }
        }
    }

    /**
     * 设置样式
     *
     * @param row
     * @param j          列数，从0开始数
     * @param row_
     * @param rowColCell
     */

    public static void bord(Row row, int i, int j, BorderStyle styleOfBorder, Color borderColor, CellStyle cellStyle_, int row_, Map<String, XSSFCellStyle> rowColCell) {
        for (; i <= j; i++) {
            var cell = row.getCell(i);

            CellStyle cellStyle = rowColCell.get(getRowColKey(row_, i));
            if (cellStyle == null) {
                cellStyle = cellStyle_;
            } else {
                cellStyle.setBorderBottom(styleOfBorder);
                cellStyle.setBorderLeft(styleOfBorder);
                cellStyle.setBorderRight(styleOfBorder);
                cellStyle.setBorderTop(styleOfBorder);

                cellStyle.setBottomBorderColor((short) borderColor.getRGB());
                cellStyle.setLeftBorderColor((short) borderColor.getRGB());
                cellStyle.setRightBorderColor((short) borderColor.getRGB());
                cellStyle.setTopBorderColor((short) borderColor.getRGB());
            }
            cell.setCellStyle(cellStyle);

        }
    }

    static void setCellValue(List<?> celldataObjectList, XSSFSheet
            sheet, XSSFWorkbook workbook, List<List<LuckySheetExcelJsonData.DataDTO>> dataObjectList, Map<String, XSSFCellStyle> rowColCell) {
//        //设置字体大小和颜色
//        Map<Integer, String> fontMap = new HashMap<>();
//        fontMap.put(-1, "Arial");
//        fontMap.put(0, "Times New Roman");
//        fontMap.put(1, "Arial");
//        fontMap.put(2, "Tahoma");
//        fontMap.put(3, "Verdana");
//        fontMap.put(4, "微软雅黑");
//        fontMap.put(5, "宋体");
//        fontMap.put(6, "黑体");
//        fontMap.put(7, "楷体");
//        fontMap.put(8, "仿宋");
//        fontMap.put(9, "新宋体");
//        fontMap.put(10, "华文新魏");
//        fontMap.put(11, "华文行楷");
//        fontMap.put(12, "华文隶书");

        for (int rowIndex = 0; rowIndex < dataObjectList.size(); rowIndex++) {
            var jsonRow = dataObjectList.get(rowIndex);

            for (int columnIndex = 0; columnIndex < jsonRow.size(); columnIndex++) {
                var cellJsonObj = jsonRow.get(columnIndex);
                if (ObjectUtils.isEmpty(cellJsonObj)) {
                    continue;
                }
                var ct = cellJsonObj.getCt();
                var v = cellJsonObj.getV();
                var m = cellJsonObj.getM();
                var fc = cellJsonObj.getFc();
                var bg = cellJsonObj.getBg();
                var row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                XSSFCell xssfCell = sheet.getRow(rowIndex).getCell(columnIndex);

                if (xssfCell == null) {
                    xssfCell = row.createCell(columnIndex);
                }
                //填充值
                xssfCell.setCellValue(v);


                String ff = cellJsonObj.getFf();
                int it = toInteger(cellJsonObj.getIt(), 0);
                int fs = toInteger(cellJsonObj.getFs(), 0);
                int bl = toInteger(cellJsonObj.getBl(), 0);
                int cl = toInteger(cellJsonObj.getCl(), 0);
                XSSFFont font = workbook.createFont();//字体样式
                XSSFCellStyle style = workbook.createCellStyle();//样式
                DataFormat poiFormat = workbook.createDataFormat();

                if (ct != null) {
                    // ct = ct.getJSONObject("key");
                    if (ct != null) {
                        var fa = ct.getFa();
                        var t = ct.getT();
                        // g

                        if ("d".equalsIgnoreCase(t)) {
                            /**
                             *  "fa": "yyyy-MM-dd",
                             *  "t": "d"
                             */
//                            v = m;

//                            CreationHelper createHelper = workbook.getCreationHelper();
//                            short format = createHelper.createDataFormat().getFormat(fa);
//                            style.setDataFormat(format);

//                            style.setDataFormat(poiFormat.getFormat(fa));
                            style.setDataFormat(poiFormat.getFormat(fa));
                            v = m;
                        } else if ("n".equalsIgnoreCase(t)) {
                            /**
                             * {
                             *     "ct": {
                             *         "fa": "##0.00",
                             *         "t": "n"
                             *     },
                             *     "m": "22.23",
                             *     "v": 22.2322
                             * }
                             */
                            if ("w".equalsIgnoreCase(fa)) {
                                style.setDataFormat(poiFormat.getFormat("#\"万元\""));
                            } else if ("w0.00".equalsIgnoreCase(fa)) {
                                style.setDataFormat(poiFormat.getFormat("0.00\"万元\""));
                            } else {
                                style.setDataFormat(poiFormat.getFormat(fa));
                            }

//                            v = m;
                        }
                    }
                }
//                if(!ObjectUtils.isEmpty(cellJsonObj.getS())){
//                    var firstS = cellJsonObj.getS().get(0);
//
//                    fc = firstS.getFc();
//                    var ffInS = firstS.getFf();
//                    it = toInteger(firstS.getIt(), it);
//                    fs = toInteger(firstS.getFs(), fs);
//                    bl = toInteger(firstS.getBl(), bl);
//                    cl = toInteger(firstS.getCl(), cl);
////                    bg = firstS.getBg();
//                    v = firstS.getV();
//                }

                if (cellJsonObj.getCt() != null && !ObjectUtils.isEmpty(cellJsonObj.getCt().getS())) {
                    var firstS = cellJsonObj.getCt().getS().get(0);

                    fc = firstS.getFc();
                    var ffInS = firstS.getFf();
                    it = toInteger(firstS.getIt(), it);
                    fs = toInteger(firstS.getFs(), fs);
                    bl = toInteger(firstS.getBl(), bl);
                    cl = toInteger(firstS.getCl(), cl);
//                    bg = firstS.getBg();
                    v = firstS.getV();
                }


                xssfCell.setCellValue(v);

                if (!ObjectUtils.isEmpty(fc)) {

                    font.setColor(toXSSFColor(fc));
                }
                if (!ObjectUtils.isEmpty(ff)) {
                    font.setFontName(ff);//字体名字
                }

                if (fs > 0) {
                    font.setFontHeightInPoints((short) fs);//字体大小

                }
                if (bl == 1) {
                    font.setBold(true);
                }
                font.setItalic(it == 1 ? true : false);//斜体
                if (cl == 1) {
                    font.setStrikeout(true);
                }

                style.setFont(font);
                style.setWrapText(true);//设置自动换行
                if (!ObjectUtils.isEmpty(bg)) {
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    style.setFillForegroundColor(toXSSFColor(bg));
                }

                rowColCell.put(getRowColKey(rowIndex, columnIndex), style);
                xssfCell.setCellStyle(style);


            }
        }
    }

    private static int toInteger(Integer it, int defaultValue) {
        return Optional.ofNullable(it).orElse(defaultValue);
    }

    private static Integer toInteger(String v, int defaultValue) {
        return Optional.ofNullable(v).map(o -> Integer.parseInt(o)).orElse(defaultValue);
    }

    private static XSSFColor toXSSFColor(String fc) {
        return new XSSFColor(toColor(fc), null);
    }

    private static Color toColor(String fc) {


        if (fc.contains("rgb")) {
            RGBA rgba = rgbStrToIntArr(fc);

            return new Color(rgba.r, rgba.g, rgba.b, rgba.a);
        }
        var bg = Integer.parseInt(fc.replace("#", ""), 16);
        return new Color(bg);
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RGBA {
        int r;
        int g;
        int b;
        // 0-255
        int a;
    }

    private static RGBA rgbStrToIntArr(String fc) {

        if (fc.toLowerCase().contains("rgba")) {
            var rgba = fc.replace("rgba", "").replace("(", "").replace(")", "");
            String[] rgbaArr = rgba.split(",");

            if (rgbaArr.length == 4) {
                var intValue = new BigDecimal(rgbaArr[3].trim()).multiply(BigDecimal.valueOf(255)).intValue();
                return RGBA.builder()
                        .r(Integer.parseInt(rgbaArr[0].trim()))
                        .g(Integer.parseInt(rgbaArr[1].trim()))
                        .b(Integer.parseInt(rgbaArr[2].trim()))
                        .a(intValue)
                        .build();
            }
        }

        var rgb = fc.replace("rgb", "").replace("(", "").replace(")", "");
        String[] rgbArr = rgb.split(",");
        if (rgbArr.length == 3) {
            return RGBA.builder()
                    .r(Integer.parseInt(rgbArr[0].trim()))
                    .g(Integer.parseInt(rgbArr[1].trim()))
                    .b(Integer.parseInt(rgbArr[2].trim()))
                    .a(255)
                    .build();
        }
        log.warn("rgb parse error.fc:{}", fc);
        return RGBA.builder().r(255).g(255).b(255).a(255).build();
    }
}