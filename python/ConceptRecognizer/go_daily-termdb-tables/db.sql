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
-- Table structure for table `db`
--

DROP TABLE IF EXISTS `db`;
CREATE TABLE `db` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(55) default NULL,
  `fullname` varchar(255) default NULL,
  `datatype` varchar(255) default NULL,
  `generic_url` varchar(255) default NULL,
  `url_syntax` varchar(255) default NULL,
  `url_example` varchar(255) default NULL,
  `uri_prefix` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `db0` (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `db1` (`name`),
  KEY `db2` (`fullname`),
  KEY `db3` (`datatype`)
) TYPE=MyISAM AUTO_INCREMENT=226;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
