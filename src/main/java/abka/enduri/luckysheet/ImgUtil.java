package abka.enduri.luckysheet;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Slf4j
public class ImgUtil {

    public enum TypeImg {
        TYPE_JPG("jpeg", "FFD8FF"),
        TYPE_GIF("gif", "47494638"),
        TYPE_PNG("png", "89504E47"),
        TYPE_BMP("bmp", "424D"),
        TYPE_WEBP("webp", "52494646"),
        TYPE_TIF("tif", "49492A00"),
        ;

        final String code;
        final String fileHeader;

        TypeImg(String code, String fileHeader) {
            this.code = code;
            this.fileHeader = fileHeader;
        }

        public static TypeImg getByFileHeader(String fileHeader) {
            if (fileHeader == null) {
                return null;
            }
            for (TypeImg typeImg : TypeImg.values()) {
                if (typeImg.fileHeader.equalsIgnoreCase(fileHeader)) {
                    return typeImg;
                }
            }
            return null;
        }
    }

    /**
     * 将网络链接图片或者本地图片文件转换成Base64编码字符串
     *
     * @param imgSrc 网络图片Url/本地图片目录路径
     * @return
     */
    public static String tryTransImgToBase64WithDataPrefix(String imgSrc) {

        if(ObjectUtils.isEmpty(imgSrc)){
            return imgSrc;
        }

        if(imgSrc.startsWith("data")){
            return imgSrc;
        }
        String imgType = TypeImg.TYPE_JPG.code;


        Result result = getResult(imgSrc, imgType);
        if (!result.success){
            return imgSrc;
        }
        log.info("imageType:{}, src:{}", result.imgType, imgSrc );
        // 对字节数组Base64编码
        return "data:image/"+ result.imgType +";base64,"+ Base64.getEncoder().encodeToString(result.buffer);
    }

    static Result getResult(String imgStr, String imgType) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        byte[] bufferRead = null;

        boolean success = true;
        for(int i=0;i< 5;i++){
            try {
                //判断网络链接图片文件/本地目录图片文件
                if (imgStr.startsWith("http://") || imgStr.startsWith("https://")) {
                    // 创建URL
                    URL url = new URL(imgStr);
                    // 创建链接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0");
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);

                    inputStream = conn.getInputStream();
                    outputStream = new ByteArrayOutputStream();
                    // 将内容读取内存中
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    boolean first = true;

                    while ((len = inputStream.read(buffer)) != -1) {
                        if(first){
                            first = false;
                            imgType = ImgUtil.getPicType(buffer);
                        }
                        outputStream.write(buffer, 0, len);
                    }
                    bufferRead = outputStream.toByteArray();
                } else {
                    inputStream = new FileInputStream(imgStr);
                    int count = 0;
                    while (count == 0) {
                        count = inputStream.available();
                    }
                    bufferRead = new byte[count];
                    imgType = ImgUtil.getPicType(bufferRead);
                    inputStream.read(bufferRead);
                }
            } catch (Exception e) {
                log.warn("{}",e);
                success = false;
            } finally {
                if (inputStream != null) {
                    try {
                        // 关闭inputStream流
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        // 关闭outputStream流
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(success){
                break;
            }
        }

        if (bufferRead == null){
            success = false;
        }
        return new Result(imgType, bufferRead, success);
    }

    static class Result {
        public final String imgType;
        public final byte[] buffer;

        private final boolean success;

        public Result(String imgType, byte[] buffer,boolean success) {
            this.imgType = imgType;
            this.buffer = buffer;
            this.success = success;
        }
    }

    /**
     * byte数组转换成16进制字符串*
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 判断图片类型
     *
     * @param bytes
     * @return
     */
    public static String getPicType(byte[] bytes) {
        if (ObjectUtils.isEmpty(bytes) || bytes.length < 4) {
            return TypeImg.TYPE_JPG.code;
        }
        //读取文件的前几个字节来判断图片格式
        byte[] b = new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]};

        String type = bytesToHexString(b).toUpperCase();
        log.info("type:{}", type);
        TypeImg typeImg = TypeImg.getByFileHeader(type);
        if (typeImg == null) {
            return TypeImg.TYPE_JPG.code;
        }
        return typeImg.code;
    }
}
