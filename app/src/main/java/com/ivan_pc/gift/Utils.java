package com.ivan_pc.gift;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Ivan-PC on 25.01.2017.
 */

public class Utils {
    static String readStringFromFile(File file) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(file));
        StringBuilder ret = new StringBuilder();
        String cur;
        while ((cur = bf.readLine()) != null) {
            ret.append(cur);
        }
        return ret.toString();
    }

    static RunnableTaskDownloader initDownloader(File to, String from) {
        return new RunnableTaskDownloader(to.toString(), 0, false, null, null);
    }

    static void downloadFile(File to, String from) throws LoadException {
        RunnableTaskDownloader downloader = new RunnableTaskDownloader(to.toString(), 0, false, null, null);
        downloader.downloadToFile(to, from);
    }
}
