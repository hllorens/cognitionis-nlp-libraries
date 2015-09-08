Multi-platform: It works on pure Linux or Pure Windows or Cygwin Linux.


nlp-taggers-1.0.jar
-a TRAIN_BASELINE_TAGGER   ---> outputs the model to STD Output by default








For cygwin use "cygpath":
java -jar cognitionis-nlp-libraries/nlp-taggers/target/nlp-taggers-1.0.jar -a TRAIN_BASELINE_TAGGER "$(cygpath -aw /home/Hector_Llorens/cognitionis-nlp-libraries/nlp-taggers/example-data/train-TE3-mini.txt)" -ap n=3 -d

Command to be run in Windows (CMD, not bash):
java -jar cognitionis-nlp-libraries\nlp-taggers\target\nlp-taggers-1.0.jar -a TRAIN_BASELINE_TAGGER C:\cygwin\home\Hector_Llorens\cognitionis-nlp-libraries\nlp-taggers\example-data\train-TE3-mini.txt -ap n=3 -d > caca.model

   
   
TODO:
Implement a basic unit test able to find resources inside the distro
    useful for debugging from eclipse...
Implement test method to test models