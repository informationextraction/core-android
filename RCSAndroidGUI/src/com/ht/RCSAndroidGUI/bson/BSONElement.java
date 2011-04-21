package com.ht.RCSAndroidGUI.bson;


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

public class BSONElement {
	public static final byte TYPE_FLOAT = 0x01;
	public static final byte TYPE_STRING = 0x02;
	public static final byte TYPE_DOCUMENT = 0x03;
	public static final byte TYPE_ARRAY = 0x04;
	public static final byte TYPE_BINARY = 0x05;
	public static final byte TYPE_OBJECTID = 0x07;
	public static final byte TYPE_BOOLEAN = 0x08;
	public static final byte TYPE_DATETIME = 0x09;
	public static final byte TYPE_NULL = 0x0A;
	public static final byte TYPE_REGEXP = 0x0B;
	public static final byte TYPE_JAVASCRIPT = 0x0D;
	public static final byte TYPE_SYMBOL = 0x0E;
	public static final byte TYPE_JAVASCRIPT_WITH_SCOPE = 0x0F;
	public static final byte TYPE_INT32 = 0x10;
	public static final byte TYPE_TIMESTAMP = 0x11;
	public static final byte TYPE_INT64 = 0x12;
	
	private final String key;
	private final Object value;
	private final byte type;
	
	public BSONElement(String key, Object value, byte type) {
		this.key = key;
		this.value = value;
		this.type = type;		
	}
	
	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public byte getType() {
		return type;
	}
	
	public String toString() {
		return value.toString();
	}
	
	public boolean isType(int type) {
		return this.type == type;
	}
	
	/**** Typed Value Getters ****/
	
    public long getLong() throws BSONException {
        if (isType(TYPE_INT32)) {
            return ((Integer)value).intValue();
        } else if (isType(TYPE_INT64)) {
            return ((Long)value).longValue();
        } else if (isType(TYPE_FLOAT)) {
            return (long) ((Double)value).doubleValue();
        }
        
        throw new BSONException("BSONElement [" + key + "] is not a number.");
    }

	public BSONDocument getDocument() throws BSONException {
        if (isType(TYPE_DOCUMENT)) {
            return (BSONDocument)value;
        }
        throw new BSONException("BSONElement [" + key + "] is not a JSONObject.");
	}

	public BSONArray getArray() throws BSONException {
        if (isType(TYPE_ARRAY)) {
            return (BSONArray)value;
        }
        throw new BSONException("BSONElement [" + key + "] is not a JSONArray.");
	}

	public int getInt() throws BSONException {
        if (isType(TYPE_INT32)) {
            return ((Integer)value).intValue();
        } else if (isType(TYPE_INT64)) {
            return (int) ((Long)value).longValue();
        } else if (isType(TYPE_FLOAT)) {
            return (int) ((Double)value).doubleValue();
        } 
        throw new BSONException("BSONElement [" + key + "] is not a number.");
	}

	public double getDouble() throws BSONException {
        if (isType(TYPE_INT32)) {
            return (double) ((Integer)value).intValue();
        } else if (isType(TYPE_INT64)) {
            return (double) ((Long)value).longValue();
        } else if (isType(TYPE_FLOAT)) {
            return ((Double)value).doubleValue();
//        } else if (o instanceof String) {
//            try {
//                 return Double.valueOf((String)o).doubleValue();
//            } catch (Exception e) {
//                throw new JSONException("JSONObject[" + quote(key) +
//                    "] is not a number.");
//            }
        } 
        throw new BSONException("BSONElement [" + key + "] is not a number.");
	}

	public boolean getBoolean() throws BSONException {
        if (isType(TYPE_BOOLEAN)) {
	        if (value.equals(Boolean.FALSE)) {
	            return false;
	        } else if (value.equals(Boolean.TRUE)) {
	            return true;
	        }
		}
        throw new BSONException("BSONElement [" + key + "] is not a Boolean.");
	}
	
}
