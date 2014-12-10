#!/usr/bin/ruby
require "optparse"

require 'openssl'
require 'zip' # zip
require 'zip/filesystem' # rubyzip
require 'rubygems'
require 'json'
require 'pp'
require 'digest/md5'

"""
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.3.0/android.zip
 core.android.default.apk md5=03e90c8c603c42977e706c681fc5a755
 core.android.default.v2.apk md5=cf4a89c9f85cf7bfda10fa6117a6b72f
 core.android.default.melt.apk md5=e449374d8cf5caa80692ce5a22d735d7
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.2.0/android.zip
 core.android.default.apk md5=c0870d801b03caaa0292754f3b2c2044
 core.android.default.v2.apk md5=315ae1c6d2e34dceb944f0f2ebdd9cf5
 core.android.default.melt.apk md5=720ecb0f5fbee65f91cbd62559b142fb
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.2.1/android.zip
 core.android.default.apk md5=6e894a1ddb7aed7e398add92aa5e563e
 core.android.default.v2.apk md5=e2f7cce9405a56eb8c8694eb9e195f15
 core.android.default.melt.apk md5=920336ed845548b9dfe0d3649ddeb0de
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.0.0/android.zip
 core.android.default.apk md5=c7ccdefad7c154a8d0a1e0f53d49316b
 core.android.default.v2.apk md5=bc427eeb26a706195772ba41825bfc9e
 core.android.default.melt.apk md5=7794a39bc2220cca4ad119035edf142d
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.1.0/android.zip
 core.android.default.apk md5=7c9b98d2027a8a4b628d431c58c70967
 core.android.default.v2.apk md5=22acb333af193441db0bb24153650e50
 core.android.default.melt.apk md5=ed2c161b8da6761f5b87add74836895b
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.1.5/android.zip
 core.android.default.apk md5=ecf914bcd533fb23570982d3fc5449e7
 core.android.default.v2.apk md5=e31327e8e5db9db308599a21249458e3
 core.android.default.melt.apk md5=0458f92001d48f192fb26cc9db4a44b7
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//8.4.1/android.zip
 core.android.default.apk md5=8b6ff935d4c7e12a1a7458e4994e4778
 core.android.default.v2.apk md5=a413f01503427b6f226a13b2e8799d91
 core.android.default.melt.apk md5=56510f977ac7eede8cf481a5ff1c65db
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.2.3/android.zip
 core.android.default.apk md5=a3d065c92de286c1ef40cb1f282da65c
 core.android.default.v2.apk md5=36fd79e15448fd1a332db9a415690d49
 core.android.default.melt.apk md5=9c5302c8ec2cbe1cf62c78d904f78132
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//9.1.1/android.zip
 core.android.default.apk md5=ecf914bcd533fb23570982d3fc5449e7
 core.android.default.v2.apk md5=e31327e8e5db9db308599a21249458e3
 core.android.default.melt.apk md5=0458f92001d48f192fb26cc9db4a44b7
checking /Volumes/SHARE/RELEASE/SVILUPPO/previous cores//8.4.0/android.zip
 core.android.default.apk md5=8b6ff935d4c7e12a1a7458e4994e4778
 core.android.default.v2.apk md5=a413f01503427b6f226a13b2e8799d91
 core.android.default.melt.apk md5=56510f977ac7eede8cf481a5ff1c65db
"""

  def aes_decrypt(enc_text, key, padding = 1)
    decipher = OpenSSL::Cipher.new('AES-128-CBC')
    decipher.decrypt
    decipher.padding = padding
    decipher.key = key
    decipher.iv = "\x00" * decipher.iv_len
    data = decipher.update(enc_text)
    data << decipher.final
    return data
  end
  def analyze_android(file, dir)
    conf = ""
    core = ""
    digest = ""

    Zip::File.open(file) do |z|
      conf = z.file.open('assets/c.bin', "rb") { |f| f.read }
      core = z.file.open('assets/r.bin', "rb") { |f| f.read }
      digest = z.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}

    end

    ident = 'RCS_' + core[4..13]
    water = core[150..157]
    key = core[46...(46+16)]
   # key = core[46...78]
    conf = aes_decrypt(conf, key)
    conf = conf[0...-20]
    json = JSON.parse(conf)
    actions = json['actions']
    sync = nil
    actions.each do |action|
      action['subactions'].each do |sub|
        if action['desc'].eql?("SYNC")
        sync =  sub['host']
        end
      end
    end if !actions.nil?

    puts "WATERMARK: #{water}"
    puts 'IDENT: ' + ident
    puts 'SYNC ADDRESS: ' + (sync ? sync : 'failed to retrieve')
    puts 'MD5 digest: ' + digest
    File.open("./c.conf","w+") {|r| r.write(json.pretty_inspect)}

    if !dir.nil?
      Dir.glob(dir+'/**/android.zip') do |f|
        puts "checking "+f
        Zip::File.open(f) do |z|
          tmp = z.file.open('core.android.default.apk', "rb") { |f| f.read }
          File.delete("./tmp.conf") if File.exist?("./tmp.conf")
          File.binwrite("./tmp.conf",tmp,0)
          Zip::File.open("./tmp.conf") do |zz|
            digestTmp = zz.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}
            if(digest.eql?digestTmp)
              puts "found in " +f+  " core.android.default.apk md5="+digestTmp+"MATCH!"
              return;
            end
          end
          tmp = z.file.open('core.android.v2.apk', "rb") { |f| f.read }
          File.delete("./tmp.conf") if File.exist?("./tmp.conf")
          File.binwrite("./tmp.conf",tmp,0)
          Zip::File.open("./tmp.conf") do |zz|
            digestTmp = zz.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}
            if(digest.eql?digestTmp)
              puts "found in " +f+  " core.android.v2.apk md5="+digestTmp+"MATCH!"
              return;
            end
          end
          tmp = z.file.open('core.android.melt.apk', "rb") { |f| f.read }
          File.delete("./tmp.conf") if File.exist?("./tmp.conf")
          File.binwrite("./tmp.conf",tmp,0)
          Zip::File.open("./tmp.conf") do |zz|
            digestTmp = zz.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}
            if(digest.eql?digestTmp)
              puts "found in " +f+  " core.android.melt.apk md5="+digestTmp+"MATCH!"
              return;
            end
          end
        end
        # core.android.default.apk core.android.melt.apk core.android.v2.apk

      end
    end

  end

  def check_md5(f, file, digest)
    puts "checking "+f
        Zip::File.open(f) do |z|
          tmp = z.file.open('core.android.default.apk', "rb") { |f| f.read }
          File.delete("./tmp.conf") if File.exist?("./tmp.conf")
          File.binwrite("./tmp.conf",tmp,0)
          Zip::File.open("./tmp.conf") do |zz|
            digestTmp = zz.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}
             puts " core.android.default.apk md5="+digestTmp
            if(digest.eql?digestTmp)
              puts "found in " +f+  " core.android.default.apk md5="+digestTmp+"MATCH!"
              return;
            end
          end
          tmp = z.file.open('core.android.v2.apk', "rb") { |f| f.read }
          File.delete("./tmp.conf") if File.exist?("./tmp.conf")
          File.binwrite("./tmp.conf",tmp,0)
          Zip::File.open("./tmp.conf") do |zz|
            digestTmp = zz.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}
            puts " core.android.default.v2.apk md5="+digestTmp
            if(digest.eql?digestTmp)
              puts "found in " +f+  " core.android.v2.apk md5="+digestTmp+"MATCH!"
              return;
            end
          end
          tmp = z.file.open('core.android.melt.apk', "rb") { |f| f.read }
          File.delete("./tmp.conf") if File.exist?("./tmp.conf")
          File.binwrite("./tmp.conf",tmp,0)
          Zip::File.open("./tmp.conf") do |zz|
            digestTmp = zz.file.open('classes.dex', "rb") { |f| Digest::MD5.hexdigest(f.read)}
            puts " core.android.default.melt.apk md5="+digestTmp
            if(digest.eql?digestTmp)
              puts "found in " +f+  " core.android.melt.apk md5="+digestTmp+"MATCH!"
              return;
            end
          end
        end
        # core.android.default.apk core.android.melt.apk core.android.v2.apk
  end

  def analyze_dex(file, dir)
    digest = ""
    digest = Digest::MD5.hexdigest(file)


    puts 'MD5 digest: ' + digest

    if !dir.nil?
      Dir.glob(dir+'/**/android.zip') do |f|
        check_md5(f, file, digest)
      end
      Dir.glob(dir+'/**/android_2*.zip') do |f|
        check_md5(f, file, digest)
      end
    end

  end


################## Main file
options = {}

options[:dir] = "/Volumes/SHARE/RELEASE/SVILUPPO/previous cores/"

optparse = OptionParser.new do |opts|
  opts.banner = "extracts info from RCS zip/apk"

  opts.on("-z", "--zip zip", "RCS zip/apk file") do |zip|
    options[:apkfile] = zip
  end
  opts.on("-D", "--dex dexsample", "searching only dex") do |file|
    options[:dex] = file
  end
  opts.on("-d", "--dir dir", "searching dir for md5 match with old core") do |file|
    options[:dir] = file
  end
end

optparse.parse!

if options[:apkfile].nil? and options[:dex].nil?
  printf("usage: -z <zip/apk to analyze>\n")
  printf("usage: -d <dir for md5 match>\n")
  exit 0
elsif  options[:dex].nil? and  options[:dir].nil?
  printf("usage: -D <dex to analyze>\n")
  printf("usage: -d <dir for md5 match>\n")
  exit 0
end

printf("CHECKING "+ options[:apkfile] +"\n") if not options[:apkfile].nil?
analyze_android(options[:apkfile].to_s,options[:dir]) if not options[:apkfile].nil?
analyze_dex(options[:dex].to_s,options[:dir]) if not options[:dex].nil?

# examples:
# ../decryptRCS/extractInfo.rb -z  33ae1e1bc6596238876e167b9df67b8bd4f57bb14f08fd67048364ad3398bb17.zip -d '/Volumes/SHARE/RELEASE/SVILUPPO/previous cores/'
# if the sample is a DEX
# ../decryptRCS/extractInfo.rb -D  33ae1e1bc6596238876e167b9df67b8bd4f57bb14f08fd67048364ad3398bb17.zip -d '/Volumes/SHARE/RELEASE/SVILUPPO/previous cores/'
