-- MySQL dump 10.11
--
-- Host: localhost    Database: go
-- ------------------------------------------------------
-- Server version	5.0.45
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE=',MYSQL40' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `seq`
--

DROP TABLE IF EXISTS `seq`;
CREATE TABLE `seq` (
  `id` int(11) NOT NULL auto_increment,
  `display_id` varchar(64) default NULL,
  `description` varchar(255) default NULL,
  `seq` mediumtext,
  `seq_len` int(11) default NULL,
  `md5checksum` varchar(32) default NULL,
  `moltype` varchar(25) default NULL,
  `timestamp` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `seq0` (`id`),
  UNIQUE KEY `display_id` (`display_id`,`md5checksum`),
  KEY `seq1` (`display_id`),
  KEY `seq2` (`md5checksum`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
