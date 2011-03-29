#!/usr/local/bin/perl 

#
# what_links_PBB.pl
#
# retrieve the list of all Gene Wiki pages
#
# AS 20100204  
#

use LWP::Simple;
use JSON;

$x=get("http://genewikitools.appspot.com/ListPages");
$y=from_json($x);
$z=join "\n",@{$$y{"PageList"}};
print $z;