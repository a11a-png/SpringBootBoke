package com.wjg.vueboke.config;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.util.List;
import java.util.Map;

public class higlioghtField {

    public Map<String,Object> field(SearchHit Hit, List<String> str) {
        // 获取高亮内容
        Map<String, HighlightField> highlightField = Hit.getHighlightFields();
        // 原查询内容
        Map<String, Object> sourceAsMap=Hit.getSourceAsMap();
        for (String s : str) {
            HighlightField title = highlightField.get(s);
            //解析高亮的字段，将原来的字段换为我们高亮的字段既可
            if (title != null) {
                Text[] fragment = title.fragments();
                String n_title = "";
                for (Text text : fragment) {
                    n_title += text;
                }
                //覆盖title文本,设置为高亮
                sourceAsMap.put(s, n_title);
            }
        }
        //设置高亮
        return sourceAsMap;
    }
}
