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

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;


/**
 * A BSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>BSONArray</code>, <code>BSONDocument</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>BSONDocument.NULL object</code>.
 * <p>
 * The constructor can convert a BSON byte array into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coersion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there
 *     is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 *     will be ignored.</li>
 * </ul>

 * @author Ufuk Kayserilioglu
 * @version 1
 */
public class BSONArray {


    /**
     * The Vector where the JSONArray's properties are kept.
     */
    private Vector myArrayList;


    /**
     * Construct an empty JSONArray.
     */
    public BSONArray() {
        this.myArrayList = new Vector();
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     * @param x A JSONTokener
     * @throws BSONException If there is a syntax error.
     */
    public BSONArray(BSONTokener x) throws BSONException {
        this();
        int length = x.nextInt();
        if (x.size() != length) {
            throw x.syntaxError("The length of the BSONArray does not match with the length of given data.");
        }
        
        for (;;) {
        	BSONElement e = x.nextElement();
        	
        	if (e == null)
        		return;
        	
        	this.myArrayList.addElement(e);
        }
    }


    /**
     * Construct a JSONArray from a source sJSON text.
     * @param string     A string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     *  @throws BSONException If there is a syntax error.
     */
    public BSONArray(byte[] data, int offset, int length) throws BSONException {
        this(new BSONTokener(data, offset, length));
    }


    /**
     * Construct a JSONArray from a Collection.
     * @param collection     A Collection.
     */
    public BSONArray(Vector collection) {
        if (collection == null) {
            this.myArrayList = new Vector();
        } else {
            int size = collection.size();
            this.myArrayList = new Vector(size);
            for (int i=0; i < size; i++) {
                this.myArrayList.addElement(collection.elementAt(i));
            }
        }
    }

    /**
     * Get the object value associated with an index.
     * @param index
     *  The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws BSONException If there is no value for the index.
     */
    private BSONElement getElement(int index) throws BSONException {
        BSONElement o = opt(index);
        if (o == null) {
            throw new BSONException("JSONArray[" + index + "] not found.");
        }
        return o;
    }

    /**
     * Get the object value associated with an index.
     * @param index
     *  The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws BSONException If there is no value for the index.
     */
    public Object get(int index) throws BSONException {
        return getElement(index).getValue();
    }

    /**
     * Get the boolean value associated with an index.
     * The string values "true" and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The truth.
     * @throws BSONException If there is no value for the index or if the
     *  value is not convertable to boolean.
     */
    public boolean getBoolean(int index) throws BSONException {
    	BSONElement o = getElement(index);
    	return o.getBoolean();
    }

     /**
      * Get the double value associated with an index.
      *
      * @param index The index must be between 0 and length() - 1.
      * @return      The value.
      * @throws   JSONException If the key is not found or if the value cannot
      *  be converted to a number.
      */
     public double getDouble(int index) throws BSONException {
         BSONElement o = getElement(index);
         return o.getDouble();
     }


     /**
      * Get the int value associated with an index.
      *
      * @param index The index must be between 0 and length() - 1.
      * @return      The value.
      * @throws   JSONException If the key is not found or if the value cannot
      *  be converted to a number.
      *  if the value cannot be converted to a number.
      */
     public int getInt(int index) throws BSONException {
         BSONElement o = getElement(index);
         return o.getInt();
     }


    /**
     * Get the JSONArray associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A JSONArray value.
     * @throws BSONException If there is no value for the index. or if the
     * value is not a JSONArray
     */
    public BSONArray getArray(int index) throws BSONException {
        BSONElement o = getElement(index);
        if (o.getType() == BSONElement.TYPE_ARRAY) {
            return (BSONArray)o.getValue();
        }
        throw new BSONException("JSONArray[" + index +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject associated with an index.
     * @param index subscript
     * @return      A JSONObject value.
     * @throws BSONException If there is no value for the index or if the
     * value is not a JSONObject
     */
    public BSONDocument getDocument(int index) throws BSONException {
    	BSONElement o = getElement(index);
        if (o.getType() == BSONElement.TYPE_DOCUMENT) {
            return (BSONDocument)o.getValue();
        }
        throw new BSONException("JSONArray[" + index +
            "] is not a JSONObject.");
    }


     /**
      * Get the long value associated with an index.
      *
      * @param index The index must be between 0 and length() - 1.
      * @return      The value.
      * @throws   JSONException If the key is not found or if the value cannot
      *  be converted to a number.
      */
     public long getLong(int index) throws BSONException {
         BSONElement o = getElement(index);
         return o.getLong();
     }

    /**
     * Get the string associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A string value.
     * @throws BSONException If there is no value for the index.
     */
    public String getString(int index) throws BSONException {
        return get(index).toString();
    }


    /**
     * Determine if the value is null.
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(int index) {
        return BSONDocument.NULL.equals(opt(index));
    }


    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element.
     * Warning: This method assumes that the data structure is acyclical.
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws BSONException If the array contains an invalid number.
     */
    public String join(String separator) throws BSONException {
        int len = length();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(BSONDocument.valueToString(this.myArrayList.elementAt(i)));
        }
        return sb.toString();
    }


    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return this.myArrayList.size();
    }


    /**
     * Get the optional object value associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      An object value, or null if there is no
     *              object at that index.
     */
    public BSONElement opt(int index) {
        return (index < 0 || index >= length()) ?
            null : (BSONElement) this.myArrayList.elementAt(index);
    }

    /**
     * Get the optional boolean value associated with an index.
     * It returns false if there is no value at that index,
     * or if the value is not Boolean.TRUE or the String "true".
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The truth.
     */
    public boolean optBoolean(int index)  {
        return optBoolean(index, false);
    }
    
    /**
     * Get the optional boolean value associated with an index.
     * It returns the defaultValue if there is no value at that index or if
     * it is not a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     A boolean default.
     * @return      The truth.
     */
    public boolean optBoolean(int index, boolean defaultValue)  {
        try {
            return getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional double value associated with an index.
     * NaN is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     */
    public double optDouble(int index) {
        return optDouble(index, Double.NaN);
    }

    /**
     * Get the optional double value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index subscript
     * @param defaultValue     The default value.
     * @return      The value.
     */
    public double optDouble(int index, double defaultValue) {
        try {
            return getDouble(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional int value associated with an index.
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     */
    public int optInt(int index) {
        return optInt(index, 0);
    }

    /**
     * Get the optional int value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      The value.
     */
    public int optInt(int index, int defaultValue) {
        try {
            return getInt(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional JSONArray associated with an index.
     * @param index subscript
     * @return      A JSONArray value, or null if the index has no value,
     * or if the value is not a JSONArray.
     */
    public BSONArray optArray(int index) {
        BSONElement o = opt(index);
        return o.isType(BSONElement.TYPE_ARRAY) ? (BSONArray)o.getValue() : null;
    }


    /**
     * Get the optional JSONObject associated with an index.
     * Null is returned if the key is not found, or null if the index has
     * no value, or if the value is not a JSONObject.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      A JSONObject value.
     */
    public BSONDocument optBSONDocument(int index) {
    	BSONElement o = opt(index);
        return o.isType(BSONElement.TYPE_DOCUMENT) ? (BSONDocument)o.getValue() : null;
    }

    /**
     * Get the optional long value associated with an index.
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     */
    public long optLong(int index) {
        return optLong(index, 0);
    }

    /**
     * Get the optional long value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      The value.
     */
    public long optLong(int index, long defaultValue) {
        try {
            return getLong(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional string value associated with an index. It returns an
     * empty string if there is no value at that index. If the value
     * is not a string and is not null, then it is coverted to a string.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      A String value.
     */
    public String optString(int index) {
        return optString(index, "");
    }


    /**
     * Get the optional string associated with an index.
     * The defaultValue is returned if the key is not found.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      A String value.
     */
    public String optString(int index, String defaultValue) {
    	BSONElement o = opt(index);
        return o != null ? o.getValue().toString() : defaultValue;
    }


    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this.
     */
    public BSONArray put(boolean value) {
        put(new BSONElement(Integer.toString(this.myArrayList.size()), value ? Boolean.TRUE : Boolean.FALSE, BSONElement.TYPE_BOOLEAN));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param value	A Collection value.
     * @return		this.
     */
    public BSONArray put(Vector value) {
        put(new BSONElement(Integer.toString(this.myArrayList.size()), new BSONArray(value), BSONElement.TYPE_ARRAY));
        return this;
    }
    

//#if CLDC!="1.0"
//#     /**
//#      * Append a double value. This increases the array's length by one.
//#      *
//#      * @param value A double value.
//#      * @throws JSONException if the value is not finite.
//#      * @return this.
//#      */
//#     public JSONArray put(double value) throws JSONException {
//#         Double d = new Double(value);
//#         JSONObject.testValidity(d);
//#         put(d);
//#         return this;
//#     }
//#endif

    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this.
     */
    public BSONArray put(int value) {
        put(new BSONElement(Integer.toString(this.myArrayList.size()), new Integer(value), BSONElement.TYPE_INT32));
        return this;
    }


    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this.
     */
    public BSONArray put(long value) {
        put(new BSONElement(Integer.toString(this.myArrayList.size()), new Long(value), BSONElement.TYPE_INT64));
        return this;
    }


//#ifdef PRODUCER
//#     /**
//#      * Put a value in the JSONArray, where the value will be a
//#      * JSONObject which is produced from a Map.
//#      * @param value	A Map value.
//#      * @return		this.
//#      */
//#     public JSONArray put(Hashtable value) {
//#         put(new JSONObject(value));
//#         return this;
//#     }
//#endif    
    
    /**
     * Append an object value. This increases the array's length by one.
     * @param value An object value.  The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     */
    public BSONArray put(Object value) {
        this.myArrayList.addElement(value);
        return this;
    }


    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     * @param index The subscript.
     * @param value A boolean value.
     * @return this.
     * @throws BSONException If the index is negative.
     */
    public BSONArray put(int index, boolean value) throws BSONException {
        put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }
    
    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param index The subscript.
     * @param value	A Collection value.
     * @return		this.
     * @throws BSONException If the index is negative or if the value is
     * not finite.
     */
    public BSONArray put(int index, Vector value) throws BSONException {
        put(index, new BSONArray(value));
        return this;
    }

    
    /**
     * Put or replace a double value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A double value.
     * @return this.
     * @throws JSONException If the index is negative or if the value is
     * not finite.
     */
    public BSONArray put(int index, double value) throws BSONException {
        put(index, new Double(value));
        return this;
    }

    /**
     * Put or replace an int value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value An int value.
     * @return this.
     * @throws BSONException If the index is negative.
     */
    public BSONArray put(int index, int value) throws BSONException {
        put(index, new Integer(value));
        return this;
    }


    /**
     * Put or replace a long value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A long value.
     * @return this.
     * @throws BSONException If the index is negative.
     */
    public BSONArray put(int index, long value) throws BSONException {
        put(index, new Long(value));
        return this;
    }


//#ifdef PRODUCER
//#     /**
//#      * Put a value in the JSONArray, where the value will be a
//#      * JSONObject which is produced from a Map.
//#      * @param index The subscript.
//#      * @param value	The Map value.
//#      * @return		this.
//#      * @throws JSONException If the index is negative or if the the value is
//#      *  an invalid number.
//#      */
//#     public JSONArray put(int index, Hashtable value) throws JSONException {
//#         put(index, new JSONObject(value));
//#         return this;
//#     }
//#endif    
    
    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     *  than the length of the JSONArray, then null elements will be added as
     *  necessary to pad it out.
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     * @throws BSONException If the index is negative or if the the value is
     *  an invalid number.
     */
    public BSONArray put(int index, Object value) throws BSONException {
        BSONDocument.testValidity(value);
        if (index < 0) {
            throw new BSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            this.myArrayList.setElementAt(value, index);
        } else {
            while (index != length()) {
                put(BSONDocument.NULL);
            }
            put(value);
        }
        return this;
    }


    /**
     * Produce a JSONObject by combining a JSONArray of names with the values
     * of this JSONArray.
     * @param names A JSONArray containing a list of key strings. These will be
     * paired with the values.
     * @return A JSONObject, or null if there are no names or if this JSONArray
     * has no values.
     * @throws BSONException If any of the names are null.
     */
    public BSONDocument toJSONObject(BSONArray names) throws BSONException {
        if (names == null || names.length() == 0 || length() == 0) {
            return null;
        }
        BSONDocument jo = new BSONDocument();
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }


    /**
     * Make a JSON text of this JSONArray. For compactness, no
     * unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This
     * could occur if the array contains an invalid number.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable
     *  representation of the array.
     */
    public String toString() {
        try {
            return '[' + join(",") + ']';
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *  with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws BSONException
     */
    public String toString(int indentFactor) throws BSONException {
        return toString(indentFactor, 0);
    }


    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @param indent The indention of the top level.
     * @return a printable, displayable, transmittable
     *  representation of the array.
     * @throws BSONException
     */
    String toString(int indentFactor, int indent) throws BSONException {
        int len = length();
        if (len == 0) {
            return "[]";
        }
        int i;
        StringBuffer sb = new StringBuffer("[");
        if (len == 1) {
            sb.append(BSONDocument.valueToString(this.myArrayList.elementAt(0),
                    indentFactor, indent));
        } else {
            int newindent = indent + indentFactor;
            sb.append('\n');
            for (i = 0; i < len; i += 1) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(BSONDocument.valueToString(this.myArrayList.elementAt(i),
                        indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; i += 1) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }


    /**
     * Write the contents of the JSONArray as JSON text to a writer.
     * For compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return The writer.
     * @throws BSONException
     */
    public Writer write(Writer writer) throws BSONException {
        try {
            boolean b = false;
            int     len = length();

            writer.write('[');

            for (int i = 0; i < len; i += 1) {
                if (b) {
                    writer.write(',');
                }
                Object v = this.myArrayList.elementAt(i);
                if (v instanceof BSONDocument) {
                    ((BSONDocument)v).write(writer);
                } else if (v instanceof BSONArray) {
                    ((BSONArray)v).write(writer);
                } else {
                    writer.write(BSONDocument.valueToString(v));
                }
                b = true;
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
           throw new BSONException(e);
        }
    }
}