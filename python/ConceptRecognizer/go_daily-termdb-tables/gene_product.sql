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
-- Table structure for table `gene_product`
--

DROP TABLE IF EXISTS `gene_product`;
CREATE TABLE `gene_product` (
  `id` int(11) NOT NULL auto_increment,
  `symbol` varchar(128) NOT NULL,
  `dbxref_id` int(11) NOT NULL,
  `species_id` int(11) default NULL,
  `type_id` int(11) default NULL,
  `full_name` text,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `dbxref_id` (`dbxref_id`),
  UNIQUE KEY `g0` (`id`),
  KEY `type_id` (`type_id`),
  KEY `g1` (`symbol`),
  KEY `g2` (`dbxref_id`),
  KEY `g3` (`species_id`),
  KEY `g4` (`id`,`species_id`),
  KEY `g5` (`dbxref_id`,`species_id`),
  KEY `g6` (`id`,`dbxref_id`),
  KEY `g7` (`id`,`species_id`),
  KEY `g8` (`id`,`dbxref_id`,`species_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
