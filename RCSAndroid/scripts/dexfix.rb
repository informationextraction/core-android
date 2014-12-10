
require 'zlib'

filename = "/Users/zeno/Desktop/RCS\ Downloads/i/classes.dex"
fh = File.open(filename, 'rb')
content = fh.read()

actual = content.dup

adler32 = Zlib::adler32(content[12, content.size-12])
content[8] = [adler32].pack('C*')
content[9] = [adler32 >> 8].pack('C*')
content[10] = [adler32 >> 16].pack('C*')
content[11] = [adler32 >> 24].pack('C*')

puts actual[8..11].unpack("H*")
puts content[8..11].unpack("H*")