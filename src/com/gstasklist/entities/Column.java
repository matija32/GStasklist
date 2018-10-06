package com.gstasklist.entities;

public class Column {

	private String name;
	private String type;

	public Column(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	
	public static class ColumnType{
		public static final String DEADLINE = "Deadline";
		public static final String TASK = "Task";
		public static final String COMMENTS = "Comments";
		public static final String STATUS = "Status";
	}
}
