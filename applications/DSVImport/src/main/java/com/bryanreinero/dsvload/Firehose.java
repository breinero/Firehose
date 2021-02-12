package com.bryanreinero.dsvload;

import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.dao.faunadb.FaunaInsert;
import com.bryanreinero.firehose.dao.faunadb.FaunaService;
import com.bryanreinero.firehose.dao.mongo.Insert;
import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.firehose.util.Application;
import com.faunadb.client.FaunaClient;
import com.faunadb.client.query.Expr;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.faunadb.client.query.Language.Obj;
import static com.faunadb.client.query.Language.Value;

public class Firehose {

	private static final String appName = "Firehose";
	private final Application app;
	private final SampleSet samples;
	private final Statistics stats;
	private AtomicInteger linesRead = new AtomicInteger(0);
	private Converter converter = new Converter();
	private BufferedReader br = null;

	private String dburi = "mongodb://127.0.0.1:27017/";
    private MongoClient client = null;

    private MongoDAO<Insert> descriptor = null;

    private FaunaService faunaDescriptor;
    private String collectionName;

    private boolean isFauna = false;

	private Boolean verbose = false;
	private String filename = null;

	private AtomicBoolean running = new AtomicBoolean( true );

	private void unitOfWork() {

		String currentLine = null;

		try ( Interval total = samples.set("total") ) {
//            Expr document = Obj("name", Value("importing"), "color", Value("blue"));
//            app.getThreadPool().submitTask(new FaunaInsert(this.collectionName, document, this.faunaDescriptor));

					// read the next line from source file
					try (Interval readLine = samples.set("readline")) {
						synchronized (br) {
							currentLine = br.readLine();
						}
					} catch (IOException e1) {
                        running.set(false);
                        e1.printStackTrace();
                    }

                    if (currentLine == null)
						running.set(false);

					else {
                        linesRead.incrementAndGet();

                        if (!isFauna) {
                            Document object = null;

                            // Create the DBObject for insertion
                            try (Interval build = samples.set("build")) {
                                object = converter.convert(currentLine);
                            }


                            String taxi = object.getString( "taxi" );
                            Integer ts = object.getInteger( "ts" );
                            Document _id = new Document();
                            _id.put( "ts", ts );
                            _id.put( "taxi", taxi );
                            object.put( "_id", _id );

                            object.remove( "taxi" );
                            object.remove( "ts" );
                            app.getThreadPool().submitTask( new Insert( object, descriptor ) );
                        } else {
                            // create fauna docsj
                            Expr document = Obj("name", Value("importing"), "color", Value("blue"));
                            app.getThreadPool().submitTask(new FaunaInsert(this.collectionName, document, this.faunaDescriptor));
                        }
                    }
        }
	}

	public Firehose ( String[] args  ) throws Exception {

		app = new Application( appName );
        samples = app.getSampleSet();
        stats = new Statistics( samples );

        // First step, set up the command line interface
        app.setCommandLineInterfaceCallback(
                "h", new CallBack() {
                    @Override
                    public void handle(String[] values) {
                        for (String column : values) {
                            String[] s = column.split(":");
                            converter.addField( s[0], Transformer.getTransformer( s[1] ) );
                        }
                    }
                }
        );

		// custom command line callback for delimiter
        app.setCommandLineInterfaceCallback( "d", new CallBack() {
			@Override
			public void handle(String[] values) {
				converter.setDelimiter( values[0] );
			}
		});

		// custom command line callback for delimiter
        app.setCommandLineInterfaceCallback( "f", new CallBack() {
			@Override
			public void handle(String[] values) {
				filename  = values[0];
				try {
					br = new BufferedReader(new FileReader(filename));
				}catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});

        // custom command line callback for delimiter
        app.setCommandLineInterfaceCallback( "db", new CallBack() {
            @Override
            public void handle(String[] values) {
                dburi = values[0] ;
            }
        });

        // First step, set up the command line interface
        app.setCommandLineInterfaceCallback(
                "h", new CallBack() {
                    @Override
                    public void handle(String[] values) {
                        for (String column : values) {
                            String[] s = column.split(":");
                            converter.addField( s[0], Transformer.getTransformer( s[1] ) );
                        }
                    }
                }
        );

        // determine if we're exporting to fauna or mongo. default is mongo
        app.setCommandLineInterfaceCallback(
            "t", new CallBack() {
                @Override
                public void handle(String[] values) {
                    if (values[0]=="fauna") {
                        isFauna = true;
                    }
                }
        });

        if (!isFauna) {
            client = new MongoClient( new MongoClientURI( dburi ) );
            descriptor = new MongoDAO<>( "insert", "cluster", "taxi.taxilogs" );
            descriptor.setDatabase( client.getDatabase( descriptor.getDatabaseName() ) );
            descriptor.setCollection(
                    client.getDatabase( descriptor.getDatabaseName() ).getCollection( descriptor.getCollectionName() )

            );
            descriptor.setSamples( samples );
        } else {
            FaunaClient faunaClient = FaunaClient.builder()
                    .withSecret(System.getenv("FAUNA_KEY"))
                    .build();
            this.faunaDescriptor = new FaunaService("insert", faunaClient);
            this.collectionName = "import-collection";
            this.faunaDescriptor.setSamples(samples);
        }

        samples.start();
        app.addPrinable(this);

        try {
            app.parseCommandLineArgs( args );
        } catch ( IllegalArgumentException iae ){
            System.err.println("Can't initialize Firehose. Reason: "+iae.getLocalizedMessage() );
            throw new Exception( iae.getLocalizedMessage() );
        }
    }

    public void execute() {
        while ( running.get()  )
            unitOfWork() ;

        synchronized (br) {
            if (br != null) try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("{ ");
		buf.append("threads: " + app.getNumThreads());
		buf.append(", \"lines read\": "+ this.linesRead );
		buf.append(", samples: "+ stats.report() );

		if( verbose ) {
			buf.append(", converter: "+converter);
			buf.append(", source: "+filename);
		}
		buf.append(" }");
		return buf.toString();
	}

    public static void main( String[] args ) {
    	try {
    		new Firehose( args ).execute();
		}
		catch (Exception e) {
			System.err.println( "Error: "+e.getMessage()+". Exiting Firehose" );
			System.exit(-1);
		}
    }
}
