require 'pp'
require 'find'

# read every $reference in messages.properties.all
# find every Messages.getString("$reference");
# verify that the sets are the same.

refSet = {}
refFileName = "preprocess/messages.properties.all"

File.open(refFileName).readlines.each { |row|
	key,value=row.split("=")
	refSet[key]=value;
}

#pp refSet

javaArray = []
javaFiles = Dir["**/*.java"]

javaFiles.each{ |java|
	lines = File.open(java, :encoding => "BINARY").readlines();
	matchString="Messages.getString(.*)";
	lines.each{ |line|
		#pp line
		line.gsub(/Messages.getString\(\"(\w+_\d+)\"\)/) { 
			|l| 
			#pp l 
			javaArray.push($1) 
		}		
	}
}

#pp javaArray

javaArray.each{ |j|
	if ! refSet.include?(j) 
		print "error #{j} not mapped\n"
		#javaArray.delete(j)
	else
		#refSet.delete(j)
 	end
}

outlist = []
refSet.each{ |k,v|
	if ! javaArray.include?(k)
		#print "warning #{k} not used\n"
	else
		outlist.push "#{k}=#{v}"
	end
}

outlist.sort.each{ |l|
	print l
}
