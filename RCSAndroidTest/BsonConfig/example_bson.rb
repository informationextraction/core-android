#! /usr/bin/env ruby

require 'bson'
require 'pp'

@num=0

def printTest(ex)
  print "// "
  print ex.to_s + "\n"
  print "private static final byte[] testData_" + @num.to_s + " = {"
  @num+=1
  s=BSON.serialize(ex).unpack("C*");
  s.each{|c|
    if c>=128
      print "(byte)"
    end
    print "#{c},"
    }
  print "};\n"
end

ex={"fieldNum" => 1}
printTest(ex)

ex1={"fieldNum" => 1234, "fieldDouble" => 1.1234, "fieldString" => "String", "fieldBool" => true}
printTest(ex1)

ex2={"fieldArray" => [1,2,3,4,5], "fieldDoc" => ex1 }
printTest(ex2)

ex3={"fieldFull" => [ex,ex1,ex2], "endToken" => "end" }
printTest(ex3)




