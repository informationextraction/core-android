package com.android.service.test;

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

import junit.framework.TestCase;

import org.bsonspec.me.BSONArray;
import org.bsonspec.me.BSONDocument;
import org.bsonspec.me.BSONException;

import android.test.AndroidTestCase;

public class BSONDocumentTest extends AndroidTestCase {

	private static final byte[] testData1 = { 0x16, 0x00, 0x00, 0x00, 0x02,
			0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x00, 0x06, 0x00, 0x00, 0x00, 0x77,
			0x6F, 0x72, 0x6C, 0x64, 0x00, 0x00 };

	private static final byte[] testData2 = { 0x30, 0x00, 0x00, 0x00, 0x04,
			0x42, 0x53, 0x4F, 0x4E, 0x00, 0x25, 0x00, 0x00, 0x00, 0x02, 0x30,
			0x00, 0x08, 0x00, 0x00, 0x00, 0x61, 0x77, 0x65, 0x73, 0x6F, 0x6D,
			0x65, 0x00, 0x10, 0x31, 0x00, 0x05, 0x00, 0x00, 0x00, 0x12, 0x32,
			0x00, (byte) 0xc2, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	// {"fieldNum"=>1}
	private static final byte[] testData_0 = { 19, 0, 0, 0, 16, 102, 105, 101,
			108, 100, 78, 117, 109, 0, 1, 0, 0, 0, 0, };
	// {"fieldNum"=>1234, "fieldDouble"=>1.1234, "fieldString"=>"String",
	// "fieldBool"=>true}
	private static final byte[] testData_1 = { 76, 0, 0, 0, 16, 102, 105, 101,
			108, 100, 78, 117, 109, 0, (byte) 210, 4, 0, 0, 1, 102, 105, 101,
			108, 100, 68, 111, 117, 98, 108, 101, 0, (byte) 239, 56, 69, 71,
			114, (byte) 249, (byte) 241, 63, 2, 102, 105, 101, 108, 100, 83,
			116, 114, 105, 110, 103, 0, 7, 0, 0, 0, 83, 116, 114, 105, 110,
			103, 0, 8, 102, 105, 101, 108, 100, 66, 111, 111, 108, 0, 1, 0, };
	// {"fieldArray"=>[1, 2, 3, 4, 5], "fieldDoc"=>{"fieldNum"=>1234,
	// "fieldDouble"=>1.1234, "fieldString"=>"String", "fieldBool"=>true}}
	private static final byte[] testData_2 = { (byte) 143, 0, 0, 0, 4, 102,
			105, 101, 108, 100, 65, 114, 114, 97, 121, 0, 40, 0, 0, 0, 16, 48,
			0, 1, 0, 0, 0, 16, 49, 0, 2, 0, 0, 0, 16, 50, 0, 3, 0, 0, 0, 16,
			51, 0, 4, 0, 0, 0, 16, 52, 0, 5, 0, 0, 0, 0, 3, 102, 105, 101, 108,
			100, 68, 111, 99, 0, 76, 0, 0, 0, 16, 102, 105, 101, 108, 100, 78,
			117, 109, 0, (byte) 210, 4, 0, 0, 1, 102, 105, 101, 108, 100, 68,
			111, 117, 98, 108, 101, 0, (byte) 239, 56, 69, 71, 114, (byte) 249,
			(byte) 241, 63, 2, 102, 105, 101, 108, 100, 83, 116, 114, 105, 110,
			103, 0, 7, 0, 0, 0, 83, 116, 114, 105, 110, 103, 0, 8, 102, 105,
			101, 108, 100, 66, 111, 111, 108, 0, 1, 0, 0, };
	// {"fieldFull"=>[{"fieldNum"=>1}, {"fieldNum"=>1234, "fieldDouble"=>1.1234,
	// "fieldString"=>"String", "fieldBool"=>true}, {"fieldArray"=>[1, 2, 3, 4,
	// 5], "fieldDoc"=>{"fieldNum"=>1234, "fieldDouble"=>1.1234,
	// "fieldString"=>"String", "fieldBool"=>true}}], "endToken"=>"end"}
	private static final byte[] testData_3 = { 30, 1, 0, 0, 4, 102, 105, 101,
			108, 100, 70, 117, 108, 108, 0, (byte) 252, 0, 0, 0, 3, 48, 0, 19,
			0, 0, 0, 16, 102, 105, 101, 108, 100, 78, 117, 109, 0, 1, 0, 0, 0,
			0, 3, 49, 0, 76, 0, 0, 0, 16, 102, 105, 101, 108, 100, 78, 117,
			109, 0, (byte) 210, 4, 0, 0, 1, 102, 105, 101, 108, 100, 68, 111,
			117, 98, 108, 101, 0, (byte) 239, 56, 69, 71, 114, (byte) 249,
			(byte) 241, 63, 2, 102, 105, 101, 108, 100, 83, 116, 114, 105, 110,
			103, 0, 7, 0, 0, 0, 83, 116, 114, 105, 110, 103, 0, 8, 102, 105,
			101, 108, 100, 66, 111, 111, 108, 0, 1, 0, 3, 50, 0, (byte) 143, 0,
			0, 0, 4, 102, 105, 101, 108, 100, 65, 114, 114, 97, 121, 0, 40, 0,
			0, 0, 16, 48, 0, 1, 0, 0, 0, 16, 49, 0, 2, 0, 0, 0, 16, 50, 0, 3,
			0, 0, 0, 16, 51, 0, 4, 0, 0, 0, 16, 52, 0, 5, 0, 0, 0, 0, 3, 102,
			105, 101, 108, 100, 68, 111, 99, 0, 76, 0, 0, 0, 16, 102, 105, 101,
			108, 100, 78, 117, 109, 0, (byte) 210, 4, 0, 0, 1, 102, 105, 101,
			108, 100, 68, 111, 117, 98, 108, 101, 0, (byte) 239, 56, 69, 71,
			114, (byte) 249, (byte) 241, 63, 2, 102, 105, 101, 108, 100, 83,
			116, 114, 105, 110, 103, 0, 7, 0, 0, 0, 83, 116, 114, 105, 110,
			103, 0, 8, 102, 105, 101, 108, 100, 66, 111, 111, 108, 0, 1, 0, 0,
			0, 2, 101, 110, 100, 84, 111, 107, 101, 110, 0, 4, 0, 0, 0, 101,
			110, 100, 0, 0, };

	public void testBSONDocument1() throws BSONException {
		BSONDocument doc = new BSONDocument(testData1);

		assertEquals("world", doc.get("hello"));
	}

	public void testBSONDocument2Test() throws BSONException {
		BSONDocument doc = new BSONDocument(testData2);
		BSONArray values = doc.getArray("BSON");

		assertEquals(3, values.length());
		assertEquals("awesome", values.getString(0));
		assertEquals(5, values.getInt(1));
		assertEquals(1986, values.getLong(2));
	}

	public void testBSONDocumentZ0() throws BSONException {
		BSONDocument doc = new BSONDocument(testData_0);
		assertEquals(1, doc.getInt("fieldNum"));
		assertEquals(1, doc.length());
	}

	public void testBSONDocumentZ1() throws BSONException {
		BSONDocument doc = new BSONDocument(testData_1);
		assertEquals(1234, doc.getInt("fieldNum"));
		assertEquals("String", doc.getString("fieldString"));
		assertEquals(true, doc.getBoolean("fieldBoolean"));
		assertEquals(1.1234, doc.getDouble("fieldDouble"));
		assertEquals(4, doc.length());

	}

	public void testBSONDocumentZ2() throws BSONException {
		BSONDocument doc = new BSONDocument(testData_2);

		assertEquals(2, doc.length());
		BSONArray arr = doc.getArray("fieldArray");
		BSONDocument doc2 = doc.getDocument("fieldDoc");

		assertEquals(1234, doc2.getInt("fieldNum"));
		assertEquals("String", doc2.getString("fieldString"));
		assertEquals(true, doc2.getBoolean("fieldBoolean"));
		assertEquals(1.1234, doc2.getDouble("fieldDouble"));

	}

	public void testBSONDocumentZ3() throws BSONException {
		BSONDocument doc = new BSONDocument(testData_3);
		assertEquals(2, doc.length());
		BSONArray arr = doc.getArray("fieldFull");
		assertEquals("end", doc.getString("endToken"));
		assertEquals(3, arr.length());

		BSONDocument doc0 = arr.getDocument(0);
		assertEquals(1, doc0.length());
		BSONDocument doc1 = arr.getDocument(1);
		assertEquals(4, doc1.length());
		BSONDocument doc2 = arr.getDocument(2);
		assertEquals(2, doc2.length());

	}

}
