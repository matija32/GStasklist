package com.gstasklist.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.gstasklist.entities.Task;

public class TaskDeadlineComparator implements Comparator<Task> {

	@Override
	public int compare(Task task1, Task task2) {
		
		if (task1.hasInfiniteDeadline()) {
			if (task2.hasInfiniteDeadline()) {
				return 0;
			}
			else {
				return 1;
			}
		}
		else if (task2.hasInfiniteDeadline()) 
		{
			return -1;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		Date date1, date2;
		try {
			date1 = dateFormat.parse(task1.getDeadline());
			date2 = dateFormat.parse(task2.getDeadline());
			return date1.compareTo(date2);
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}		
	}

}
