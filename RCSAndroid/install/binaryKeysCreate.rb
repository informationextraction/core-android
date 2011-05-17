require 'active_support/secure_random'

#instanceId = ["instanceId","bg5etG87q20Kg52W5Fg1"]
backdoorId = ["backdoorId","av3pVck1gb4eR2"]
aesKey = ["aesKey","3j9WmmDgBqyU270FTid3719g64bP4s52"]
confKey = ["confKey","Adf5V57gQtyi90wUhpb8Neg56756j87R"]
challengeKey = ["challengeKey","f7Hk0f5usd04apdvqw13F5ed25soV5eD"]

keys=[ backdoorId, aesKey, confKey, challengeKey ]

# Generates a random string from a set of easily readable characters
def generate_random_string(size = 64)
  charset =  [('a'..'z'),('A'..'Z'),('0'..'9')].map{|i| i.to_a}.flatten;  
  (0...size).map{ charset.to_a[rand(charset.size)] }.join
end

filename="resources.bin"

if File.exists? filename
  File.delete(filename)
end

file=File.open(filename,"w")


random_string = generate_random_string
file.write random_string

keys.each do |pair|
  name=pair.first
  key=pair.last
  
  puts "#{name}=Utils.copy(resource, #{file.pos}, #{key.length});  "
	file.write key 
	file.write generate_random_string
end

file.close



