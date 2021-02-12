package com.bryanreinero.dsvload;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public abstract class Converter <V extends Object> {
	
	String delimiter = "(?!\\B\"[^\"]*),(?![^\"]*\"\\B)" ;
	
	static final String fieldNameSeparator = "\\.";
	static final Pattern arrayElemPosition = Pattern.compile( "^\\$(\\d+)$" );

    static final List<SimpleEntry<String, Transformer>> transforms
        = new ArrayList<SimpleEntry<String, Transformer>>();
    
    public Converter(){};

    public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

    /**
     * 
     * @param name
     * @param transformer
     */
    public void addField( String name, Transformer transformer ) {
        transforms.add(
            new SimpleEntry( name, transformer )
        );
    }

    public abstract V convert( String line );
    	
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
}