package com.bryanreinero.dsvload;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import com.faunadb.client.types.Value;

public class FaunaConverter extends Converter<Map<String, Value>> {

    @Override
    public Map<String, Value> convert( String line ) {
    	
    	String[] values = line.split( String.valueOf( delimiter ) );
    	
        if ( values.length != transforms.size() )
            throw new IllegalArgumentException ( 
                        "Number of input fields != "+transforms.size() 
                    );
       
        Map<String, Value> document = new HashMap();

        for( int i = 0; i < transforms.size(); i++ ) {
        	
            SimpleEntry transformKV = transforms.get(i);
            String fieldName = ((String)transformKV.getKey());
            document.put(fieldName, Value.from(values[i]).get() );
        }

        return document;
    }

    public static void main( String[] args ) {
    	
    	String testString = "37.5,-122.2,Slartibartfast,\"Magrethea, Center of\",5676";

        System.out.println( "FaunaConverter, testing the following csv record: " + testString ) ;

    	Converter<Map<String, Value> > c = new FaunaConverter();
        c = new FaunaConverter();
        c.setDelimiter("(?!\\B\"[^\"]*),(?![^\"]*\"\\B)");
    	c.addField("geometry.coordinates.$1", Transformer.getTransformer(Transformer.TYPE_STRING));
        c.addField("geometry.coordinates.$0", Transformer.getTransformer(Transformer.TYPE_STRING));
        c.addField("user.name", Transformer.getTransformer(Transformer.TYPE_STRING));
        c.addField("user.address", Transformer.getTransformer(Transformer.TYPE_STRING));
        c.addField("_id", Transformer.getTransformer(Transformer.TYPE_STRING));     
    	System.out.println( "Here is the converted object\n" +  c.convert( testString ) );
    }
}