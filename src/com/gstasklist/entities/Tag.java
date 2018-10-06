package com.gstasklist.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Color;

public class Tag implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private int foregroundColor = Color.WHITE;
	private int backgroundColor = Color.BLACK;
	
	private static final String DELIMITER = "###";
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getForegroundColor() {
		return foregroundColor;
	}
	public void setForegroundColor(int foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	public int getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public Tag(String name, int foregroundColor, int backgroundColor) {
		this.name = name;
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
	}
	
	public Tag(String name) {
		this.name = name;
	}
	
	public String toString(){
		return name+DELIMITER+foregroundColor+DELIMITER+backgroundColor;
	}
	
	public static Tag parseTag(String tagString){
		String[] fields = tagString.split(DELIMITER);
		return new Tag(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
	}
	
	public static ArrayList<Tag> parseTagSet(Set<String> tagStrings){
        ArrayList<Tag> tags = new ArrayList<Tag>(); 
        for(String tagString: tagStrings){
        	tags.add(Tag.parseTag(tagString));
        }
        
        return tags;  
	}
	
	public static HashSet<String> toStringSet(List<Tag> tagList){
		HashSet<String> tagStrings = new HashSet<String>();
		for (Tag tag : tagList){
			tagStrings.add(tag.toString());
		}
		return tagStrings;
	}
	
}
