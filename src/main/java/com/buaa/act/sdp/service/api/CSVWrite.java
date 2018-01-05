package com.buaa.act.sdp.service.api;

import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by YLT on 2017/6/26.
 */
public class CSVWrite {
    public static void writeToCSV(List<String[]> allRelation, String path) {
        CsvWriter wr = new CsvWriter(path, ',', Charset.forName("GBK"));
        int num = 0;
        for (int i = 0; i < allRelation.size(); i++) {
            try {
                wr.writeRecord(allRelation.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wr.flush();
        wr.close();
    }
}
