package com.gstasklist.entities;

import java.io.Serializable;

public class TaskServerStatus implements Serializable {

	private TaskServerAction action = TaskServerAction.NO_ACTION;	
	private boolean isCommitted = true;

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}
	
	public TaskServerAction getAction() {
		return action;
	}

	public void setAction(TaskServerAction action) {
		this.action = action;
	}


	public TaskServerStatus(boolean isCommitted, TaskServerAction action) {
		super();
		this.isCommitted = isCommitted;
		this.action = action;
	}
	
	public TaskServerStatus(){
		
	}
}
