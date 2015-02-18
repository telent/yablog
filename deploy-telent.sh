#!/bin/sh
set -e 
HOST=sehll.telent.net
(cd ../texticlj && lein install) 
lein uberjar
DEST=yablog_`date +%s`.jar
scp target/uberjar/yablog-0.1.0-SNAPSHOT-standalone.jar sehll.telent.net:share/$DEST
cat  <<EOF | ssh $HOST /bin/sh
set -ex
cd share
test -L yablog.jar && rm yablog.jar
ln -s \$(pwd)/$DEST yablog.jar
EOF
# have to do this as separate step because of password prompt
ssh -t $HOST sudo sv term telent-blog coruskate-blog
