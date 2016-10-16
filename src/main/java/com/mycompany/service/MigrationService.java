package com.mycompany.service;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mycompany.App;
import com.mycompany.domain.ScriptLog;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class MigrationService
{
	final static Logger logger = LoggerFactory.getLogger(MigrationService.class);

	public static void runUpdateScripts() throws IOException, URISyntaxException
	{
		logger.info("Running update scripts");
		Config conf = ConfigFactory.defaultApplication();
		String dbName = StringUtils.substringAfterLast(conf.getString("db"), "/");
		DB db = new MongoClient().getDB(dbName);
		try
		{
			Jongo jongo = new Jongo(db);
			MongoCollection scriptlogs = jongo.getCollection("scriptlogs");
			String path = "db-update";
			final File jarFile = new File(MigrationService.class.getProtectionDomain().getCodeSource().getLocation().getPath());

			if (jarFile.isFile())
			{  // Run with JAR file
				final JarFile jar = new JarFile(jarFile);
				final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				while (entries.hasMoreElements())
				{
					final String name = entries.nextElement().getName();
					if (name.startsWith(path + "/"))
					{ //filter according to the path
						logger.info(name);
						InputStream initialStream = MigrationService.class.getResourceAsStream(name);
						File targetFile = new File(System.getProperty("user.dir") + "/" + StringUtils.substringAfterLast(name, "/"));
						FileUtils.copyInputStreamToFile(initialStream, targetFile);
						runScript(dbName, scriptlogs, targetFile.getName(), targetFile.getAbsolutePath());
					}
				}
				jar.close();
			}
			else
			{ // Run with IDE
				final URL url = MigrationService.class.getResource("/" + path);
				if (url != null)
				{
					logger.info(url.toString());
					final File scriptFolder = new File(url.toURI());
					for (File script : scriptFolder.listFiles())
					{
						runScript(dbName, scriptlogs, script.getName(), script.getAbsolutePath());
					}
				}
			}
			logger.info("Update scripts performed");
		}
		finally
		{
			db.getMongo().close();
		}
	}

	private static void runScript(String dbName, MongoCollection scriptlogs, String scriptName, String scriptPath)
	{
		ScriptLog log = scriptlogs.findOne(String.format("{version: '%s'}", scriptName)).as(ScriptLog.class);
		if (log == null)
		{
			try
			{
				logger.info("Applying script " + scriptName);
				String command = String.format("mongo %s %s", dbName, scriptPath);
				Process p = Runtime.getRuntime().exec(command);
				String line;
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = in.readLine()) != null)
				{
					logger.info(line);
				}
				in.close();
			}
			catch (Exception e)
			{
				logger.error("Error running update scripts", e);
			}
			log = new ScriptLog();
			log.version = scriptName;
			log.date = new Date();
			scriptlogs.insert(log);
		}
	}
}
