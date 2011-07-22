###########
#
#
# Mediawiki_XMLdumper.pl
#
#
# A script to download XML dumps of Mediawiki pages, using the Special:Export page.
# I: list of pages ($a)
# O: XML dumps in the $dir directory.
#
#
# 11/12/2009
# 
# Authors: M.Persico, S.Loguercio
#
#
###########



#!/usr/bin/perl
use strict;
use warnings;

use lib 'YourLibPathHere';
use HTML::TagFilter;
use lib 'YourLibPathHere';
use HTML::TagReader;
use lib 'YourLibPathHere';
use HTML::Scrubber::StripScripts;
use LWP::Simple;

require HTTP::Request;

my $command;
my $ua;
my $fname;
my $url;
my $content;
my $dir;
my $path;

$a='genelist.txt';
$dir="./YourDir/";
$path;
open(FILECONTENT, "< $a");

while (!eof(FILECONTENT))
{

my          $line=<FILECONTENT>;
            chomp $line;
          
           $url = 'http://en.wikipedia.org/wiki/Special:Export/' . $line;
           print "Url: $url\n";
           $fname= $line . '.xml';
           print $fname,"\n";
          
$ua="\"Mozilla/4.0\"";        

$path=$dir . $fname;
$command="wget --user-agent $ua -O $path $url";

print "Command: $command\n";

system("$command");

        
            
            }






