# !/bin/bash
cd /auroraBlogSystem/jar

java -jar article-boot-1.0-SNAPSHOT-run.jar -Xms200m -Xmx300m
java -jar aurora-admin-1.0-SNAPSHOT-run.jar -Xms200m -Xmx300m
java -jar auth-server-boot-1.0-SNAPSHOT-run.jar -Xms200m -Xmx250m
java -jar comment-boot-1.0-SNAPSHOT-run.jar -Xms200m -Xmx250m
java -jar file-boot-1.0-SNAPSHOT-run.jar -Xms200m -Xmx300m
java -jar gateway-boot-1.0-SNAPSHOT-run.jar -Xms200m -Xmx300m
java -jar message-boot-1.0-SNAPSHOT-run.jar -Xms200m -Xmx300m