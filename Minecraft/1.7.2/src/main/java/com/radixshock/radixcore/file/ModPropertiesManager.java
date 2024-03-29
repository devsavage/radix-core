/*******************************************************************************
 * ModPropertiesManager.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package com.radixshock.radixcore.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;

import com.radixshock.radixcore.core.IEnforcedCore;
import com.radixshock.radixcore.core.RadixCore;

/**
 * Handles reading and writing properties that effect how the entire mod
 * operates.
 */
public class ModPropertiesManager implements Serializable
{
	private transient IEnforcedCore		mod;
	private transient Properties		properties			= new Properties();
	private transient FileInputStream	inputStream			= null;
	private transient FileOutputStream	outputStream		= null;
	private transient File				modPropertiesFile	= null;
	private transient File				configFolder		= null;

	private transient Class				modPropertiesClass;

	/** An instance of the class containing mod properties. */
	public transient Object				modPropertiesInstance;

	/**
	 * Constructor
	 * 
	 * @param mod
	 *            This ModPropertiesManager's owner mod.
	 * @param modPropertiesClass
	 *            The class containing the mod's properties.
	 */
	public ModPropertiesManager(IEnforcedCore mod, Class modPropertiesClass)
	{
		this.mod = mod;
		this.modPropertiesClass = modPropertiesClass;

		try
		{
			modPropertiesInstance = modPropertiesClass.newInstance();
		}

		catch (final Exception e)
		{
			e.printStackTrace();
		}

		// Assign the location of the mod properties file and config folder.
		configFolder = new File(RadixCore.getInstance().runningDirectory + "/config/" + mod.getShortModName() + "/");
		modPropertiesFile = new File(RadixCore.getInstance().runningDirectory + "/config/" + mod.getShortModName() + "/ModProps.properties");

		// Ensure the config folder exists.
		if (!configFolder.exists())
		{
			configFolder.mkdirs();
		}

		// Now check if the mod properties file must be created or should be
		// loaded.
		if (!modPropertiesFile.exists())
		{
			mod.getLogger().log("File not found: " + RadixCore.getInstance().runningDirectory + "/config/" + mod.getShortModName() + "/ModProps.properties. " + "Creating new mod properties file...");
			saveModProperties();
		}

		else
		{
			loadModProperties();
		}
	}

	/**
	 * Saves the current mod properties to file.
	 */
	public void saveModProperties()
	{
		try
		{
			// Clear the properties instance to avoid saving unwanted variables.
			properties.clear();

			// Use reflection to get all the fields in this class. Only work
			// with the ones whose name is prefixed with setting_.
			for (final Field f : modPropertiesClass.getFields())
			{
				final String fieldType = f.getType().toString();

				if (fieldType.contains("int"))
				{
					properties.put(f.getName(), f.get(modPropertiesInstance).toString());
				}

				else if (fieldType.contains("boolean"))
				{
					properties.put(f.getName(), f.get(modPropertiesInstance).toString());
				}

				else if (fieldType.contains("String"))
				{
					properties.put(f.getName(), f.get(modPropertiesInstance).toString());
				}
			}

			// Store information in the properties instance to file.
			outputStream = new FileOutputStream(modPropertiesFile);
			properties.store(outputStream, mod.getShortModName() + " Mod Properties File - Change global mod settings here.");
			outputStream.close();

			mod.getLogger().log("Mod properties successfully saved.");
		}

		catch (final FileNotFoundException e)
		{
			RadixCore.getInstance().quitWithException("FileNotFoundException occurred while creating a new mod properties file.", e);
		}

		catch (final IllegalAccessException e)
		{
			RadixCore.getInstance().quitWithException("IllegalAccessException occurred while creating a new mod properties file.", e);
		}

		catch (final IOException e)
		{
			RadixCore.getInstance().quitWithException("IOException occurred while creating a new mod properties file.", e);
		}
	}

	/**
	 * Loads each value from the mod properties file into memory.
	 */
	public void loadModProperties()
	{
		mod.getLogger().log("Loading mod properties...");

		try
		{
			// Clear the properties instance and get the mod's properties file.
			properties.clear();

			// Make sure the file exists.
			if (modPropertiesFile.exists())
			{
				// Load its properties into the properties instance.
				inputStream = new FileInputStream(modPropertiesFile);
				properties.load(inputStream);
				inputStream.close();

				// Loop through each field and assign the value stored in the
				// properties.
				for (final Field f : modPropertiesClass.getFields())
				{
					final String fieldType = f.getType().toString();

					if (fieldType.contains("int"))
					{
						f.set(modPropertiesInstance, Integer.parseInt(properties.getProperty(f.getName())));
					}

					else if (fieldType.contains("boolean"))
					{
						f.set(modPropertiesInstance, Boolean.parseBoolean(properties.getProperty(f.getName())));
					}

					else if (fieldType.contains("String"))
					{
						f.set(modPropertiesInstance, properties.getProperty(f.getName()));
					}
				}
			}

			else
			// The mod properties file does not exist. It was either deleted by
			// the user or hasn't been created yet.
			{
				mod.getLogger().log("Mod properties file was not found.");
				saveModProperties();
			}
		}

		// The user didn't edit the file correctly or assigned an invalid ID for
		// an item or block. A new property could have also been added.
		catch (final NumberFormatException e)
		{
			mod.getLogger().log("NumberFormatException while reading mod properties. You edited the file incorrectly or a new property has been added to " + mod.getShortModName() + ".");
			resetModProperties();
			saveModProperties();
		}

		catch (final FileNotFoundException e)
		{
			RadixCore.getInstance().quitWithException("FileNotFoundException occurred while loading the mod properties file.", e);
		}

		catch (final IllegalAccessException e)
		{
			RadixCore.getInstance().quitWithException("IllegalAccessException occurred while loading the new mod properties file.", e);
		}

		catch (final IOException e)
		{
			RadixCore.getInstance().quitWithException("IOException occurred while loading the new mod properties file.", e);
		}
	}

	/** Resets all mod properties back to their default values. */
	public void resetModProperties()
	{
		try
		{
			modPropertiesInstance = modPropertiesClass.newInstance();
		}

		catch (final InstantiationException e)
		{
			e.printStackTrace();
		}

		catch (final IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
}
