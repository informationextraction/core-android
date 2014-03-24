filename = "resources.bin"
file= File.open(filename)
text=file.read

backdoorId = ["backdoorId","av3pVck1gb4eR2"]
aesKey = ["aesKey","3j9WmmDgBqyU270FTid3719g64bP4s52"]
confKey = ["confKey","Adf5V57gQtyi90wUhpb8Neg56756j87R"]
challengeKey = ["challengeKey","f7Hk0f5usd04apdvqw13F5ed25soV5eD"]
keys=[ backdoorId, aesKey, confKey, challengeKey ]

new_backdoorId="RCS_0000000179"
new_aesKey ="9edb164755177772af6bfd6fc9d56ffd"
new_confKey="a998767f8c3199b0338cb2d998084258"
new_challengeKey="572ebc94391281ccf53a851330bb0d99"

text.sub!(backdoorId.last,new_backdoorId)
text.sub!(aesKey.last,new_aesKey)
text.sub!(confKey.last,new_confKey)
text.sub!(challengeKey.last,new_challengeKey)

file= File.open(filename+".patched","w")
file.write text

