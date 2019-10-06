# Yet Another Blog Engine

As the name suggests.  Serves my personal blog, which is a collection
of Textile files.

## Installation

* download JAR from Github releases

## Usage

Create an EDN file with configuration.  For example

```
{
 :base-folder "/var/www/blogs/telent.blog"
 :base-url "http://ww.telent.net/"
 :title "diary at Telent Netowrks"
 :description "Daniel Barlow's tech blog"
 :author "dan@telent.net (Daniel Barlow)"
 }
```


Then run

    $ java -jar yablog-0.1.0-standalone.jar conf.edn

## License

Copyright © 2015-2019 Daniel Barlow

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
