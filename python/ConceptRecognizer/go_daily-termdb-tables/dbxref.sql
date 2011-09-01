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
-- Table structure for table `dbxref`
--

DROP TABLE IF EXISTS `dbxref`;
CREATE TABLE `dbxref` (
  `id` int(11) NOT NULL auto_increment,
  `xref_dbname` varchar(55) NOT NULL,
  `xref_key` varchar(255) NOT NULL,
  `xref_keytype` varchar(32) default NULL,
  `xref_desc` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `xref_key` (`xref_key`,`xref_dbname`),
  UNIQUE KEY `dx0` (`id`),
  UNIQUE KEY `dx6` (`xref_key`,`xref_dbname`),
  KEY `dx1` (`xref_dbname`),
  KEY `dx2` (`xref_key`),
  KEY `dx3` (`id`,`xref_dbname`),
  KEY `dx4` (`id`,`xref_key`,`xref_dbname`),
  KEY `dx5` (`id`,`xref_key`)
) TYPE=MyISAM AUTO_INCREMENT=49702;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
