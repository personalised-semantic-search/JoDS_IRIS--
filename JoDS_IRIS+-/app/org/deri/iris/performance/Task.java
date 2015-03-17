/*
 * <Project Name>
 * <Project Description>
 * 
 * Copyright (C) 2010 ICT Institute - Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.performance;

/**
 * Models a performance task
 */
public class Task implements Comparable<Object>{
	private int number;
	private String name;
	private float time;
	private float initTime;
	private float finalTime;
	private String timeUnit;
	private String taskDetails;

	/**
	 * Instantiates a new task.
	 *
	 * @param number the task ID
	 * @param name the task name
	 * @param time the absolute time
	 * @param timeUnit the time unit
	 * @param details the task details
	 */
	public Task(int number, String name, float time, float initTime, float finalTime, String timeUnit, String taskDetails) {
		this.number = number;
		this.name = name;
		this.time = time;
		this.timeUnit = timeUnit;
		this.initTime = initTime;
		this.finalTime = finalTime;
		this.taskDetails = taskDetails;
	}
	
	/**
	 * Instantiates a new task.
	 *
	 * @param number the task ID
	 * @param name the task name
	 * @param time the absolute time
	 * @param timeUnit the time unit
	 */
	public Task(int number, String name, float time, float initTime, float finalTime, String timeUnit) {
		this.number = number;
		this.name = name;
		this.time = time;
		this.timeUnit = timeUnit;
		this.initTime = initTime;
		this.finalTime = finalTime;
	}

	public float getInitTime() {
		return initTime;
	}

	public void setInitTime(float initTime) {
		this.initTime = initTime;
	}

	public float getFinalTime() {
		return finalTime;
	}

	public void setFinalTime(float finalTime) {
		this.finalTime = finalTime;
	}

	/**
	 * Gets the number.
	 *
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the number.
	 *
	 * @param number the new number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public float getTime() {
		return time;
	}

	/**
	 * Sets the time.
	 *
	 * @param time the new time
	 */
	public void setTime(float time) {
		this.time = time;
	}

	/**
	 * Gets the time unit.
	 *
	 * @return the time unit
	 */
	public String getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Sets the time unit.
	 *
	 * @param timeUnit the new time unit
	 */
	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	/**
	 * @return the task details
	 */
	public String getTaskDetails() {
		return taskDetails;
	}

	/**
	 * Set a detailed explanantion for this task
	 * @param taskDetails
	 */
	public void setTaskDetails(String taskDetails) {
		this.taskDetails = taskDetails;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object that) {
		return this.number - ((Task)that).getNumber();
	}

	@Override
	public String toString() {
		return "Task [number = " + number + ", name = " + name + ", time = " + time
				+ ", initTime = " + initTime + ", finalTime = " + finalTime
				+ ", timeUnit = " + timeUnit + ", details = " + getTaskDetails() + "]";
	}
}
