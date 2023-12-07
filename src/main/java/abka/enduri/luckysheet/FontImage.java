package abka.enduri.luckysheet;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class FontImage {

    @Data
    public static class Watermark {
        private Boolean enable;
        private String text;
        private String dateFormat;
        private String color;
    }

    public static BufferedImage createWatermarkImage( String ... text) {
        StringBuilder sb = new StringBuilder();
        for(String s : text){
            sb.append(s).append("\n");
        }
        if (sb.lastIndexOf("\n") == sb.length() -1){
            sb.delete(sb.length()-1, sb.length());
        }
        return createWatermarkImage(null, sb.toString());
    }

    public static void addWatermarkToExcel(XSSFWorkbook workbook, String[] text) throws IOException {
        BufferedImage image = FontImage.createWatermarkImage(text);
        // 导出到字节流B
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);

        int pictureIdx = workbook.addPicture(os.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG);
        XSSFPictureData poixmlDocumentPart = workbook.getAllPictures().get(pictureIdx);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {//获取每个Sheet表
            XSSFSheet sheet = workbook.getSheetAt(i);
            PackagePartName ppn = poixmlDocumentPart.getPackagePart().getPartName();
            String relType = XSSFRelation.IMAGES.getRelation();
            //add relation from sheet to the picture data
            PackageRelationship pr = sheet.getPackagePart().addRelationship(ppn, TargetMode.INTERNAL, relType, null);
            //set background picture to sheet
            sheet.getCTWorksheet().addNewPicture().setId(pr.getId());
        }
    }

    public static Font loadStyleFont(int style, float fontSize) {
        try{

            var in = FontImage.class.getClassLoader().getResourceAsStream("SourceHanSansSC-VF.ttf");

            Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, in);
            Font dynamicFontPt =  dynamicFont.deriveFont(style,fontSize);
            in.close();
            return dynamicFontPt;
        }catch(Exception e) {
            log.error("load font error. ", e);
            return new Font("宋体", Font.PLAIN, 20);
        }
    }
    public static BufferedImage createWatermarkImage( Watermark watermark, String text) {

        if (watermark == null) {
            watermark = new Watermark();
            watermark.setEnable(true);
            watermark.setText(text);
            watermark.setColor("#C5CBCF");
            watermark.setDateFormat("yyyy-MM-dd HH:mm");
        }
        String[] textArray = watermark.getText().split("\n");
        Font font = loadStyleFont(Font.PLAIN,20);
        Integer width = 300;
        Integer height = 100;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 背景透明 开始
        Graphics2D g = image.createGraphics();
        image = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g.dispose();
        // 背景透明 结束
        g = image.createGraphics();
        g.setColor(new Color(Integer.parseInt(watermark.getColor().substring(1), 16)));// 设定画笔颜色
        g.setFont(font);// 设置画笔字体
        g.shear(0.1, -0.26);// 设定倾斜度

//        设置字体平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 50;
        for (int i = 0; i < textArray.length; i++) {
            g.drawString(textArray[i], 0, y);// 画出字符串
            y = y + font.getSize();
        }
        g.dispose();// 释放画笔
        return image;

    }
}
