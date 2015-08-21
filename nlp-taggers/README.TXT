It works on pure Linux or Pure Windows
It fails in cygwin because of path issues: maybe there is a way to use absolute
windows paths there since the JVM is using those...


Command to be run in Windows (CMD, not bash):
C:\cygwin\home\Hector_Llorens>
   java -jar cognitionis-nlp-libraries\nlp-taggers\target\nlp-taggers-1.0.jar -a TRAIN_BASELINE_TAGGER C:\cygwin\home\Hector_Llorens\cognitionis-nlp-libraries\nlp-taggers\example-data\train-TE3-mini.txt -ap n=3 -d > caca.model

   it outputs the model to STD Output by default
   
   
TODO:
Implement a basic unit test able to find resources inside the distro
    useful for debugging from eclipse...
Implement test method to test models