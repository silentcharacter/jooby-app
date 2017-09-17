package com.mycompany.service;

import com.mycompany.domain.ScriptLog;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class MigrationService
{
	final static Logger logger = LoggerFactory.getLogger(MigrationService.class);

	public static void runUpdateScripts(Registry r)
	{
		logger.info("Running update scripts");
		Config conf = r.require(Config.class);
		String dbName = StringUtils.substringAfterLast(conf.getString("db"), "/");
		try
		{
			Jongo jongo = r.require(Jongo.class);
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
					if (name.startsWith(path + "/") && name.endsWith("script"))
					{ //filter according to the path
						logger.info(name);
						File targetFile = copyResourceToFile(name);
						runScript(dbName, scriptlogs, targetFile.getName(), targetFile.getAbsolutePath());
						Files.delete(targetFile.toPath());
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
		catch (URISyntaxException | IOException e) {
			logger.error("Error running update scripts", e);
		}
	}

	private static File copyResourceToFile(String name) throws IOException
	{
		InputStream initialStream = MigrationService.class.getClassLoader().getResourceAsStream(name);
		if (initialStream == null) {
			logger.error("initialStream == null");
		}
		String targetFileName = System.getProperty("user.dir") + "/" + StringUtils.substringAfterLast(name, "/");
		logger.info(targetFileName);
		try (FileOutputStream fos = new FileOutputStream(targetFileName)){
			byte[] buf = new byte[2048];
			int r;
			while(-1 != (r = initialStream.read(buf))) {
				fos.write(buf, 0, r);
			}
		}
		return new File(targetFileName);
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

				log = new ScriptLog();
				log.version = scriptName;
				log.date = new Date();
				scriptlogs.insert(log);
			}
			catch (Exception e)
			{
				logger.error("Error running update scripts", e);
			}

		}
	}
}
