/**
 * Copyright 2016 mojo Friedrich Schiller University Jena
 * 
 * This file is part of mojo.
 * 
 * mojo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * mojo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with mojo. If not, see <http://www.gnu.org/licenses/>.
 */
package de.jena.uni.mojo.command;

/**
 * A command is a parameter which can be used to modify the behavior of the Mojo
 * analyses.
 * 
 * @author Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class Command {

	/**
	 * The name of the command.
	 */
	private final String name;

	/**
	 * The command that should be used as argument.
	 */
	private final String command;

	/**
	 * The short command that could be used as argument.
	 */
	private final String shortCommand;

	/**
	 * A short description of the command.
	 */
	private final String description;

	/**
	 * Whether this command is a flag or not.
	 */
	private final boolean flag;

	/**
	 * The type of the value of this command.
	 */
	private final Class<?> valueType;

	/**
	 * The value of the command.
	 */
	private Object value;

	/**
	 * The constructor that defines a new command.
	 * 
	 * @param name
	 *            The name of the command.
	 * @param command
	 *            The command that is used as argument.
	 * @param shortCommand
	 *            The short command that is used as argument.
	 * @param description
	 *            The description of the command.
	 * @param flag
	 *            Determines whether this command is a flag or not.
	 * @param valueType
	 *            The type of the command.
	 * @param defValue
	 */
	public Command(String name, String command, String shortCommand,
			String description, boolean flag, Class<?> valueType,
			Object defValue) {
		this.name = name;
		this.command = command;
		this.shortCommand = shortCommand;
		this.description = description;
		this.flag = flag;
		this.valueType = valueType;
		this.value = defValue;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return the shortCommand
	 */
	public String getShortCommand() {
		return shortCommand;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the flag
	 */
	public boolean isFlag() {
		return flag;
	}

	/**
	 * @return the valueType
	 */
	public Class<?> getValueType() {
		return valueType;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Get the value as boolean value.
	 * 
	 * @return The value of the boolean command.
	 */
	public boolean asBooleanValue() {
		return (Boolean) this.value;
	}

	/**
	 * Get the value as string value.
	 * 
	 * @return The value of the string command.
	 */
	public String asStringValue() {
		return (String) this.value;
	}

	/**
	 * Get the value as integer value.
	 * 
	 * @return The value of the integer command.
	 */
	public int asIntegerValue() {
		return (Integer) this.value;
	}

}
