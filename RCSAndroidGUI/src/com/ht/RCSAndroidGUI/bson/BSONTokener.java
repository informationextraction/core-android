package com.ht.RCSAndroidGUI.bson;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/***
Copyright (c) 2010 Ufuk Kayserilioglu (ufuk@paralaus.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at
	http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/**
 * A BSONTokener takes a byte array and extracts elements from
 * it. It is used by the BSONDocument and BSONArray constructors to parse
 * BSON data.
 * @author Ufuk Kayserilioglu
 * @version 1
 */
public class BSONTokener {
    
    /**
     * The index of the next character.
     */
    private int myIndex;
    
    
    /**
     * The source byte data being tokenized.
     */
    private final byte[] mySource;


    /**
     * The the length of source byte data being tokenized.
     */
	private final int myLength;
	
	private final int myOffset;


	private final Calendar utcCalendar;
    
    
    /**
     * Construct a JSONTokener from a string.
     *
     * @param s     A source string.
     */
    public BSONTokener(byte[] s) {
    	this(s, 0, s.length);
    }
    
    public BSONTokener(byte[] s, int offset, int length) {
    	this.myIndex = this.myOffset = offset;    	
    	this.mySource = s;
    	this.myLength = length;
		utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }
        
    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     * @return true if not yet at the end of the source.
     */
    public boolean more() {
        return isIndexInBounds(this.myIndex);
    }
    
    private boolean isIndexInBounds(int idx) {
        return idx < this.myOffset + this.myLength;    	
    }
    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     */
    public byte next() {
        if (more()) {
            byte b = this.mySource[this.myIndex];
            this.myIndex += 1;
            return b;
        }
        return 0;
    }
    
    
    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param b The character to match.
     * @return The character.
     * @throws BSONException if the character does not match.
     */
    public byte next(byte b) throws BSONException {
        byte n = next();
        if (n != b) {
            throw syntaxError("Expected '" + b + "' and instead saw '" +
                n + "'.");
        }
        return n;
    }
    
    
    /**
     * Get the next n characters.
     *
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @throws BSONException
     *   Substring bounds error if there are not
     *   n characters remaining in the source string.
     */
    public String next(int n) throws BSONException {
        int i = this.myIndex;
        int j = i + n;
        if (!isIndexInBounds(j)) {
            throw syntaxError("Substring bounds error");
        }
        this.myIndex += n;
        try {
			return new String(mySource, i, n, "utf-8");
		} catch (Throwable t) {
			throw new BSONException(t);
		}
    }
    
    
    /**
     * Get the next char in the string, skipping whitespace
     * and comments (slashslash, slashstar, and hash).
     * @throws BSONException
     * @return  A character, or 0 if there are no more characters.
     */
    public byte nextClean() throws BSONException {
    	return next(); // It seems like we don't really need a "nextClean".
    }
    
    
    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws BSONException Unterminated string.
     */
    public String nextString() throws BSONException {
    	int length = nextInt();
    	String data = next(length - 1);
    	next();
    	return data;
    }
    
    
    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws BSONException If syntax error.
     *
     * @return An object.
     */
    public BSONElement nextElement() throws BSONException {
    	try {
			return readElement();
		} catch (UnsupportedFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new BSONException("read");
		}
    }
    
    
    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A JSONException object, suitable for throwing
     */
    public BSONException syntaxError(String message) {
        return new BSONException(message + toString());
    }
    
    
    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at character [this.myIndex] of [this.mySource]"
     */
    public String toString() {
        return " at character " + this.myIndex + " of " + this.mySource;
    }


	public int nextInt() {
		int i = 0;
		for (int idx = 0; idx < 4; idx++) {
			i |= (int)((next() & 0xff) << (idx * 8));
		}
		return i;
	}

	public long nextLong() {
		long l = 0;
		for (int idx = 0; idx < 8; idx++) {
			l |= (long)((next() & 0xff) << (idx * 8));
		}
		return l;
	}	

	public int size() {
		return myLength;
	}


	public String nextCString() throws BSONException {
        int i = this.myIndex;
        int length = 0;
        while (next() != 0) {            
            length++;
        }
        try {
			return new String(mySource, i, length, "utf-8");
		} catch (Throwable t) {
			throw new BSONException(t);
		}
	}


	private BSONElement readElement() throws BSONException, UnsupportedFieldException {
		byte type = next();
		
		if (type == 0x00)
			return null;
		
		String key = nextCString();
		Object value = null;
		switch (type) {
//			case BSONElement.TYPE_FLOAT:			
//				break;
			case BSONElement.TYPE_STRING: {
				value = nextString();
				break;
			}
			case BSONElement.TYPE_INT32: {
				value = new Integer(nextInt());
				break;
			}				
			case BSONElement.TYPE_INT64: {
				value = new Long(nextLong());
				break;
			}				
			case BSONElement.TYPE_DATETIME: {
				long timestamp = nextLong();
				utcCalendar.setTime(new Date(timestamp));
				value = utcCalendar.getTime();
				break;
			}
			case BSONElement.TYPE_BOOLEAN: {
				byte b = next();
				value = (b == 0x00) ? Boolean.FALSE : Boolean.TRUE; // TODO: Do this better.
				break;
			}
			case BSONElement.TYPE_NULL: {
				value = BSONDocument.NULL;
				break;
			}
			case BSONElement.TYPE_ARRAY: {
				int offset = myIndex;
				int length = nextInt();
				value = new BSONArray(mySource, offset, length);
				break;
			}
			case BSONElement.TYPE_DOCUMENT: {
				int offset = myIndex;
				int length = nextInt();
				value = new BSONDocument(mySource, offset, length);
				break;
			}				
			default:
				throw new UnsupportedFieldException("The element type " + type + " is not supported.");
		}
		
		return new BSONElement(key, value, type);
	}
}