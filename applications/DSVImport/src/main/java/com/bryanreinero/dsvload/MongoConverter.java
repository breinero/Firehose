package com.bryanreinero.dsvload;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import com.bryanreinero.dsvload.Transformer.Type;

import org.bson.Document;

public class MongoConverter extends Converter<Document> {

 	@Override
    public Document convert( String line ) {
    	
    	String[] values = line.split( String.valueOf( delimiter ) );
		Document document = new Document();
    	
        if ( values.length != transforms.size() )
            throw new IllegalArgumentException ( 
                        "Number of input fields != "+transforms.size() 
                    );

        for( int i = 0; i < transforms.size(); i++ ) {
        	
            SimpleEntry transformKV = transforms.get(i);
            String fieldName = ((String)transformKV.getKey());
            Object value = ((Transformer)transformKV.getValue()).transform( values[i] );
            
            nest( document, fieldName.split( fieldNameSeparator ), 0, value );
        }
        return document;
    }
    
    public static void convert( Map<String, Object> document, String name, Type type, String value ) {	
    	try {
    	Object v = Transformer.getTransformer(type.getName()).transform(value);
        nest( document, name.split( fieldNameSeparator ), 0, v );
    	}	catch ( NumberFormatException e ) {
    		throw new IllegalArgumentException( "Field "+name+" should be of type "+type, e );
    	}
    }
    
    private static void nest( Object object, String[] prefix, int i, Object value ) {
    	String name = prefix[i];
    	Matcher m;
    	
    	if ( i < prefix.length - 1  ) {

    		// casting hell
    		Document parent = (Document)object;
    		Object obj = parent.get( name );
    		
    		if ( obj == null ) {
    			// look ahead to see if this is an array
    			m = arrayElemPosition.matcher( prefix[ i + 1 ] );
    			if( m.matches() ) 
    				obj = new ArrayList();

    			else
    				obj = new Document();

    			parent.put( name, obj );
    		}
    		
    		nest( obj, prefix, ++i, value );
    	} else {
    		// check if this is an array element
    		m = arrayElemPosition.matcher( name );
    		if ( object instanceof ArrayList  && m.matches() ) {
    			
    			ArrayList array = ((ArrayList)object);
    		
    			// frontfill if we are getting array elements out of order
    			Integer index = Integer.parseInt(m.group(1));
    			while ( index >= array.size() )
    				array.add(null);
    		
    			// value is an array element 
    			array.set(index, value);
    		}
    		else {
				((Document)object).put( name, value);
    		}
    	}
    }
    
    @Override
    public String toString() {
    	StringBuffer buf =  new StringBuffer( "{ fields: [");
    	
    	boolean first = true;
    	
    	for ( Entry entry : transforms ) {
    		if( !first )
    			buf.append(",");
    		else
    			first = false;
    		buf.append(" { field: \""+entry.getKey()+"\", type: "+entry.getValue().toString()+" }" );
    	}
    	buf.append(" ] }");
		return buf.toString();
    }
    
    public static void main( String[] args ) {
    	
    	String testString = "37.5,-122.2,Slartibartfast,\"Magrethea, Center of\",5676";

        System.out.println( "Running MongoConverter\n\ttesting the following csv record: " + testString ) ;

    	Converter<Document> c = new MongoConverter();
        c = new MongoConverter();
        c.setDelimiter("(?!\\B\"[^\"]*),(?![^\"]*\"\\B)");
    	c.addField("geometry.coordinates.$1", Transformer.getTransformer(Transformer.TYPE_FLOAT));
        c.addField("geometry.coordinates.$0", Transformer.getTransformer(Transformer.TYPE_FLOAT));
        c.addField("user.name", Transformer.getTransformer(Transformer.TYPE_STRING));
        c.addField("user.address", Transformer.getTransformer(Transformer.TYPE_STRING));
        c.addField("_id", Transformer.getTransformer(Transformer.TYPE_INT));     
    	System.out.println( "Here is the converted object\n" +  c.convert( testString ) );
    }
}