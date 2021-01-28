package com.bryanreinero.firehose.cli;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class CommandLineInterface {

	static Logger log = Logger.getLogger( CommandLineInterface.class.getName() );

	private Options options = new Options();
    private String appName;
	
	
	private Map<String, CallBack> callbacks = new HashMap<String, CallBack>();
	
	public void addCallBack( String key, CallBack cb ) {
		callbacks.put(key, cb);
	}
	
	public void addOptions( String appName ) throws Exception  {
		this.appName = appName;
		InputStream is = CommandLineInterface.class.getClassLoader().getResourceAsStream(appName+".json");
		
		try {
			
			Options newOptions = OptionFactory.parseJSON( OptionFactory.ingest( is ) );
			
			Iterator<Option> it = newOptions.getOptions().iterator();
			while( it.hasNext() )
				options.addOption( it.next() );
			
		} catch (IOException e) {
			throw new Exception( "Can't read options configuration", e );
		}
	}

	public void parse(String[] args) throws Exception {

		try {
			CommandLine line = new GnuParser().parse(options, args);

			for (Option option : line.getOptions()) 
				if ( line.hasOption( option.getOpt() ) ) 
					callbacks.get( option.getOpt() ).handle(option.getValues());					
			
		} catch( MissingArgumentException mae) {
			log.severe(mae.getMessage());
			new HelpFormatter().printHelp(appName, options);
			throw mae;
		} catch ( ParseException pe ) {
			log.severe(pe.getMessage());
			new HelpFormatter().printHelp(appName, options);
			throw pe;
		}catch (Exception e) {
			log.severe(e.getMessage());
			new HelpFormatter().printHelp(appName, options);
			throw  new IllegalArgumentException("Failed to parse command line arguments");
		}
	}

	public void addOption( Option o ) {
		options.addOption( o );
	}
}