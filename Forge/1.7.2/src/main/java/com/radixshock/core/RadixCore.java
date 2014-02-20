package com.radixshock.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.common.MinecraftForge;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.radixshock.network.AbstractPacketCodec;
import com.radixshock.network.AbstractPacketHandler;
import com.radixshock.network.PacketPipeline;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid="radixcore", name="RadixCore", version="1.0.0")
public class RadixCore implements IMod
{
	@Instance("radixcore")
	private static RadixCore instance;
	private ModLogger logger;

	public String runningDirectory;
	public static final List<IMod> registeredMods = new ArrayList<IMod>();

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		instance = this;
		logger = new ModLogger(this);
		runningDirectory = System.getProperty("user.dir");

		logger.log("RadixCore version " + getVersion() + " is running from " + runningDirectory);

		try
		{
			FMLCommonHandler.instance().bus().register(new RadixEvents());
			MinecraftForge.EVENT_BUS.register(new RadixEvents());
			
			for (IMod mod : registeredMods)
			{
				getLogger().log("Pre-initializing " + mod.getLongModName() + "...");

				mod.preInit();
				mod.initializeProxy();
				mod.initializeItems();
				mod.initializeBlocks();

				FMLCommonHandler.instance().bus().register(mod.getEventHookClass().newInstance());
				MinecraftForge.EVENT_BUS.register(mod.getEventHookClass().newInstance());
			}
		}

		catch (Exception e)
		{
			quitWithException("Exception while registering event hook class.", e);
		}
	}

	@EventHandler
	public void onInit(FMLInitializationEvent event)
	{
		for (IMod mod : registeredMods)
		{
			getLogger().log("Initializing " + mod.getLongModName() + "...");

			mod.init();
			mod.initializeRecipes();
			mod.initializeSmeltings();
			mod.initializeAchievements();
			mod.initializeEntities();
			mod.initializeNetwork();
		}
	}

	@EventHandler
	public void onPostInit(FMLPostInitializationEvent event)
	{
		for (IMod mod : registeredMods)
		{
			getLogger().log("Post-initializing " + mod.getLongModName() + "...");

			mod.postInit();
		}
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		for (IMod mod : registeredMods)
		{
			mod.serverStarting(event);
		}
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event)
	{
		for (IMod mod : registeredMods)
		{
			mod.serverStopping(event);
		}
	}

	public static RadixCore getInstance()
	{
		return instance;
	}

	/**
	 * Stops the game and writes the error to the Forge crash log.
	 * 
	 * @param 	description	A string providing a short description of the problem.
	 * @param 	e			The exception that caused this method to be called.
	 */
	@SideOnly(Side.CLIENT)
	public void quitWithDescription(String description)
	{
		final Writer stackTrace = new StringWriter();
		final Exception exception = new Exception();

		PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
		exception.printStackTrace(stackTraceWriter);

		logger.log(Level.FINER, "Radix Core: An exception occurred.\n>>>>>" + description + "<<<<<\n" + stackTrace.toString());
		System.out.println("Radix Core: An exception occurred.\n>>>>>" + description + "<<<<<\n" + stackTrace.toString());

		final CrashReport crashReport = new CrashReport("RADIX CORE: " + description, exception);
		Minecraft.getMinecraft().crashed(crashReport);
		Minecraft.getMinecraft().displayCrashReport(crashReport);
	}

	/**
	 * Stops the game and writes the error to the Forge crash log.
	 * 
	 * @param 	description	A string providing a short description of the problem.
	 * @param 	exception	The exception that caused this method to be called.
	 */
	@SideOnly(Side.CLIENT)
	public void quitWithException(String description, Exception exception)
	{
		final Writer stackTrace = new StringWriter();
		final PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
		exception.printStackTrace(stackTraceWriter);

		logger.log(Level.FINER, "Radix Core: An exception occurred.\n>>>>>" + description + "<<<<<\n" + stackTrace.toString());
		System.out.println("Radix Core: An exception occurred.\n>>>>>" + description + "<<<<<\n" + stackTrace.toString());

		final CrashReport crashReport = new CrashReport("RADIX CORE: " + description, exception);
		Minecraft.getMinecraft().crashed(crashReport);
		Minecraft.getMinecraft().displayCrashReport(crashReport);
	}
	
	@Override
	public void preInit() { throw new NotImplementedException(); }

	@Override
	public void init() { throw new NotImplementedException(); }

	@Override
	public void postInit() { throw new NotImplementedException(); }

	@Override
	public void serverStarting(FMLServerStartingEvent event) { throw new NotImplementedException(); }

	@Override
	public void serverStopping(FMLServerStoppingEvent event) { throw new NotImplementedException(); }

	@Override
	public String getShortModName() 
	{
		return getLongModName();
	}

	@Override
	public String getLongModName() 
	{
		return "RadixCore";
	}

	@Override
	public String getVersion() 
	{
		return "1.0.0";
	}

	@Override
	public String getUpdateURL() 
	{
		return "http://pastebin.com/raw.php?i=fWd8huwd";
	}

	@Override
	public String getRedirectURL() 
	{
		return "{REDIR}";
	}

	@Override
	public ModLogger getLogger() 
	{
		return logger;
	}

	@Override
	public AbstractPacketCodec getPacketCodec() 
	{
		return null;
	}

	@Override
	public AbstractPacketHandler getPacketHandler() 
	{
		return null;
	}

	@Override
	public PacketPipeline getPacketPipeline() 
	{
		return null;
	}

	@Override
	public Class getPacketTypeClass() 
	{
		return null;
	}

	@Override
	public Class getEventHookClass()
	{
		return null;
	}

	@Override
	public void initializeProxy() { throw new NotImplementedException(); }

	@Override
	public void initializeItems() { throw new NotImplementedException(); }

	@Override
	public void initializeBlocks() { throw new NotImplementedException(); }

	@Override
	public void initializeRecipes() { throw new NotImplementedException(); }

	@Override
	public void initializeSmeltings() { throw new NotImplementedException(); }

	@Override
	public void initializeAchievements() { throw new NotImplementedException(); }

	@Override
	public void initializeEntities() { throw new NotImplementedException(); }

	@Override
	public void initializeNetwork() { throw new NotImplementedException(); }
}