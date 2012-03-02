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
	lines = File.open(java).readlines();
	matchString="Messages.getString(.*)";
	lines.each{ |line|
		#pp line
		line.gsub(/Messages.getString\(\"(\w+\.\d+)\"\)/) { |l|
			#pp l
			javaArray.push($1) 
		}
	}
}

#pp javaArray

javaArray.each{ |j|
	if ! refSet.include?(j) 
		print "error #{j} not referenced\n"
	else
		refSet.delete(j)
 	end
}

pp refSet