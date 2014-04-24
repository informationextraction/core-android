require 'pp'
require 'find'

require 'FileUtils'

# read every $reference in messages.properties.all
# find every Messages.getString("$reference");
# verify that the sets are the same.

refSet = {}
refFileName = "preprocess/messages.properties.all"

File.open(refFileName).readlines.each { |row|
	key,value=row.split("=")
	refSet[key]=value.chomp("\n");
}

pp refSet


javaFiles = Dir["src/**/*.java"]

javaFiles.each{ |java|
	lines = File.open(java, :encoding => "BINARY").readlines()
	FileUtils.mkdir_p(File.dirname("srcnew/#{java}") )
	print "srcnew/#{java}\n"
	File.open("srcnew/#{java}", 'w' ){ |fnew|
		matchString="Messages.getString(.*)";
		lines.each{ |line|
			#pp line\
			l = lambda { |p| print p; "M.d(\"#{refSet[$1]}\")" }
			
			line.gsub(/".*?"/){ |s| 
				#print "string: #{s}\n"
			}

			line.gsub!(/Messages.getString\(\"(\w+_\d+)\"\)/, &l )
			fnew.write(line)	
		}
	}
	
}

