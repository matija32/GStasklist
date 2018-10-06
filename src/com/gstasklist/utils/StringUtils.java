package com.gstasklist.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;



public class StringUtils {

    private static int getTagPosition(String source, String tag){
        int tagPosition = source.indexOf(tag);

        if (tagPosition == -1){
            throw new IllegalArgumentException(tag);
        }

        return tagPosition;
    }

    public static String extractValueBetweenTags(String source, String startTag, String endTag){
        int startTagPosition = getTagPosition(source, startTag);
        int endTagPosition = getTagPosition(source, endTag);

        return source.substring(startTagPosition + startTag.length(), endTagPosition);
    }

    public static String substringUntilEnd(String source, String startTag){

        int startTagPosition = getTagPosition(source, startTag);
        return source.substring(startTagPosition + startTag.length());
    }

    public static String substringFromBegining(String source, String endTag){

        int endTagPosition = getTagPosition(source, endTag);
        return source.substring(0, endTagPosition);
    }

    // two utility functionS from alchemy api
    public static String getFileContents(String filename)

    throws IOException, FileNotFoundException
    {
    File file = new File(filename);
    StringBuilder contents = new StringBuilder();

    BufferedReader input = new BufferedReader(new FileReader(file));

    try {
        String line = null;

        while ((line = input.readLine()) != null) {
            contents.append(line);
            contents.append(System.getProperty("line.separator"));
        }
    } finally {
        input.close();
    }

    return contents.toString();
    }

}
