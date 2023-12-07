package abka.enduri.luckysheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * luckysheet
 * version: 2.1.13
 * https://github.com/mengshukeji/Luckysheet
 */
@NoArgsConstructor
@Data
public class LuckySheetExcelJsonData {

    @JsonProperty("name")
    private String name;
    @JsonProperty("color")
    private String color;
    @JsonProperty("index")
    private String index;
    @JsonProperty("status")
    private Integer status;
    @JsonProperty("order")
    private Integer order;
    @JsonProperty("hide")
    private Integer hide;
    @JsonProperty("row")
    private Integer row;
    @JsonProperty("column")
    private Integer column;
    @JsonProperty("defaultRowHeight")
    private Integer defaultRowHeight;
    @JsonProperty("defaultColWidth")
    private Integer defaultColWidth;
    @JsonProperty("celldata")
    private List<?> celldata;
    @JsonProperty("config")
    private ConfigDTO config;
    @JsonProperty("scrollLeft")
    private Integer scrollLeft;
    @JsonProperty("scrollTop")
    private Integer scrollTop;
    @JsonProperty("isPivotTable")
    private Boolean isPivotTable;
    @JsonProperty("filter")
    private Object filter;
    @JsonProperty("zoomRatio")
    private Integer zoomRatio;
    @JsonProperty("showGridLines")
    private Integer showGridLines;
    @JsonProperty("jfgird_select_save")
    private List<?> jfgirdSelectSave;
    @JsonProperty("luckysheet_select_save")
    private List<LuckysheetSelectSaveDTO> luckysheetSelectSave;
    @JsonProperty("data")
    private List<List<DataDTO>> data;
    @JsonProperty("load")
    private String load;
    @JsonProperty("visibledatarow")
    private List<Integer> visibledatarow;
    @JsonProperty("visibledatacolumn")
    private List<Integer> visibledatacolumn;
    @JsonProperty("ch_width")
    private Integer chWidth;
    @JsonProperty("rh_height")
    private Integer rhHeight;
    @JsonProperty("luckysheet_selection_range")
    private List<?> luckysheetSelectionRange;
    @JsonProperty("images")
    private Map<String,ImagesDTO> images;

    @JsonProperty("hyperlink")
    private Map<String,HyperlinkDTO> hyperlink;
    @NoArgsConstructor
    @Data
    public static class HyperlinkDTO {

        @JsonProperty("linkType")
        private String linkType;
        @JsonProperty("linkAddress")
        private String linkAddress;
        @JsonProperty("linkTooltip")
        private String linkTooltip;
    }
    @NoArgsConstructor
    @Data
    public static class ConfigDTO {
        @JsonProperty("borderInfo")
        private List<BorderInfoDTO> borderInfo;
        @JsonProperty("merge")
        private Map<String,MergeDTO> merge;
        @JsonProperty("columnlen")
        private Map<String,Integer> columnlen;
        @JsonProperty("customWidth")
        private Map<String,Integer> customWidth;
        @JsonProperty("rowlen")
        private Map<String, BigDecimal> rowlen;
        @JsonProperty("customHeight")
        private Map<String, Integer> customHeight;

        @NoArgsConstructor
        @Data
        public static class MergeDTO {
            @JsonProperty("r")
            private Integer r;
            @JsonProperty("c")
            private Integer c;
            @JsonProperty("rs")
            private Integer rs;
            @JsonProperty("cs")
            private Integer cs;
        }

        @NoArgsConstructor
        @Data
        public static class BorderInfoDTO {
            /**
             * range
             */
            @JsonProperty("rangeType")
            private String rangeType;
            /**
             * border-all
             */
            @JsonProperty("borderType")
            private String borderType;
            @JsonProperty("color")
            private String color;
            @JsonProperty("style")
            private String style;

            @JsonProperty("range")
            private List<RangeDTO> range;
            @NoArgsConstructor
            @Data
            public static class RangeDTO {
                @JsonProperty("left")
                private Integer left;
                @JsonProperty("width")
                private Integer width;
                @JsonProperty("top")
                private Integer top;
                @JsonProperty("height")
                private Integer height;
                @JsonProperty("left_move")
                private Integer leftMove;
                @JsonProperty("width_move")
                private Integer widthMove;
                @JsonProperty("top_move")
                private Integer topMove;
                @JsonProperty("height_move")
                private Integer heightMove;
                @JsonProperty("row")
                private List<Integer> row;
                @JsonProperty("column")
                private List<Integer> column;
                @JsonProperty("row_focus")
                private Integer rowFocus;
                @JsonProperty("column_focus")
                private Integer columnFocus;
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class ImagesDTO {

        @JsonProperty("type")
        private String type;
        @JsonProperty("src")
        private String src;
        @JsonProperty("originWidth")
        private Integer originWidth;
        @JsonProperty("originHeight")
        private Integer originHeight;
        @JsonProperty("default")
        private DefaultDTO defaultX;
        @JsonProperty("crop")
        private CropDTO crop;
        @JsonProperty("isFixedPos")
        private Boolean isFixedPos;
        @JsonProperty("fixedLeft")
        private Integer fixedLeft;
        @JsonProperty("fixedTop")
        private Integer fixedTop;
        @JsonProperty("border")
        private BorderDTO border;

        @NoArgsConstructor
        @Data
        public static class DefaultDTO {
            @JsonProperty("width")
            private Integer width;
            @JsonProperty("height")
            private Integer height;
            @JsonProperty("left")
            private Integer left;
            @JsonProperty("top")
            private Integer top;
        }

        @NoArgsConstructor
        @Data
        public static class CropDTO {
            @JsonProperty("width")
            private Integer width;
            @JsonProperty("height")
            private Integer height;
            @JsonProperty("offsetLeft")
            private Integer offsetLeft;
            @JsonProperty("offsetTop")
            private Integer offsetTop;
        }

        @NoArgsConstructor
        @Data
        public static class BorderDTO {
            @JsonProperty("width")
            private Integer width;
            @JsonProperty("radius")
            private Integer radius;
            @JsonProperty("style")
            private String style;
            @JsonProperty("color")
            private String color;
        }
    }

    @NoArgsConstructor
    @Data
    public static class LuckysheetSelectSaveDTO {
        @JsonProperty("left")
        private Integer left;
        @JsonProperty("width")
        private Integer width;
        @JsonProperty("top")
        private Integer top;
        @JsonProperty("height")
        private Integer height;
        @JsonProperty("left_move")
        private Integer leftMove;
        @JsonProperty("width_move")
        private Integer widthMove;
        @JsonProperty("top_move")
        private Integer topMove;
        @JsonProperty("height_move")
        private Integer heightMove;
        @JsonProperty("row")
        private List<Integer> row;
        @JsonProperty("column")
        private List<Integer> column;
        @JsonProperty("row_focus")
        private Integer rowFocus;
        @JsonProperty("column_focus")
        private Integer columnFocus;
    }

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("v")
        private String v;
        @JsonProperty("ff")
        private String ff;

        @JsonProperty("fs")
        private String fs;

        @JsonProperty("ct")
        private CtDTO ct;
        // font color, eg:#00ffff
        @JsonProperty("fc")
        private String fc;
        @JsonProperty("bg")
        // background color :eg :#ff0000
        private String bg;
        @JsonProperty("m")
        private String m;

        private Integer bl;

        @JsonProperty("it")
        private Integer it;
        @JsonProperty("cl")
        private Integer cl;
        /**
         * eg: inlineStr
         */
        private String t;



        @NoArgsConstructor
        @Data
        public static class SDTO{

            @JsonProperty("ff")
            private Object ff;
            @JsonProperty("fc")
            private String fc;
            @JsonProperty("fs")
            private Integer fs;
            @JsonProperty("cl")
            private Integer cl;
            @JsonProperty("un")
            private Integer un;
            @JsonProperty("bl")
            private Integer bl;
            @JsonProperty("it")
            private Integer it;
            @JsonProperty("v")
            private String v;
        }

        @NoArgsConstructor
        @Data
        public static class CtDTO {
            @JsonProperty("fa")
            private String fa;
            @JsonProperty("t")
            private String t;
            private List<SDTO> s;

        }
    }
}
