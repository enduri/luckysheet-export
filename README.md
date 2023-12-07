# luckysheet-export
java export lucksheet data
https://github.com/enduri/luckysheet-export

# export luckysheet version
* version: 2.1.13
* https://github.com/mengshukeji/Luckysheet

# 主要功能
* 导出 lucysheet 基于 2.1.13 版本数据
* 包含水印，内置字体，避免服务器上没有字体导致显示不出来文字水印
* 图片下载转为内嵌base64 编码格式的图片
* 支持合并表格，背景颜色等。

# 问题
* 图片定位，目前对poi和lucysheet图片定位了解不是特别清楚，图片定位，缩放有问题。
* 未作过多兼容测试，可能会有一些bug

# 使用方法
```
String exportDir = "export_dir";
String exportFileName = "/demo_with_border.xlsx";
String excelData = loadFileData("demo_with_border.json");
String [] watermark = new String[]{"water1","water2"};

ExcelUtils.exportLuckySheetXlsxByPOI(exportDir, exportFileName,excelData, watermark);
```
