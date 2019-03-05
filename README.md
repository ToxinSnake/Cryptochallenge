# Cryptochallenge

The goal was to crack an asymmetrically encrypted message. The encryption-scheme was the very unknown "Imai-Matsumoto Public-Key Cryptosystem C*" which was never uses in practice because it was broken very early on in 1998, although the German Federal Intelligence Service broke it three years earlier.
See: https://www.researchgate.net/publication/220638622_Cryptanalysis_of_the_Matsumoto_and_Imai_Public_Key_Scheme_of_Eurocrypt'98

Only the encrypted message and public key were given. 

# Installation (Linux)

1. Clone the project: 
"git clone https://github.com/ToxinSnake/Cryptochallenge.git"

2. Switch into the cloned folder an run:
"mvn package"maven no manifest
This will create a runnable .jar file in the target folder

3. Run the program with an suitable Public-Key + encrypted message. There are four example files in this repository, called "3Bit.txt", "5Bit.txt", "7Bit.txt" and "45Bit.txt"
To run with 45Bit.txt use: "java -jar ./target/Hackerman-jar-with-dependencies.jar ./45Bit.txt"
