package com.mycompany.service;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mycompany.domain.ScriptLog;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;


public class MigrationService
{
	final static Logger logger = LoggerFactory.getLogger(MigrationService.class);

	public static void runUpdateScripts() throws IOException
	{
		logger.info("Running update scripts");
		Config conf = ConfigFactory.defaultApplication();
		String dbName = StringUtils.substringAfterLast(conf.getString("db"), "/");
		DB db = new MongoClient().getDB(dbName);
		try
		{
			Jongo jongo = new Jongo(db);
			MongoCollection scriptlogs = jongo.getCollection("scriptlogs");

			File scriptFolder = new File(System.getProperty("user.dir") + "/public/db-update");
			for (final File script : scriptFolder.listFiles())
			{
				String scriptName = script.getName();
				String scriptPath = script.getAbsolutePath();

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
			logger.info("Update scripts performed");
		} finally
		{
			db.getMongo().close();
		}

	}
}
