#!/bin/sh
(cd ../texticlj && lein install) 
lein uberjar
DEST=yablog_`date +%s`.jar
scp target/uberjar/yablog-0.1.0-SNAPSHOT-standalone.jar sehll.telent.net:share/$DEST
cat  <<EOF | ssh  sehll.telent.net /bin/sh
set -ex
cd share
test -L yablog.jar && rm yablog.jar
ln -s \$(pwd)/$DEST yablog.jar
EOF
