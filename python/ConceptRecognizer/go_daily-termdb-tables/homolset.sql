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
-- Table structure for table `homolset`
--

DROP TABLE IF EXISTS `homolset`;
CREATE TABLE `homolset` (
  `id` int(11) NOT NULL auto_increment,
  `symbol` varchar(128) default NULL,
  `dbxref_id` int(11) default NULL,
  `target_gene_product_id` int(11) default NULL,
  `taxon_id` int(11) default NULL,
  `type_id` int(11) default NULL,
  `description` text,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `dbxref_id` (`dbxref_id`),
  KEY `target_gene_product_id` (`target_gene_product_id`),
  KEY `taxon_id` (`taxon_id`),
  KEY `type_id` (`type_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
